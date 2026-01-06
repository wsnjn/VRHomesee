/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：SmartMatchingService.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：智能选房服务类，对接DeepSeek大模型实现用户交互需求精准画像、房源智能匹配推荐及响应结果的敏感信息脱敏（URL净化）
 * 创建日期：2026-01-06
 */
package com.example.homesee.service;

import com.example.homesee.entity.ChatHistory;
import com.example.homesee.entity.RoomInfo;
import com.example.homesee.entity.User;
import com.example.homesee.repository.ChatHistoryRepository;
import com.example.homesee.repository.RoomInfoRepository;
import com.example.homesee.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SmartMatchingService {

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomInfoRepository roomInfoRepository;

    private final String API_KEY = "sk-e2fdf28cd6f34468a5d80ad14e840d77";
    private final String API_URL = "https://api.deepseek.com/chat/completions";

    public List<ChatHistory> getHistory(Long userId) {
        return chatHistoryRepository.findByUserIdOrderByCreatedTimeAsc(userId);
    }

    public ChatHistory chat(Long userId, String userMessage) {
        // 1. 保存用户消息
        ChatHistory userHistory = new ChatHistory(userId, "user", userMessage);
        chatHistoryRepository.save(userHistory);

        // 2. 准备上下文数据
        User user = userRepository.findById(userId).orElse(null);
        List<RoomInfo> rooms = roomInfoRepository.findAll(); // 生产环境下建议使用向量搜索或分页限制数量
        List<ChatHistory> history = chatHistoryRepository.findByUserIdOrderByCreatedTimeAsc(userId);

        // 3. 构建提示词模板
        List<Map<String, String>> messages = new ArrayList<>();

        // 带上下文的系统提示词
        StringBuilder systemContent = new StringBuilder();
        systemContent.append("你是一个专业的房屋租赁智能助手。你的目标是帮助用户找到最合适的房源。\n\n");

        if (user != null) {
            systemContent.append("【当前用户信息】\n");
            systemContent.append("姓名: ").append(user.getRealName() != null ? user.getRealName() : user.getUsername())
                    .append("\n");
            systemContent.append("预算范围: ").append(user.getRentalBudgetMin()).append(" - ")
                    .append(user.getRentalBudgetMax()).append(" 元/月\n");
            systemContent.append("偏好区域: ").append(user.getPreferredDistricts()).append("\n");
            systemContent.append("其他要求: ").append(user.getHouseRequirements()).append("\n\n");
        }

        systemContent.append("【可用房源列表】\n");
        for (RoomInfo room : rooms) {
            if (room.getStatus() == 0) { // 仅显示可租房源
                systemContent.append(String.format("- ID:%d, %s%s%s%s, %s, %.0f元/月, %s\n",
                        room.getId(), room.getCity(), room.getDistrict(), room.getStreet(), room.getCommunityName(),
                        room.getRoomNumber() != null ? room.getRoomNumber() : "",
                        room.getRentPrice(),
                        room.getDescription() != null
                                ? room.getDescription().substring(0, Math.min(room.getDescription().length(), 50))
                                : ""));
            }
        }
        systemContent.append("\n请根据用户的需求和上述房源信息进行推荐。");
        systemContent.append("\n【重要指令】\n");
        systemContent.append("1. 如果推荐了具体房源，必须提供以下两个链接（请使用HTML格式）：\n");
        systemContent.append(
                "   - VR在线看房: <a href=\"https://www.homesee.xyz/#/house-tour?houseId={id}\" target=\"_blank\">VR看房</a>\n");
        systemContent.append(
                "   - 预约现场看房: <a href=\"https://www.homesee.xyz/#/appointment?houseId={id}\" target=\"_blank\">预约看房</a>\n");
        systemContent.append("   ⚠️ 【严格禁止】绝对不允许在链接中使用任何IP地址！必须使用域名 https://www.homesee.xyz\n");
        systemContent.append("   ⚠️ 如果你在历史消息中看到了带IP地址的链接（如 http://39.108.142.250），那是错误的，请忽略并使用正确的域名。\n");
        systemContent.append("2. 请使用HTML标签来格式化你的回答，使其美观易读：\n");
        systemContent.append("   - 使用 <p> 标签分段。\n");
        systemContent.append("   - 使用 <br> 换行。\n");
        systemContent.append("   - 使用 <strong> 加粗重要信息（如小区名、价格）。\n");
        systemContent.append("   - 不要使用Markdown格式，直接返回HTML字符串。\n");
        systemContent.append("3. 如果用户的意图是进行页面跳转（例如：'我想看VR'，'带我去预约'），请在回答的最后附加一个隐藏的JSON指令：\n");
        systemContent.append("   <!-- ACTION: {\"type\": \"navigate\", \"path\": \"/target-path\"} -->\n");
        systemContent.append("   常用路径：\n");
        systemContent.append("   - VR看房: /house-tour?houseId={id}\n");
        systemContent.append("   - 预约看房: /appointment?houseId={id}\n");
        systemContent.append("   - 登录: /login\n");
        systemContent.append("   - 注册: /register\n");
        systemContent.append("   - 个人中心: /profile\n");

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemContent.toString());
        messages.add(systemMessage);

        // 历史消息处理（限制为最近20条以避免超出 Token 限制）
        int start = Math.max(0, history.size() - 20);
        for (int i = start; i < history.size(); i++) {
            ChatHistory h = history.get(i);
            // 这里的 history 包含刚刚保存的消息。
            // 按照 DeepSeek API 要求的对话流，我们将包含当前消息在内的所有历史记录加入。
            Map<String, String> msg = new HashMap<>();
            msg.put("role", h.getRole());
            msg.put("content", h.getContent());
            messages.add(msg);
        }

        // 4. 调用 DeepSeek API接口
        String aiResponseContent = callDeepSeekApi(messages);

        // 5. 结果清理：将响应中的所有 IP 地址替换为正确的域名
        aiResponseContent = sanitizeResponse(aiResponseContent);

        // 6. 保存 AI 回复内容
        ChatHistory aiHistory = new ChatHistory(userId, "assistant", aiResponseContent);
        return chatHistoryRepository.save(aiHistory);
    }

    /**
     * 清理 AI 响应，将基于 IP 的 URL 替换为基于域名的 URL
     */
    private String sanitizeResponse(String content) {
        if (content == null)
            return content;

        // 将 http://IP:端口 替换为 https://www.homesee.xyz
        // 正则模式: http://[IP地址]:[可选端口号]
        content = content.replaceAll("http://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d+)?",
                "https://www.homesee.xyz");

        return content;
    }

    private String callDeepSeekApi(List<Map<String, String>> messages) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-chat");
        body.put("messages", messages);
        body.put("stream", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，我现在无法连接到AI服务，请稍后再试。";
        }
    }
}
