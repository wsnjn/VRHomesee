/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：SmartMatchingController.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：智能匹配对话控制器，提供基于大语言模型（LLM）的智能选房咨询协助服务，支持对话历史追溯及多轮语义理解
 * 创建日期：2026-01-06
 */
package com.example.homesee.controller;

import com.example.homesee.entity.ChatHistory;
import com.example.homesee.service.SmartMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/smart-matching")
@CrossOrigin(origins = "http://39.108.142.250:9999,http://localhost:5173", allowCredentials = "true")
public class SmartMatchingController {

    @Autowired
    private SmartMatchingService smartMatchingService;

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatHistory>> getHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(smartMatchingService.getHistory(userId));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String message = request.get("message").toString();

            ChatHistory aiResponse = smartMatchingService.chat(userId, message);

            response.put("success", true);
            response.put("data", aiResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Chat failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
