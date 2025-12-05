package com.example.homesee.controller;

import com.example.homesee.service.AdminChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/channel")
@CrossOrigin(origins = {
        "http://39.108.142.250:9999",
        "http://localhost:5173",
        "https://www.homesee.xyz",
        "https://homesee.xyz"
})

public class AdminChannelController {

    @Autowired
    private AdminChannelService adminChannelService;

    // Chat Management
    @GetMapping("/chat/messages")
    public ResponseEntity<List<Map<String, Object>>> getAllChatMessages() {
        return ResponseEntity.ok(adminChannelService.getAllChatMessages());
    }

    @DeleteMapping("/chat/message/{id}")
    public ResponseEntity<Void> deleteChatMessage(@PathVariable Long id) {
        adminChannelService.deleteChatMessage(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/chat/mute")
    public ResponseEntity<Void> muteUser(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Integer duration = Integer.valueOf(payload.get("duration").toString());
        adminChannelService.muteUser(userId, duration);
        return ResponseEntity.ok().build();
    }

    // Community Management
    @GetMapping("/community/posts")
    public ResponseEntity<List<Map<String, Object>>> getAllSocialPosts() {
        return ResponseEntity.ok(adminChannelService.getAllSocialPosts());
    }

    @DeleteMapping("/community/post/{id}")
    public ResponseEntity<Void> deleteSocialPost(@PathVariable Long id) {
        adminChannelService.deleteSocialPost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/community/ban")
    public ResponseEntity<Void> banUserFromPosting(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Integer duration = Integer.valueOf(payload.get("duration").toString());
        adminChannelService.banUserFromPosting(userId, duration);
        return ResponseEntity.ok().build();
    }
}
