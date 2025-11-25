package com.example.homesee.service;

import com.example.homesee.entity.*;
import com.example.homesee.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityService {

    @Autowired
    private ChatGroupRepository chatGroupRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SocialPostRepository socialPostRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    // --- Chat Group Logic ---
    public List<ChatGroup> getAllGroups() {
        return chatGroupRepository.findAll();
    }

    public List<ChatGroup> getUserGroups(Long userId) {
        // 获取用户已加入的群组
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        List<ChatGroup> userGroups = memberships.stream()
                .map(m -> chatGroupRepository.findById(m.getGroupId()).orElse(null))
                .filter(g -> g != null)
                .collect(java.util.stream.Collectors.toList());

        // 获取用户创建的群组（通过 ownerId）
        List<ChatGroup> ownedGroups = chatGroupRepository.findByOwnerId(userId);

        // 获取所有全局群组（type=1）和租客群组（type=2）
        List<ChatGroup> globalGroups = chatGroupRepository.findByGroupType(1);
        List<ChatGroup> tenantGroups = chatGroupRepository.findByGroupType(2);

        // 合并结果，去重
        userGroups.addAll(ownedGroups);
        userGroups.addAll(globalGroups);
        userGroups.addAll(tenantGroups);

        // 去重（基于ID）
        return userGroups.stream()
                .distinct()
                .toList();
    }

    public List<ChatGroup> getGlobalGroups() {
        return chatGroupRepository.findByGroupType(1); // 1-Global
    }

    @SuppressWarnings("null")
    public ChatGroup createGroup(ChatGroup group) {
        // Save the group first
        ChatGroup savedGroup = chatGroupRepository.save(group);
        // If ownerId is provided, automatically add the owner as a member with role 2
        // (owner)
        if (savedGroup.getOwnerId() != null) {
            GroupMember ownerMember = new GroupMember();
            ownerMember.setGroupId(savedGroup.getId());
            ownerMember.setUserId(savedGroup.getOwnerId());
            ownerMember.setRole(2); // Owner role
            groupMemberRepository.save(ownerMember);
        }
        return savedGroup;
    }

    // --- Group Membership Logic ---
    @SuppressWarnings("null")
    public GroupMember joinGroup(Long groupId, Long userId, Integer role) {
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            return groupMemberRepository.findByGroupIdAndUserId(groupId, userId).orElse(null);
        }
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(role != null ? role : 0);
        return groupMemberRepository.save(member);
    }

    public List<GroupMember> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    // --- Chat Message Logic ---
    @SuppressWarnings("null")
    public ChatMessage sendMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getGroupMessages(Long groupId) {
        return chatMessageRepository.findByGroupIdOrderByCreatedTimeAsc(groupId);
    }

    // --- Friendship Logic ---
    @SuppressWarnings("null")
    public Friendship sendFriendRequest(Long userId, Long friendId) {
        // Check if already exists
        var existing = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Friendship friendship = new Friendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);
        friendship.setStatus(0); // Pending
        return friendshipRepository.save(friendship);
    }

    @SuppressWarnings("null")
    public Friendship respondToFriendRequest(Long requestId, Integer status) {
        var friendship = friendshipRepository.findById(requestId).orElse(null);
        if (friendship != null) {
            friendship.setStatus(status);
            return friendshipRepository.save(friendship);
        }
        return null;
    }

    public List<Friendship> getFriends(Long userId) {
        return friendshipRepository.findAcceptedFriendships(userId);
    }

    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findByFriendIdAndStatus(userId, 0);
    }

    // --- Social Feed Logic ---
    @SuppressWarnings("null")
    public SocialPost createPost(SocialPost post) {
        return socialPostRepository.save(post);
    }

    public List<SocialPost> getAllPosts() {
        return socialPostRepository.findAll(Sort.by(Sort.Direction.DESC, "createdTime"));
    }
}
