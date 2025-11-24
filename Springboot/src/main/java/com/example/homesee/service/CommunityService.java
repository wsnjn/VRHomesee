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
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(m -> chatGroupRepository.findById(m.getGroupId()).orElse(null))
                .filter(g -> g != null)
                .toList();
    }

    public List<ChatGroup> getGlobalGroups() {
        return chatGroupRepository.findByGroupType(1); // 1-Global
    }

    @SuppressWarnings("null")
    public ChatGroup createGroup(ChatGroup group) {
        return chatGroupRepository.save(group);
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
