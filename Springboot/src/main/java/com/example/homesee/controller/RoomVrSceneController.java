package com.example.homesee.controller;

import com.example.homesee.entity.RoomVrScene;
import com.example.homesee.service.RoomVrSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vr-scenes")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class RoomVrSceneController {

    @Autowired
    private RoomVrSceneService roomVrSceneService;

    @GetMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> getScenes(@PathVariable Long roomId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<RoomVrScene> scenes = roomVrSceneService.getScenesByRoomId(roomId);
            response.put("success", true);
            response.put("data", scenes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadScene(
            @RequestParam("roomId") Long roomId,
            @RequestParam("sceneName") String sceneName,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomVrScene scene = roomVrSceneService.addScene(roomId, sceneName, file);
            response.put("success", true);
            response.put("data", scene);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteScene(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            roomVrSceneService.deleteScene(id);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
