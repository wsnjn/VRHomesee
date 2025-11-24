package com.example.homesee.controller;

import com.example.homesee.entity.ChatGroup;
import com.example.homesee.entity.ChatMessage;
import com.example.homesee.entity.SocialPost;
import com.example.homesee.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@CrossOrigin
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    // --- Chat Groups ---
    @GetMapping("/groups")
    public ResponseEntity<Map<String, Object>> getAllGroups() {
        List<ChatGroup> list = communityService.getAllGroups();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/groups/create")
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody ChatGroup group) {
        ChatGroup created = communityService.createGroup(group);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", created);
        return ResponseEntity.ok(response);
    }

    // --- Chat Messages ---
    @GetMapping("/messages/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupMessages(@PathVariable Long groupId) {
        List<ChatMessage> list = communityService.getGroupMessages(groupId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/messages/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody ChatMessage message) {
        ChatMessage sent = communityService.sendMessage(message);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", sent);
        return ResponseEntity.ok(response);
    }

    // --- Social Feed ---
    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getAllPosts() {
        List<SocialPost> list = communityService.getAllPosts();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts/create")
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody SocialPost post) {
        SocialPost created = communityService.createPost(post);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", created);
        return ResponseEntity.ok(response);
    }

    // --- Group Membership ---
    @PostMapping("/groups/join")
    public ResponseEntity<Map<String, Object>> joinGroup(@RequestBody Map<String, Object> request) {
        Long groupId = Long.valueOf(request.get("groupId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        Integer role = request.get("role") != null ? Integer.valueOf(request.get("role").toString()) : 0;

        var member = communityService.joinGroup(groupId, userId, role);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", member);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserGroups(@PathVariable Long userId) {
        var groups = communityService.getUserGroups(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", groups);
        return ResponseEntity.ok(response);
    }

    // --- Friendships ---
    @PostMapping("/friends/request")
    public ResponseEntity<Map<String, Object>> sendFriendRequest(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long friendId = Long.valueOf(request.get("friendId").toString());

        var friendship = communityService.sendFriendRequest(userId, friendId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", friendship);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/friends/respond")
    public ResponseEntity<Map<String, Object>> respondToFriendRequest(@RequestBody Map<String, Object> request) {
        Long requestId = Long.valueOf(request.get("requestId").toString());
        Integer status = Integer.valueOf(request.get("status").toString());

        var friendship = communityService.respondToFriendRequest(requestId, status);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", friendship);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<Map<String, Object>> getFriends(@PathVariable Long userId) {
        var friends = communityService.getFriends(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", friends);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/friends/pending/{userId}")
    public ResponseEntity<Map<String, Object>> getPendingRequests(@PathVariable Long userId) {
        var requests = communityService.getPendingRequests(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", requests);
        return ResponseEntity.ok(response);
    }

    // --- Media Upload ---
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMedia(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            // In a real app, save the file to storage (S3, local disk, etc.)
            // For this demo, we'll return a mock URL or the original filename
            String originalFilename = file.getOriginalFilename();
            // Mock URL - in production this would be the actual URL to access the file
            String fileUrl = "http://localhost:8080/uploads/" + originalFilename;

            response.put("success", true);
            response.put("url", fileUrl);
            response.put("message", "File uploaded successfully (Mock)");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
