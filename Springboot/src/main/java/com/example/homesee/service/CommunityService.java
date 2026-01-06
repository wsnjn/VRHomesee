/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：CommunityService.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：社区服务类，提供聊天群组管理、社交圈动态、好友关系维护、点赞评论以及私聊会话建立等综合社交功能逻辑实现
 * 创建日期：2026-01-06
 */
package com.example.homesee.service;

import com.example.homesee.entity.*;
import com.example.homesee.repository.*;
import com.example.homesee.dto.SocialPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import com.example.homesee.dto.FriendDTO;
import com.example.homesee.dto.ChatGroupDTO;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocialLikeRepository socialLikeRepository;

    @Autowired
    private SocialCommentRepository socialCommentRepository;

    // --- Chat Group Logic ---
    public List<ChatGroup> getAllGroups() {
        return chatGroupRepository.findAll();
    }

    public List<ChatGroupDTO> getUserGroups(Long userId) {
        // 获取用户已加入的群组
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        List<ChatGroup> userGroups = memberships.stream()
                .map(m -> chatGroupRepository.findById(m.getGroupId()).orElse(null))
                .filter(g -> g != null)
                .collect(Collectors.toList());

        // 获取用户创建的群组（通过 ownerId）
        List<ChatGroup> ownedGroups = chatGroupRepository.findByOwnerId(userId);

        // 获取所有全局群组（type=1）和租客群组（type=2）
        List<ChatGroup> globalGroups = chatGroupRepository.findByGroupType(1);
        List<ChatGroup> tenantGroups = chatGroupRepository.findByGroupType(2);

        // 合并结果，去重
        List<ChatGroup> allGroups = new ArrayList<>(userGroups);
        allGroups.addAll(ownedGroups);
        allGroups.addAll(globalGroups);
        allGroups.addAll(tenantGroups);

        // 去重（基于ID）
        List<ChatGroup> distinctGroups = allGroups.stream()
                .distinct()
                .collect(Collectors.toList());

        // Convert to DTO
        return distinctGroups.stream().map(group -> {
            ChatGroupDTO dto = new ChatGroupDTO();
            dto.setId(group.getId());
            dto.setGroupName(group.getGroupName());
            dto.setGroupType(group.getGroupType());
            dto.setOwnerId(group.getOwnerId());
            dto.setAnnouncement(group.getAnnouncement());
            dto.setCreatedTime(group.getCreatedTime());

            // Default display info
            dto.setDisplayName(group.getGroupName());
            dto.setDisplayAvatar(null); // Default group avatar logic in frontend

            // Handle Private Chat (Type 3)
            if (group.getGroupType() == 3) {
                // Find the other member
                List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
                Optional<GroupMember> otherMember = members.stream()
                        .filter(m -> !m.getUserId().equals(userId))
                        .findFirst();

                if (otherMember.isPresent()) {
                    userRepository.findById(otherMember.get().getUserId()).ifPresent(user -> {
                        dto.setDisplayName(user.getUsername()); // Or realName
                        dto.setDisplayAvatar(user.getAvatar());
                        dto.setTargetUserId(user.getId());
                    });
                } else {
                    // Fallback if other member not found (shouldn't happen in valid private chat)
                    dto.setDisplayName("Unknown User");
                }
            }

            return dto;
        }).collect(Collectors.toList());
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

    public List<FriendDTO> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);
        return friendships.stream().map(f -> {
            FriendDTO dto = new FriendDTO();
            dto.setId(f.getId());

            // Determine who is the friend
            Long targetId = f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId();
            dto.setFriendId(targetId);

            dto.setStatus(f.getStatus());
            dto.setCreatedTime(f.getCreatedTime());

            userRepository.findById(targetId).ifPresent(user -> {
                dto.setUsername(user.getUsername());
                dto.setAvatar(user.getAvatar());
                dto.setRealName(user.getRealName());
            });
            return dto;
        }).collect(Collectors.toList());
    }

    public ChatGroup getPrivateChatGroup(Long userId, Long friendId) {
        long minId = Math.min(userId, friendId);
        long maxId = Math.max(userId, friendId);
        String uniqueName = "PRIVATE_" + minId + "_" + maxId;

        Optional<ChatGroup> existing = chatGroupRepository.findByGroupName(uniqueName);
        if (existing.isPresent()) {
            return existing.get();
        }

        ChatGroup group = new ChatGroup();
        group.setGroupName(uniqueName);
        group.setGroupType(3); // 3 = Private
        group.setOwnerId(userId);
        group.setCreatedTime(LocalDateTime.now());

        ChatGroup saved = chatGroupRepository.save(group);

        joinGroup(saved.getId(), userId, 0);
        joinGroup(saved.getId(), friendId, 0);

        return saved;
    }

    public void deleteGroup(Long groupId, Long ownerId) {
        Optional<ChatGroup> groupOpt = chatGroupRepository.findById(groupId);
        if (groupOpt.isPresent()) {
            ChatGroup group = groupOpt.get();
            // Check if user is owner and group is not system group (1 or 2)
            // Note: Global groups usually have ID 1 or 2, or type 1/2.
            // The requirement says "except ID 1 and 2".
            if (group.getId() == 1 || group.getId() == 2) {
                throw new RuntimeException("Cannot delete system groups");
            }

            if (!group.getOwnerId().equals(ownerId)) {
                throw new RuntimeException("Only owner can delete group");
            }

            // Delete members and messages first?
            // JPA might handle cascade if configured, but let's be safe or just delete
            // group.
            // Assuming cascade or simple delete for now.
            // Better to delete members and messages.
            List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
            groupMemberRepository.deleteAll(members);

            List<ChatMessage> messages = chatMessageRepository.findByGroupIdOrderByCreatedTimeAsc(groupId);
            chatMessageRepository.deleteAll(messages);

            chatGroupRepository.delete(group);
        } else {
            throw new RuntimeException("Group not found");
        }
    }

    public void inviteToGroup(Long groupId, Long inviterId, Long inviteeId) {
        Optional<ChatGroup> groupOpt = chatGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Group not found");
        }

        ChatGroup group = groupOpt.get();
        if (group.getId() == 1 || group.getId() == 2) {
            throw new RuntimeException("Cannot invite to system groups via this method");
        }

        // Check if inviter is in group OR is the owner
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, inviterId);
        boolean isOwner = group.getOwnerId() != null && group.getOwnerId().equals(inviterId);

        if (!isMember) {
            if (isOwner) {
                // Auto-repair: Add owner to group
                joinGroup(groupId, inviterId, 2); // Role 2 for owner
            } else {
                throw new RuntimeException("You are not a member of this group");
            }
        }

        // Check if invitee is already in group
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, inviteeId)) {
            // Idempotent: just return, consider it a success
            return;
        }

        joinGroup(groupId, inviteeId, 0);
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

    public List<SocialPostDTO> getAllPostsWithUserInfo(Long currentUserId) {
        List<SocialPost> posts = socialPostRepository.findAll(Sort.by(Sort.Direction.DESC, "createdTime"));
        return posts.stream().map(post -> {
            SocialPostDTO dto = new SocialPostDTO();
            dto.setId(post.getId());
            dto.setUserId(post.getUserId());
            dto.setContent(post.getContent());
            dto.setMediaUrls(post.getMediaUrls());
            dto.setVisibility(post.getVisibility());
            dto.setCreatedTime(post.getCreatedTime());

            // 获取用户信息
            userRepository.findById(post.getUserId()).ifPresent(user -> {
                dto.setUsername(user.getUsername());
                dto.setAvatar(user.getAvatar());
            });

            // 获取互动数据
            dto.setLikeCount(socialLikeRepository.countByPostId(post.getId()));
            dto.setCommentCount(socialCommentRepository.countByPostId(post.getId()));

            if (currentUserId != null) {
                dto.setLiked(socialLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId));
            } else {
                dto.setLiked(false);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // --- Like Logic ---
    public boolean toggleLike(Long postId, Long userId) {
        Optional<SocialLike> existingLike = socialLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existingLike.isPresent()) {
            socialLikeRepository.delete(existingLike.get());
            return false; // Unliked
        } else {
            SocialLike like = new SocialLike();
            like.setPostId(postId);
            like.setUserId(userId);
            socialLikeRepository.save(like);
            return true; // Liked
        }
    }

    // --- Comment Logic ---
    public SocialComment addComment(Long postId, Long userId, String content) {
        SocialComment comment = new SocialComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        return socialCommentRepository.save(comment);
    }

    public List<SocialComment> getComments(Long postId) {
        return socialCommentRepository.findByPostIdOrderByCreatedTimeAsc(postId);
    }

    // --- Missing methods for CommunityController ---

    @SuppressWarnings("null")
    public void deletePost(Long postId, Long userId) {
        Optional<SocialPost> postOpt = socialPostRepository.findById(postId);
        if (postOpt.isPresent()) {
            SocialPost post = postOpt.get();
            // Check if user is the owner of the post
            if (!post.getUserId().equals(userId)) {
                throw new RuntimeException("Only post owner can delete the post");
            }
            socialPostRepository.delete(post);
        } else {
            throw new RuntimeException("Post not found");
        }
    }

    @SuppressWarnings("null")
    public SocialPost updatePostVisibility(Long postId, Integer visibility, Long userId) {
        Optional<SocialPost> postOpt = socialPostRepository.findById(postId);
        if (postOpt.isPresent()) {
            SocialPost post = postOpt.get();
            // Check if user is the owner of the post
            if (!post.getUserId().equals(userId)) {
                throw new RuntimeException("Only post owner can update visibility");
            }
            post.setVisibility(visibility);
            return socialPostRepository.save(post);
        } else {
            throw new RuntimeException("Post not found");
        }
    }
}
