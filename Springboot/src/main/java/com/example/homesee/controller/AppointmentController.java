/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：AppointmentController.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：基础预约管理控制器，处理用户侧的房源带看与合同面谈预约创建、详情查询及状态控制逻辑
 * 创建日期：2026-01-06
 */
package com.example.homesee.controller;

import com.example.homesee.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/appointment")
@CrossOrigin(origins = {
        "http://39.108.142.250:9999",
        "http://localhost:5173",
        "https://www.homesee.xyz",
        "https://homesee.xyz"
})

public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 创建预约
     * 
     * @param request 预约请求数据
     * @return 创建结果
     */
    @PostMapping("/create")
    public Map<String, Object> createAppointment(@RequestBody Map<String, Object> request) {
        try {
            Long houseId = Long.valueOf(request.get("houseId").toString());
            Long userId = request.containsKey("userId") ? Long.valueOf(request.get("userId").toString()) : null;
            String contactName = (String) request.get("contactName");
            String contactPhone = (String) request.get("contactPhone");
            String appointmentDateTimeStr = (String) request.get("appointmentDateTime");
            Integer appointmentType = Integer.valueOf(request.get("appointmentType").toString());
            String remarks = (String) request.get("remarks");

            // 解析日期时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime appointmentDateTime = LocalDateTime.parse(appointmentDateTimeStr, formatter);

            return appointmentService.createAppointment(houseId, userId, contactName, contactPhone,
                    appointmentDateTime, appointmentType, remarks);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 获取预约详情
     * 
     * @param appointmentId 预约ID
     * @return 预约详情
     */
    @GetMapping("/{appointmentId}")
    public Map<String, Object> getAppointment(@PathVariable Long appointmentId) {
        return appointmentService.getAppointmentById(appointmentId);
    }

    /**
     * 根据用户ID获取预约列表
     * 
     * @param userId 用户ID
     * @return 预约列表
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getAppointmentsByUser(@PathVariable Long userId) {
        return appointmentService.getAppointmentsByUserId(userId);
    }

    /**
     * 更新预约状态
     * 
     * @param appointmentId 预约ID
     * @param request       状态更新请求
     * @return 更新结果
     */
    @PutMapping("/{appointmentId}/status")
    public Map<String, Object> updateAppointmentStatus(@PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer status = Integer.valueOf(request.get("status").toString());
            return appointmentService.updateAppointmentStatus(appointmentId, status);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 取消预约
     * 
     * @param appointmentId 预约ID
     * @return 取消结果
     */
    @PutMapping("/{appointmentId}/cancel")
    public Map<String, Object> cancelAppointment(@PathVariable Long appointmentId) {
        return appointmentService.cancelAppointment(appointmentId);
    }

    /**
     * 确认预约
     * 
     * @param appointmentId 预约ID
     * @return 确认结果
     */
    @PutMapping("/{appointmentId}/confirm")
    public Map<String, Object> confirmAppointment(@PathVariable Long appointmentId) {
        return appointmentService.confirmAppointment(appointmentId);
    }
}
