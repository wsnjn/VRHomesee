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
