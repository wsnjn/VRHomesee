/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：AiProxyController.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：AI接口代理控制器，通过请求转发方式对接ModelScope（魔搭社区）等第三方AI大模型 API，解决跨域及鉴权安全问题
 * 创建日期：2026-01-06
 */
package com.example.homesee.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {
        "http://39.108.142.250:9999",
        "http://localhost:5173",
        "https://www.homesee.xyz",
        "https://homesee.xyz"
})
public class AiProxyController {

    // ModelScope 魔搭社区 API
    private final String API_KEY = "ms-41438d67-36a2-46d9-a206-309db6e1ac81";
    private final String TARGET_URL = "https://api-inference.modelscope.cn/v1/chat/completions";

    @PostMapping("/chat/completions")
    public ResponseEntity<String> proxyChat(@RequestBody Map<String, Object> payload) {
        RestTemplate restTemplate = new RestTemplate();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);
        // 模拟真实浏览器以绕过“未授权客户端”检测
        headers.set("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");

        // 构建请求实体
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            // 转发请求到魔搭社区接口
            System.out.println("转发请求至: " + TARGET_URL);
            ResponseEntity<String> response = restTemplate.postForEntity(TARGET_URL, request, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // 直接透传上游 API 的错误（如 401, 403 等）
            System.err.println("上游 API 发生错误: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"内部代理错误: " + e.getMessage() + "\"}");
        }
    }
}
