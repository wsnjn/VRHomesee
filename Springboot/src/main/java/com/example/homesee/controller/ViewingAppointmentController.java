package com.example.homesee.controller;

import com.example.homesee.service.ViewingAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/viewing-appointment")
@CrossOrigin(origins = "http://39.108.142.250:9999,http://localhost:5173")
public class ViewingAppointmentController {

    @Autowired
    private ViewingAppointmentService viewingAppointmentService;

    /**
     * 创建预约看房
     * 
     * @param request 预约请求数据
     * @return 创建结果
     */
    @PostMapping("/create")
    public Map<String, Object> createViewingAppointment(@RequestBody Map<String, Object> request) {
        try {
            Long roomId = Long.valueOf(request.get("roomId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            String preferredDateStr = (String) request.get("preferredDate");
            String preferredTimeSlot = (String) request.get("preferredTimeSlot");
            Integer appointmentType = Integer.valueOf(request.get("appointmentType").toString());
            String contactName = (String) request.get("contactName");
            String contactPhone = (String) request.get("contactPhone");

            // 解析日期
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate preferredDate = LocalDate.parse(preferredDateStr, formatter);

            // 可选字段
            String wechatId = (String) request.get("wechatId");
            Integer tenantCount = request.containsKey("tenantCount")
                    ? Integer.valueOf(request.get("tenantCount").toString())
                    : null;
            LocalDate expectedMoveInDate = null;
            if (request.containsKey("expectedMoveInDate")) {
                String expectedMoveInDateStr = (String) request.get("expectedMoveInDate");
                expectedMoveInDate = LocalDate.parse(expectedMoveInDateStr, formatter);
            }
            String rentalIntention = (String) request.get("rentalIntention");

            return viewingAppointmentService.createViewingAppointment(
                    roomId, userId, preferredDate, preferredTimeSlot, appointmentType,
                    contactName, contactPhone, wechatId, tenantCount, expectedMoveInDate, rentalIntention);
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
        return viewingAppointmentService.getAppointmentById(appointmentId);
    }

    /**
     * 根据预约编号获取预约详情
     * 
     * @param appointmentNumber 预约编号
     * @return 预约详情
     */
    @GetMapping("/number/{appointmentNumber}")
    public Map<String, Object> getAppointmentByNumber(@PathVariable String appointmentNumber) {
        return viewingAppointmentService.getAppointmentByNumber(appointmentNumber);
    }

    /**
     * 根据用户ID获取预约列表
     * 
     * @param userId 用户ID
     * @return 预约列表
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getAppointmentsByUser(@PathVariable Long userId) {
        return viewingAppointmentService.getAppointmentsByUserId(userId);
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
            String cancellationReason = (String) request.get("cancellationReason");
            return viewingAppointmentService.updateAppointmentStatus(appointmentId, status, cancellationReason);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 确认预约
     * 
     * @param appointmentId 预约ID
     * @return 确认结果
     */
    @PutMapping("/{appointmentId}/confirm")
    public Map<String, Object> confirmAppointment(@PathVariable Long appointmentId) {
        return viewingAppointmentService.updateAppointmentStatus(appointmentId, 1, null);
    }

    /**
     * 取消预约
     * 
     * @param appointmentId 预约ID
     * @param request       取消请求
     * @return 取消结果
     */
    @PutMapping("/{appointmentId}/cancel")
    public Map<String, Object> cancelAppointment(@PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request) {
        String cancellationReason = (String) request.get("cancellationReason");
        return viewingAppointmentService.updateAppointmentStatus(appointmentId, 3, cancellationReason);
    }

    /**
     * 完成预约
     * 
     * @param appointmentId 预约ID
     * @return 完成结果
     */
    @PutMapping("/{appointmentId}/complete")
    public Map<String, Object> completeAppointment(@PathVariable Long appointmentId) {
        return viewingAppointmentService.updateAppointmentStatus(appointmentId, 2, null);
    }

    /**
     * 分配业务员
     * 
     * @param appointmentId 预约ID
     * @param request       分配请求
     * @return 分配结果
     */
    @PutMapping("/{appointmentId}/assign-agent")
    public Map<String, Object> assignAgent(@PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request) {
        try {
            Long agentId = Long.valueOf(request.get("agentId").toString());
            return viewingAppointmentService.assignAgent(appointmentId, agentId);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 添加业务员备注
     * 
     * @param appointmentId 预约ID
     * @param request       备注请求
     * @return 添加结果
     */
    @PutMapping("/{appointmentId}/agent-notes")
    public Map<String, Object> addAgentNotes(@PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request) {
        try {
            String agentNotes = (String) request.get("agentNotes");
            return viewingAppointmentService.addAgentNotes(appointmentId, agentNotes);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 设置跟进信息
     * 
     * @param appointmentId 预约ID
     * @param request       跟进信息请求
     * @return 设置结果
     */
    @PutMapping("/{appointmentId}/follow-up")
    public Map<String, Object> setFollowUpInfo(@PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request) {
        try {
            Boolean followUpRequired = Boolean.valueOf(request.get("followUpRequired").toString());
            LocalDate followUpDate = null;
            if (request.containsKey("followUpDate")) {
                String followUpDateStr = (String) request.get("followUpDate");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                followUpDate = LocalDate.parse(followUpDateStr, formatter);
            }
            return viewingAppointmentService.setFollowUpInfo(appointmentId, followUpRequired, followUpDate);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 获取所有预约列表（管理端使用）
     * 
     * @return 预约列表
     */
    @GetMapping("/all")
    public Map<String, Object> getAllAppointments() {
        return viewingAppointmentService.getAllAppointments();
    }

    /**
     * 获取需要跟进的预约列表
     * 
     * @return 跟进预约列表
     */
    @GetMapping("/follow-up")
    public Map<String, Object> getFollowUpAppointments() {
        return viewingAppointmentService.getFollowUpAppointments();
    }

    /**
     * 根据房东手机号获取预约列表
     * 
     * @param landlordPhone 房东手机号
     * @return 预约列表
     */
    @GetMapping("/landlord/{landlordPhone}")
    public Map<String, Object> getAppointmentsByLandlord(@PathVariable String landlordPhone) {
        return viewingAppointmentService.getAppointmentsByLandlordPhone(landlordPhone);
    }

    /**
     * 根据房东手机号和状态获取预约列表
     * 
     * @param landlordPhone 房东手机号
     * @param status        预约状态
     * @return 预约列表
     */
    @GetMapping("/landlord/{landlordPhone}/status/{status}")
    public Map<String, Object> getAppointmentsByLandlordAndStatus(@PathVariable String landlordPhone,
            @PathVariable Integer status) {
        return viewingAppointmentService.getAppointmentsByLandlordPhoneAndStatus(landlordPhone, status);
    }
}
