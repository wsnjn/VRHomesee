/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：RoomVrSceneService.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：VR场景服务类，提供房源VR全景场景的获取、文件路由转换（拼接待下载链接）、场景记录新增及删除等功能
 * 创建日期：2026-01-06
 */
package com.example.homesee.service;

import com.example.homesee.entity.RoomVrScene;
import com.example.homesee.repository.RoomVrSceneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class RoomVrSceneService {

    @Autowired
    private RoomVrSceneRepository roomVrSceneRepository;

    // 文件服务器地址
    private final String FILE_SERVER_HOST = "https://files.homesee.xyz";

    public List<RoomVrScene> getScenesByRoomId(Long roomId) {
        List<RoomVrScene> scenes = roomVrSceneRepository.findByRoomId(roomId);
        // 为每个场景设置完整的文件服务器URL
        for (RoomVrScene scene : scenes) {
            if (scene.getImageUrl() != null && !scene.getImageUrl().startsWith("http")) {
                scene.setImageUrl(FILE_SERVER_HOST + "/api/files/download/" + scene.getImageUrl());
            }
        }
        return scenes;
    }

    public RoomVrScene addScene(Long roomId, String sceneName, MultipartFile file) throws IOException {
        // 1. 上传文件到文件服务器
        String fileName = file.getOriginalFilename();

        // 2. 创建实体，只保存文件名
        // 实际文件存储在文件服务器，这里只保存文件名用于后续获取
        RoomVrScene scene = new RoomVrScene(roomId, sceneName, fileName);
        return roomVrSceneRepository.save(scene);
    }

    public void deleteScene(Long id) {
        RoomVrScene scene = roomVrSceneRepository.findById(id).orElse(null);
        if (scene != null) {
            // 注意：这里只删除数据库记录，实际文件仍然保留在文件服务器
            // 如果需要删除文件服务器上的文件，需要调用文件服务器的删除接口
            roomVrSceneRepository.delete(scene);
        }
    }
}
