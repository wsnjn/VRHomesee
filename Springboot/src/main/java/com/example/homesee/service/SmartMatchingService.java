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
        // 1. Save User Message
        ChatHistory userHistory = new ChatHistory(userId, "user", userMessage);
        chatHistoryRepository.save(userHistory);

        // 2. Prepare Context
        User user = userRepository.findById(userId).orElse(null);
        List<RoomInfo> rooms = roomInfoRepository.findAll(); // In production, limit this or use vector search
        List<ChatHistory> history = chatHistoryRepository.findByUserIdOrderByCreatedTimeAsc(userId);

        // 3. Build Prompt
        List<Map<String, String>> messages = new ArrayList<>();

        // System Prompt with Context
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
            if (room.getStatus() == 0) { // Only available rooms
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
                "   - VR在线看房: <a href=\"http://localhost:5173/house-tour?houseId={id}\" target=\"_blank\">VR看房</a>\n");
        systemContent.append(
                "   - 预约现场看房: <a href=\"http://localhost:5173/appointment?houseId={id}\" target=\"_blank\">预约看房</a>\n");
        systemContent.append("2. 请使用HTML标签来格式化你的回答，使其美观易读：\n");
        systemContent.append("   - 使用 <p> 标签分段。\n");
        systemContent.append("   - 使用 <br> 换行。\n");
        systemContent.append("   - 使用 <strong> 加粗重要信息（如小区名、价格）。\n");
        systemContent.append("   - 不要使用Markdown格式，直接返回HTML字符串。\n");

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemContent.toString());
        messages.add(systemMessage);

        // History Messages (Limit to last 20 to avoid token limits)
        int start = Math.max(0, history.size() - 20);
        for (int i = start; i < history.size(); i++) {
            ChatHistory h = history.get(i);
            // Skip the one we just saved to avoid duplication if we re-fetch,
            // but here 'history' includes the one we just saved?
            // Actually 'history' was fetched AFTER saving, so it includes the current
            // message.
            // We should exclude the last one because we want to send it as the 'latest'
            // message or just include all.
            // DeepSeek API expects the conversation flow.
            // Let's just add all history including the current one.
            Map<String, String> msg = new HashMap<>();
            msg.put("role", h.getRole());
            msg.put("content", h.getContent());
            messages.add(msg);
        }

        // 4. Call DeepSeek API
        String aiResponseContent = callDeepSeekApi(messages);

        // 5. Save AI Response
        ChatHistory aiHistory = new ChatHistory(userId, "assistant", aiResponseContent);
        return chatHistoryRepository.save(aiHistory);
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
