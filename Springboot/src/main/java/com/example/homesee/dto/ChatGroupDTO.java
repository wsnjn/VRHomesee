package com.example.homesee.dto;

import java.time.LocalDateTime;

public class ChatGroupDTO {
    private Long id;
    private String groupName;
    private Integer groupType; // 0-Normal, 1-Global, 2-Tenant, 3-Private
    private Long ownerId;
    private String announcement;
    private LocalDateTime createdTime;

    // Display fields for frontend
    private String displayName;
    private String displayAvatar;
    private Long targetUserId; // For private chat, the other user's ID

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getGroupType() {
        return groupType;
    }

    public void setGroupType(Integer groupType) {
        this.groupType = groupType;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayAvatar() {
        return displayAvatar;
    }

    public void setDisplayAvatar(String displayAvatar) {
        this.displayAvatar = displayAvatar;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }
}
