package com.example.homesee.service;

import com.example.homesee.entity.Appointment;
import com.example.homesee.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * 创建预约
     * @param houseId 房屋ID
     * @param userId 用户ID
     * @param contactName 联系人姓名
     * @param contactPhone 联系电话
     * @param appointmentDateTime 预约时间
     * @param appointmentType 预约类型
     * @param remarks 备注信息
     * @return 创建结果
     */
    public Map<String, Object> createAppointment(Long houseId, Long userId, String contactName, 
                                                String contactPhone, LocalDateTime appointmentDateTime, 
                                                Integer appointmentType, String remarks) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查时间冲突（同一房屋在同一时间段的预约）
            LocalDateTime startTime = appointmentDateTime.minusHours(1);
            LocalDateTime endTime = appointmentDateTime.plusHours(1);
            
            List<Appointment> conflictingAppointments = appointmentRepository.findConflictingAppointments(houseId, startTime, endTime);
            if (!conflictingAppointments.isEmpty()) {
                result.put("success", false);
                result.put("message", "该时间段已有预约，请选择其他时间");
                return result;
            }

            // 创建预约记录
            Appointment appointment = new Appointment(houseId, userId, contactName, contactPhone, 
                                                     appointmentDateTime, appointmentType, remarks);
            
            Appointment savedAppointment = appointmentRepository.save(appointment);
            
            result.put("success", true);
            result.put("message", "预约提交成功");
            result.put("appointmentId", savedAppointment.getId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "预约失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 根据ID获取预约详情
     * @param appointmentId 预约ID
     * @return 预约详情
     */
    public Map<String, Object> getAppointmentById(Long appointmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentId);
            if (appointmentOptional.isPresent()) {
                Appointment appointment = appointmentOptional.get();
                
                Map<String, Object> appointmentInfo = new HashMap<>();
                appointmentInfo.put("id", appointment.getId());
                appointmentInfo.put("houseId", appointment.getHouseId());
                appointmentInfo.put("userId", appointment.getUserId());
                appointmentInfo.put("contactName", appointment.getContactName());
                appointmentInfo.put("contactPhone", appointment.getContactPhone());
                appointmentInfo.put("appointmentDateTime", appointment.getAppointmentDateTime());
                appointmentInfo.put("appointmentType", appointment.getAppointmentType());
                appointmentInfo.put("remarks", appointment.getRemarks());
                appointmentInfo.put("status", appointment.getStatus());
                appointmentInfo.put("createdTime", appointment.getCreatedTime());
                appointmentInfo.put("updatedTime", appointment.getUpdatedTime());
                
                result.put("success", true);
                result.put("appointment", appointmentInfo);
            } else {
                result.put("success", false);
                result.put("message", "预约记录不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取预约详情失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 根据用户ID获取预约列表
     * @param userId 用户ID
     * @return 预约列表
     */
    public Map<String, Object> getAppointmentsByUserId(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var appointments = appointmentRepository.findByUserId(userId);
            result.put("success", true);
            result.put("appointments", appointments);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取预约列表失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新预约状态
     * @param appointmentId 预约ID
     * @param status 新状态
     * @return 更新结果
     */
    public Map<String, Object> updateAppointmentStatus(Long appointmentId, Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentId);
            if (appointmentOptional.isPresent()) {
                Appointment appointment = appointmentOptional.get();
                appointment.setStatus(status);
                appointment.setUpdatedTime(LocalDateTime.now());
                
                appointmentRepository.save(appointment);
                
                result.put("success", true);
                result.put("message", "预约状态更新成功");
            } else {
                result.put("success", false);
                result.put("message", "预约记录不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新预约状态失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 取消预约
     * @param appointmentId 预约ID
     * @return 取消结果
     */
    public Map<String, Object> cancelAppointment(Long appointmentId) {
        return updateAppointmentStatus(appointmentId, 3); // 3: 已取消
    }

    /**
     * 确认预约
     * @param appointmentId 预约ID
     * @return 确认结果
     */
    public Map<String, Object> confirmAppointment(Long appointmentId) {
        return updateAppointmentStatus(appointmentId, 1); // 1: 已确认
    }
}
