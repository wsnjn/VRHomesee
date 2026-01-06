/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：AdminChannelService.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：频道管理服务类，提供聊天记录获取、消息删除、用户禁言以及社区动态管理等业务逻辑实现
 * 创建日期：2026-01-06
 */
package com.example.homesee.service;

import com.example.homesee.entity.ChatMessage;
import com.example.homesee.entity.SocialPost;
import com.example.homesee.entity.User;
import com.example.homesee.repository.ChatMessageRepository;
import com.example.homesee.repository.SocialPostRepository;
import com.example.homesee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminChannelService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SocialPostRepository socialPostRepository;

    @Autowired
    private UserRepository userRepository;

    // 聊天管理
    public List<Map<String, Object>> getAllChatMessages() {
        List<ChatMessage> messages = chatMessageRepository.findAll();
        return messages.stream().map(msg -> {
            User sender = userRepository.findById(msg.getSenderId()).orElse(null);
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("senderId", msg.getSenderId());
            map.put("senderName",
                    sender != null ? (sender.getRealName() != null ? sender.getRealName() : sender.getUsername())
                            : "Unknown");
            map.put("senderAvatar", sender != null && sender.getAvatar() != null ? sender.getAvatar() : "");
            map.put("content", msg.getContent());
            map.put("msgType", msg.getMsgType());
            map.put("groupId", msg.getGroupId() != null ? msg.getGroupId() : -1);
            map.put("receiverId", msg.getReceiverId() != null ? msg.getReceiverId() : -1);
            map.put("createdTime", msg.getCreatedTime());
            return map;
        }).collect(Collectors.toList());
    }

    public void deleteChatMessage(Long id) {
        chatMessageRepository.deleteById(id);
    }

    @Transactional
    public void muteUser(Long userId, Integer durationMinutes) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setChatMutedUntil(LocalDateTime.now().plusMinutes(durationMinutes));
        userRepository.save(user);
    }

    // 社区管理
    public List<Map<String, Object>> getAllSocialPosts() {
        List<SocialPost> posts = socialPostRepository.findAll();
        return posts.stream().map(post -> {
            User user = userRepository.findById(post.getUserId()).orElse(null);
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("userId", post.getUserId());
            map.put("userName",
                    user != null ? (user.getRealName() != null ? user.getRealName() : user.getUsername()) : "Unknown");
            map.put("userAvatar", user != null && user.getAvatar() != null ? user.getAvatar() : "");
            map.put("content", post.getContent() != null ? post.getContent() : "");
            map.put("mediaUrls", post.getMediaUrls() != null ? post.getMediaUrls() : "[]");
            map.put("visibility", post.getVisibility());
            map.put("createdTime", post.getCreatedTime());
            return map;
        }).collect(Collectors.toList());
    }

    public void deleteSocialPost(Long id) {
        socialPostRepository.deleteById(id);
    }

    @Transactional
    public void banUserFromPosting(Long userId, Integer durationMinutes) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setSocialBannedUntil(LocalDateTime.now().plusMinutes(durationMinutes));
        userRepository.save(user);
    }
}
