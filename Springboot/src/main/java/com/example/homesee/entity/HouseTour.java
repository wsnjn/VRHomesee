package com.example.homesee.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "house_tours")
public class HouseTour {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "vr_scene_url")
    private String vrSceneUrl;
    
    // 构造函数
    public HouseTour() {}
    
    public HouseTour(String title, String description, String imageUrl, String vrSceneUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.vrSceneUrl = vrSceneUrl;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getVrSceneUrl() {
        return vrSceneUrl;
    }
    
    public void setVrSceneUrl(String vrSceneUrl) {
        this.vrSceneUrl = vrSceneUrl;
    }
    
    @Override
    public String toString() {
        return "HouseTour{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", vrSceneUrl='" + vrSceneUrl + '\'' +
                '}';
    }
}
