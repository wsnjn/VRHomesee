/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：AiHouseTourController.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：AI虚拟导购控制器，提供VR全景看房场景下的智能语音/文本交互、房源信息自动解读及多模态对话接口
 * 创建日期：2026-01-06
 */
package com.example.homesee.controller;

import com.example.homesee.service.AiAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/house-tour")
@CrossOrigin(origins = {
        "http://39.108.142.250:9999",
        "http://localhost:5173",
        "https://www.homesee.xyz",
        "https://homesee.xyz"
})
public class AiHouseTourController {

    @Autowired
    private AiAgentService aiAgentService;

    /**
     * VR看房AI对话接口
     * 
     * @param request 包含：
     *                - message: 用户消息
     *                - userId: 用户ID (用于存储历史)
     *                - roomInfo: 房源信息 (可选)
     *                - history: 对话历史 (可选)
     *                - screenshot: VR截图Base64 (可选)
     * @return AI回复
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "消息内容不能为空"));
            }

            // 获取用户ID
            Long userId = null;
            try {
                if (request.get("userId") != null) {
                    userId = Long.valueOf(request.get("userId").toString());
                }
            } catch (Exception e) {
                System.err.println("Invalid userId: " + request.get("userId"));
            }

            // 获取房源信息
            @SuppressWarnings("unchecked")
            Map<String, Object> roomInfo = (Map<String, Object>) request.get("roomInfo");

            // 获取对话历史
            @SuppressWarnings("unchecked")
            List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

            // 获取截图
            String screenshot = (String) request.get("screenshot");

            // 调用AI服务 (服务层负责保存历史记录)
            String response = aiAgentService.chat(userId, message, roomInfo, history, screenshot);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "AI服务暂时不可用: " + e.getMessage()));
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "AI House Tour"));
    }
}
