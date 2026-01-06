/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：HouseTour.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：房屋导览实体类，封装房源展示的基础信息（标题、描述）及 VR 全景场景的访问路径
 * 创建日期：2026-01-06
 */
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
    public HouseTour() {
    }

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
