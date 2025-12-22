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

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);
        // Mimic a real browser to bypass "unauthorized client detected"
        headers.set("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");

        // Request entity
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            // Send request to Agent Router
            System.out.println("Proxying request to: " + TARGET_URL);
            ResponseEntity<String> response = restTemplate.postForEntity(TARGET_URL, request, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // Pass through the upstream error (401, 403, etc.)
            System.err.println("Upstream API Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"Internal Proxy Error: " + e.getMessage() + "\"}");
        }
    }
}
