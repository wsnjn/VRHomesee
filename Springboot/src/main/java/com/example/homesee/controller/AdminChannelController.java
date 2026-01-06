/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：AdminChannelController.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：管理员频道管理控制器，负责聊天消息监管、用户禁言以及社区动态的审核与管理
 * 创建日期：2026-01-06
 */
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

    // 聊天管理
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

    // 社区管理
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
