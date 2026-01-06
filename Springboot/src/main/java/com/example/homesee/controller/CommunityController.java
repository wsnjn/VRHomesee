/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：CommunityController.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：社区与社交管理控制器，提供聊天群组维护（创建、解散、邀请）、即时通讯消息收发、朋友圈动态社交互动及好友关系维护功能
 * 创建日期：2026-01-06
 */
package com.example.homesee.controller;

import com.example.homesee.entity.ChatGroup;
import com.example.homesee.entity.ChatMessage;
import com.example.homesee.entity.SocialPost;
import com.example.homesee.entity.SocialComment;
import com.example.homesee.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    // --- 聊天群组管理 ---
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

    @PostMapping("/groups/private")
    public ResponseEntity<Map<String, Object>> getPrivateChatGroup(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long friendId = Long.valueOf(request.get("friendId").toString());

        ChatGroup group = communityService.getPrivateChatGroup(userId, friendId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", group);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/groups/delete")
    public ResponseEntity<Map<String, Object>> deleteGroup(@RequestBody Map<String, Object> request) {
        Long groupId = Long.valueOf(request.get("groupId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());

        try {
            communityService.deleteGroup(groupId, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "群组删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/groups/invite")
    public ResponseEntity<Map<String, Object>> inviteToGroup(@RequestBody Map<String, Object> request) {
        Long groupId = Long.valueOf(request.get("groupId").toString());
        Long inviterId = Long.valueOf(request.get("inviterId").toString());
        Long inviteeId = Long.valueOf(request.get("inviteeId").toString());

        try {
            communityService.inviteToGroup(groupId, inviterId, inviteeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户邀请成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // --- 聊天消息记录 ---
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

    // --- 社交朋友圈动态 ---
    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getAllPosts() {
        List<SocialPost> list = communityService.getAllPosts();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/with-user-info")
    public ResponseEntity<Map<String, Object>> getAllPostsWithUserInfo(@RequestParam(required = false) Long userId) {
        var list = communityService.getAllPostsWithUserInfo(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long postId,
            @RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        boolean isLiked = communityService.toggleLike(postId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isLiked", isLiked);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts/{postId}/comment")
    public ResponseEntity<Map<String, Object>> addComment(@PathVariable Long postId,
            @RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String content = (String) request.get("content");

        SocialComment comment = communityService.addComment(postId, userId, content);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", comment);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long postId) {
        List<SocialComment> comments = communityService.getComments(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", comments);
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

    // --- 群组成员管理 ---
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

    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<Map<String, Object>> getGroupMembers(@PathVariable Long groupId) {
        var members = communityService.getGroupMembers(groupId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", members);
        return ResponseEntity.ok(response);
    }

    // --- 好友关系维护 ---
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

    // --- 媒体文件上传 ---
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMedia(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "文件为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成带有原始后缀的唯一文件名
            String newFilename = java.util.UUID.randomUUID().toString() + extension;

            // 上传至文件服务器
            String fileServerUrl = "https://files.homesee.xyz/api/files/upload";

            // 构建多部分表单请求 (Multipart Request)
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

            org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            org.springframework.http.HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(
                    body, headers);

            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            ResponseEntity<Map> fileServerResponse = restTemplate.postForEntity(fileServerUrl, requestEntity,
                    Map.class);

            if (fileServerResponse.getStatusCode().is2xxSuccessful() && fileServerResponse.getBody() != null) {
                Map<String, Object> fileServerBody = fileServerResponse.getBody();
                System.out.println("文件服务器响应: " + fileServerBody);

                if (Boolean.TRUE.equals(fileServerBody.get("success"))) {
                    String fileUrl = (String) fileServerBody.get("fileUrl");

                    // 检查fileUrl是否为null
                    if (fileUrl == null) {
                        response.put("success", false);
                        response.put("message", "文件服务器返回的fileUrl为空");
                        return ResponseEntity.status(500).body(response);
                    }

                    // 从 URL 中提取文件名（最后一个斜杠后的部分）
                    String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

                    // 输出日志信息
                    System.out.println("=== 文件上传日志 ===");
                    System.out.println("用户上传的文件名称: " + originalFilename);
                    System.out.println("数据库存储的文件名称: " + fileName);
                    System.out.println("从服务器获取的完整路径: " + fileUrl);
                    System.out.println("=== 日志结束 ===");

                    response.put("success", true);
                    response.put("filename", fileName); // 只返回文件名
                    response.put("message", "文件上传成功");
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "文件服务器上传失败: " + fileServerBody.get("message"));
                    return ResponseEntity.status(500).body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "文件服务器响应异常");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
