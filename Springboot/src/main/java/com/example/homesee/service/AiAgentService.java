package com.example.homesee.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiAgentService {

    // ModelScope API配置
    private static final String MODELSCOPE_API_KEY = "ms-41438d67-36a2-46d9-a206-309db6e1ac81";
    private static final String MODELSCOPE_URL = "https://api-inference.modelscope.cn/v1/chat/completions";

    // 模型名称
    private static final String TEXT_MODEL = "deepseek-ai/DeepSeek-V3.2";
    private static final String VISION_MODEL = "Qwen/Qwen3-VL-8B-Instruct";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * VR看房AI对话
     * 
     * @param userMessage         用户消息
     * @param roomInfo            房源信息
     * @param conversationHistory 对话历史
     * @param screenshotBase64    VR截图（可选）
     * @return AI回复
     */
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.homesee.repository.ChatHistoryRepository chatHistoryRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.homesee.repository.UserRepository userRepository;

    /**
     * VR看房AI对话
     * 
     * @param userId              用户ID (用于存储)
     * @param userMessage         用户消息
     * @param roomInfo            房源信息
     * @param conversationHistory 对话历史
     * @param screenshotBase64    VR截图（可选）
     * @return AI回复
     */
    public String chat(Long userId, String userMessage, Map<String, Object> roomInfo,
            List<Map<String, String>> conversationHistory, String screenshotBase64) {

        com.example.homesee.entity.User user = null;

        // 1. 保存用户消息
        if (userId != null) {
            try {
                // 保存消息
                com.example.homesee.entity.ChatHistory userHistory = new com.example.homesee.entity.ChatHistory(userId,
                        "user", userMessage);
                chatHistoryRepository.save(userHistory);

                // 获取用户信息用于Prompt
                user = userRepository.findById(userId).orElse(null);
            } catch (Exception e) {
                System.err.println("Failed to save user message or get user: " + e.getMessage());
            }
        }

        // 构建系统提示词 (传入用户信息)
        String systemPrompt = buildSystemPrompt(roomInfo, user);

        // 第一步：让DeepSeek判断是否需要图片理解
        String firstResponse = callDeepSeek(systemPrompt, userMessage, conversationHistory, null);

        String finalResponse;

        // 检查是否需要图片理解
        if (firstResponse.contains("[NEED_VISION]") && screenshotBase64 != null && !screenshotBase64.isEmpty()) {
            // 调用视觉模型分析图片
            String imageDescription = analyzeImage(screenshotBase64, userMessage);

            // 将图片描述作为上下文，再次调用DeepSeek生成最终回答
            String contextWithVision = "用户问题：" + userMessage + "\n\n" +
                    "【图片分析结果】\n" + imageDescription + "\n\n" +
                    "请根据图片分析结果和房源信息，回答用户的问题。";

            finalResponse = callDeepSeek(systemPrompt, contextWithVision, conversationHistory, null);
        } else {
            // 不需要图片，直接使用第一次回答
            finalResponse = firstResponse;
        }

        // 移除 [NEED_VISION] 标记，确保展示给用户的回复干净
        String cleanedResponse = finalResponse.replace("[NEED_VISION]", "").trim();

        // 2. 保存AI回答
        if (userId != null) {
            try {
                com.example.homesee.entity.ChatHistory aiHistory = new com.example.homesee.entity.ChatHistory(userId,
                        "assistant", cleanedResponse);
                chatHistoryRepository.save(aiHistory);
            } catch (Exception e) {
                System.err.println("Failed to save AI message: " + e.getMessage());
            }
        }

        return cleanedResponse;
    }

    /**
     * 调用DeepSeek文本模型
     */
    private String callDeepSeek(String systemPrompt, String userMessage,
            List<Map<String, String>> history, String additionalContext) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // 系统提示词
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // 对话历史
        if (history != null) {
            for (Map<String, String> msg : history) {
                messages.add(Map.of("role", msg.get("role"), "content", msg.get("content")));
            }
        }

        // 当前用户消息
        String content = userMessage;
        if (additionalContext != null) {
            content = additionalContext + "\n\n用户原始问题：" + userMessage;
        }
        messages.add(Map.of("role", "user", "content", content));

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", TEXT_MODEL);
        payload.put("messages", messages);
        payload.put("max_tokens", 2048);
        payload.put("stream", false);

        return callModelScope(payload);
    }

    /**
     * 调用Qwen-VL视觉模型分析图片
     */
    public String analyzeImage(String imageBase64, String question) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // 构建视觉消息
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of("type", "text", "text", "请描述这张房间图片的内容，重点关注：" + question));
        content.add(Map.of(
                "type", "image_url",
                "image_url", Map.of("url", imageBase64)));

        messages.add(Map.of("role", "user", "content", content));

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", VISION_MODEL);
        payload.put("messages", messages);
        payload.put("max_tokens", 1024);
        payload.put("stream", false);

        return callModelScope(payload);
    }

    /**
     * 调用ModelScope API
     */
    private String callModelScope(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + MODELSCOPE_API_KEY);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            System.out.println("Calling ModelScope API with model: " + payload.get("model"));
            ResponseEntity<Map> response = restTemplate.postForEntity(MODELSCOPE_URL, request, Map.class);

            if (response.getBody() != null) {
                List<Map> choices = (List<Map>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map message = (Map) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "抱歉，AI 暂时无法回答您的问题。";
        } catch (Exception e) {
            System.err.println("ModelScope API Error: " + e.getMessage());
            e.printStackTrace();
            return "抱歉，AI 服务暂时不可用：" + e.getMessage();
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(Map<String, Object> roomInfo, com.example.homesee.entity.User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的房产顾问AI助手，正在陪同用户进行VR看房。\n\n");

        if (user != null) {
            String name = user.getRealName();
            if (name == null || name.isEmpty()) {
                name = user.getUsername();
            }
            sb.append("【当前用户】\n");
            sb.append("称呼：").append(name).append("\n");
            if (user.getPhone() != null) {
                sb.append("联系方式：").append(user.getPhone()).append("\n");
            }
            sb.append("\n");
        }

        if (roomInfo != null && !roomInfo.isEmpty()) {
            sb.append("【当前房源信息】\n");
            if (roomInfo.containsKey("title"))
                sb.append("- 标题：").append(roomInfo.get("title")).append("\n");
            if (roomInfo.containsKey("price"))
                sb.append("- 价格：").append(roomInfo.get("price")).append(" 元/月\n");
            if (roomInfo.containsKey("area"))
                sb.append("- 面积：").append(roomInfo.get("area")).append(" 平方米\n");
            if (roomInfo.containsKey("layout"))
                sb.append("- 户型：").append(roomInfo.get("layout")).append("\n");
            if (roomInfo.containsKey("address"))
                sb.append("- 地址：").append(roomInfo.get("address")).append("\n");
            if (roomInfo.containsKey("description"))
                sb.append("- 描述：").append(roomInfo.get("description")).append("\n");

            // 处理其他字段映射
            if (roomInfo.containsKey("rentPrice"))
                sb.append("- 租金：").append(roomInfo.get("rentPrice")).append(" 元/月\n");
            if (roomInfo.containsKey("communityName"))
                sb.append("- 小区：").append(roomInfo.get("communityName")).append("\n");
            if (roomInfo.containsKey("roomArea"))
                sb.append("- 面积：").append(roomInfo.get("roomArea")).append(" ㎡\n");
        }

        sb.append("\n你的职责：\n");
        sb.append("1. 回答关于房源的问题\n");
        sb.append("2. 如果用户询问需要看画面才能回答的问题（如房间布局、窗户朝向、装修风格等），请在回复开头加上 [NEED_VISION] 标记\n");
        sb.append("3. 提供专业的看房建议\n");
        sb.append("4. 语气友好专业，像真人顾问一样\n");

        sb.append("\n【重要指令】\n");
        sb.append("请使用HTML标签来格式化你的回答，使其美观易读：\n");
        sb.append("   - 使用 <p> 标签分段。\n");
        sb.append("   - 使用 <br> 换行。\n");
        sb.append("   - 使用 <strong> 加粗重要信息（如小区名、价格、核心亮点）。\n");
        sb.append("   - 不要使用Markdown格式，直接返回HTML字符串。\n");

        return sb.toString();
    }
}
