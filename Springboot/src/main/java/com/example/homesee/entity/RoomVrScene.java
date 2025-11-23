package com.example.homesee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_vr_scene")
public class RoomVrScene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "scene_name", nullable = false, length = 50)
    private String sceneName;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    public RoomVrScene() {
    }

    public RoomVrScene(Long roomId, String sceneName, String imageUrl) {
        this.roomId = roomId;
        this.sceneName = sceneName;
        this.imageUrl = imageUrl;
    }

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
