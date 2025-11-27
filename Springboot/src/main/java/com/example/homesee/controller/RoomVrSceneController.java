package com.example.homesee.controller;

import com.example.homesee.entity.RoomVrScene;
import com.example.homesee.service.RoomVrSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vr-scenes")
@CrossOrigin(origins = "http://39.108.142.250:9999,http://localhost:5173", allowCredentials = "true")
public class RoomVrSceneController {

    @Autowired
    private RoomVrSceneService roomVrSceneService;

    // 文件服务器地址
    private final String FILE_SERVER_URL = "http://39.108.142.250:8088/api/files/upload";

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
            // 1. 先上传文件到文件服务器
            RestTemplate restTemplate = new RestTemplate();

            // 准备文件上传请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送文件到文件服务器
            ResponseEntity<String> fileServerResponse = restTemplate.exchange(
                    FILE_SERVER_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            // 2. 如果文件上传成功，保存场景信息到数据库
            if (fileServerResponse.getStatusCode().is2xxSuccessful()) {
                RoomVrScene scene = roomVrSceneService.addScene(roomId, sceneName, file);
                response.put("success", true);
                response.put("data", scene);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文件服务器上传失败");
                return ResponseEntity.badRequest().body(response);
            }
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
