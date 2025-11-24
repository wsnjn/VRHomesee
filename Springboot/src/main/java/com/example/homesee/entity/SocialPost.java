package com.example.homesee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_post")
public class SocialPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_urls", columnDefinition = "TEXT")
    private String mediaUrls; // JSON array string

    @Column(name = "visibility")
    private Integer visibility; // 0-Public, 1-Friends Only

    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(String mediaUrls) {
        this.mediaUrls = mediaUrls;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
