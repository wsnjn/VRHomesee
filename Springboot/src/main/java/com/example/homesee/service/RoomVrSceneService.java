package com.example.homesee.service;

import com.example.homesee.entity.RoomVrScene;
import com.example.homesee.repository.RoomVrSceneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class RoomVrSceneService {

    @Autowired
    private RoomVrSceneRepository roomVrSceneRepository;

    // Hardcoded path as per user requirement
    private final String UPLOAD_DIR = "e:/wslop/End/Vue3/frontend/src/assets/image/";

    public List<RoomVrScene> getScenesByRoomId(Long roomId) {
        return roomVrSceneRepository.findByRoomId(roomId);
    }

    public RoomVrScene addScene(Long roomId, String sceneName, MultipartFile file) throws IOException {
        // 1. Create directory if not exists
        String roomDir = UPLOAD_DIR + roomId;
        File directory = new File(roomDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 2. Save file
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(roomDir, fileName);
        Files.write(filePath, file.getBytes());

        // 3. Create entity
        // The URL should be relative to src/assets/image for frontend usage
        // e.g., /src/assets/image/1/abc.jpg
        // But frontend usually imports assets. If we use dynamic paths, we might need
        // to use public folder or handle it differently.
        // However, user asked to store in src/assets/image.
        // In Vue, dynamic images from src/assets can be tricky if not imported.
        // A common workaround is to put them in 'public' or use a specific alias.
        // But let's follow instructions. We will store the path that Vue can try to
        // resolve or serve statically.
        // 返回前端可以直接访问的路径
        // 在Vue开发环境中，图片应该放在public目录下，但用户要求放在src/assets
        // 这里我们返回一个前端可以处理的相对路径
        String relativePath = "/src/assets/image/" + roomId + "/" + fileName;

        RoomVrScene scene = new RoomVrScene(roomId, sceneName, relativePath);
        return roomVrSceneRepository.save(scene);
    }

    public void deleteScene(Long id) {
        RoomVrScene scene = roomVrSceneRepository.findById(id).orElse(null);
        if (scene != null) {
            // Optional: Delete file from disk
            // String filePath = "e:/wslop/End/Vue3/frontend" + scene.getImageUrl();
            // new File(filePath).delete();

            roomVrSceneRepository.delete(scene);
        }
    }
}
