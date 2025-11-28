    package com.example.homesee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
@CrossOrigin(origins = "http://39.108.142.250:9999,http://localhost:5173")
public class DatabaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 测试数据库连接
    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return ResponseEntity.ok("数据库连接成功！");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("数据库连接失败: " + e.getMessage());
        }
    }

    // 获取数据库版本
    @GetMapping("/version")
    public ResponseEntity<String> getVersion() {
        try {
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            return ResponseEntity.ok("MySQL版本: " + version);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("获取版本失败: " + e.getMessage());
        }
    }

    // 创建homesee数据库（如果不存在）
    @PostMapping("/create-database")
    public ResponseEntity<String> createDatabase() {
        try {
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS homesee");
            return ResponseEntity.ok("数据库 homesee 创建成功或已存在");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("创建数据库失败: " + e.getMessage());
        }
    }

    // 创建房屋导览表
    @PostMapping("/create-table")
    public ResponseEntity<String> createTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS house_tours (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "image_url VARCHAR(500), " +
                    "vr_scene_url VARCHAR(500), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            jdbcTemplate.execute(sql);
            return ResponseEntity.ok("表 house_tours 创建成功或已存在");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("创建表失败: " + e.getMessage());
        }
    }

    // 插入示例数据
    @PostMapping("/insert-sample-data")
    public ResponseEntity<String> insertSampleData() {
        try {
            String sql = "INSERT IGNORE INTO house_tours (title, description, image_url, vr_scene_url) VALUES " +
                    "('现代简约公寓', '位于市中心的现代化简约风格公寓，采光良好', '/images/apartment1.jpg', '/vr/scene1'), " +
                    "('豪华别墅', '带花园和泳池的豪华别墅，适合家庭居住', '/images/villa1.jpg', '/vr/scene2'), " +
                    "('海景房', '面向大海的海景房，视野开阔', '/images/seaview1.jpg', '/vr/scene3')";
            int rows = jdbcTemplate.update(sql);
            return ResponseEntity.ok("插入 " + rows + " 条示例数据成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("插入示例数据失败: " + e.getMessage());
        }
    }

    // 获取所有房屋导览数据
    @GetMapping("/house-tours")
    public ResponseEntity<List<Map<String, Object>>> getHouseTours() {
        try {
            List<Map<String, Object>> houseTours = jdbcTemplate.queryForList("SELECT * FROM house_tours");
            return ResponseEntity.ok(houseTours);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
