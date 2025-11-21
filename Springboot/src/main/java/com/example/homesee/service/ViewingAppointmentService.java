package com.example.homesee.service;

import com.example.homesee.entity.ViewingAppointment;
import com.example.homesee.entity.RoomInfo;
import com.example.homesee.entity.User;
import com.example.homesee.repository.ViewingAppointmentRepository;
import com.example.homesee.repository.RoomInfoRepository;
import com.example.homesee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ViewingAppointmentService {

    @Autowired
    private ViewingAppointmentRepository viewingAppointmentRepository;
    
    @Autowired
    private RoomInfoRepository roomInfoRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 创建预约看房
     */
    public Map<String, Object> createViewingAppointment(Long roomId, Long userId, LocalDate preferredDate,
                                                       String preferredTimeSlot, Integer appointmentType,
                                                       String contactName, String contactPhone, String wechatId,
                                                       Integer tenantCount, LocalDate expectedMoveInDate,
                                                       String rentalIntention) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查房间是否存在
            Optional<RoomInfo> roomOptional = roomInfoRepository.findById(roomId);
            if (roomOptional.isEmpty()) {
                result.put("success", false);
                result.put("message", "房间信息不存在");
                return result;
            }

            RoomInfo room = roomOptional.get();
            
            // 通过房东手机号获取房东用户信息
            String landlordPhone = room.getLandlordPhone();
            Optional<User> landlordOptional = userRepository.findByPhone(landlordPhone);
            if (landlordOptional.isEmpty()) {
                result.put("success", false);
                result.put("message", "房东用户信息不存在");
                return result;
            }

            User landlord = landlordOptional.get();
            Long landlordId = landlord.getId();

            // 检查时间冲突
            List<ViewingAppointment> conflictingAppointments = viewingAppointmentRepository
                    .findConflictingAppointments(roomId, preferredDate, preferredTimeSlot);
            if (!conflictingAppointments.isEmpty()) {
                result.put("success", false);
                result.put("message", "该时间段已有预约，请选择其他时间");
                return result;
            }

            // 生成预约编号
            String appointmentNumber = generateAppointmentNumber();

            // 创建预约记录
            ViewingAppointment appointment = new ViewingAppointment(appointmentNumber, roomId, userId,
                    preferredDate, preferredTimeSlot, appointmentType, contactName, contactPhone);

            // 设置分配的业务员ID为房东ID
            appointment.setAssignedAgentId(landlordId);

            // 设置可选字段
            if (wechatId != null && !wechatId.isEmpty()) {
                appointment.setWechatId(wechatId);
            }
            if (tenantCount != null) {
                appointment.setTenantCount(tenantCount);
            }
            if (expectedMoveInDate != null) {
                appointment.setExpectedMoveInDate(expectedMoveInDate);
            }
            if (rentalIntention != null && !rentalIntention.isEmpty()) {
                appointment.setRentalIntention(rentalIntention);
            }

            ViewingAppointment savedAppointment = viewingAppointmentRepository.save(appointment);

            result.put("success", true);
            result.put("message", "预约提交成功");
            result.put("appointmentId", savedAppointment.getId());
            result.put("appointmentNumber", savedAppointment.getAppointmentNumber());
            result.put("assignedAgentId", landlordId);
            result.put("landlordInfo", Map.of(
                "id", landlord.getId(),
                "realName", landlord.getRealName(),
                "phone", landlord.getPhone()
            ));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "预约失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据ID获取预约详情
     */
    public Map<String, Object> getAppointmentById(Long appointmentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<ViewingAppointment> appointmentOptional = viewingAppointmentRepository.findById(appointmentId);
            if (appointmentOptional.isPresent()) {
                ViewingAppointment appointment = appointmentOptional.get();
                result.put("success", true);
                result.put("appointment", convertToMap(appointment));
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
     * 根据预约编号获取预约详情
     */
    public Map<String, Object> getAppointmentByNumber(String appointmentNumber) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<ViewingAppointment> appointmentOptional = viewingAppointmentRepository
                    .findByAppointmentNumber(appointmentNumber);
            if (appointmentOptional.isPresent()) {
                ViewingAppointment appointment = appointmentOptional.get();
                result.put("success", true);
                result.put("appointment", convertToMap(appointment));
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
     */
    public Map<String, Object> getAppointmentsByUserId(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ViewingAppointment> appointments = viewingAppointmentRepository.findByUserId(userId);
            List<Map<String, Object>> appointmentList = appointments.stream()
                    .map(this::convertToMap)
                    .toList();

            result.put("success", true);
            result.put("appointments", appointmentList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取预约列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新预约状态
     */
    public Map<String, Object> updateAppointmentStatus(Long appointmentId, Integer status, String cancellationReason) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<ViewingAppointment> appointmentOptional = viewingAppointmentRepository.findById(appointmentId);
            if (appointmentOptional.isPresent()) {
                ViewingAppointment appointment = appointmentOptional.get();
                appointment.setStatus(status);
                appointment.setUpdatedTime(LocalDateTime.now());

                // 设置状态相关的时间戳
                switch (status) {
                    case 1: // 已确认
                        appointment.setConfirmedTime(LocalDateTime.now());
                        break;
                    case 2: // 已完成
                        appointment.setCompletedTime(LocalDateTime.now());
                        break;
                    case 3: // 已取消
                        appointment.setCancelledTime(LocalDateTime.now());
                        appointment.setCancellationReason(cancellationReason);
                        break;
                }

                viewingAppointmentRepository.save(appointment);

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
     * 分配业务员
     */
    public Map<String, Object> assignAgent(Long appointmentId, Long agentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<ViewingAppointment> appointmentOptional = viewingAppointmentRepository.findById(appointmentId);
            if (appointmentOptional.isPresent()) {
                ViewingAppointment appointment = appointmentOptional.get();
                appointment.setAssignedAgentId(agentId);
                appointment.setUpdatedTime(LocalDateTime.now());

                viewingAppointmentRepository.save(appointment);

                result.put("success", true);
                result.put("message", "业务员分配成功");
            } else {
                result.put("success", false);
                result.put("message", "预约记录不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "分配业务员失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 添加业务员备注
     */
    public Map<String, Object> addAgentNotes(Long appointmentId, String agentNotes) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<ViewingAppointment> appointmentOptional = viewingAppointmentRepository.findById(appointmentId);
            if (appointmentOptional.isPresent()) {
                ViewingAppointment appointment = appointmentOptional.get();
                appointment.setAgentNotes(agentNotes);
                appointment.setUpdatedTime(LocalDateTime.now());

                viewingAppointmentRepository.save(appointment);

                result.put("success", true);
                result.put("message", "备注添加成功");
            } else {
                result.put("success", false);
                result.put("message", "预约记录不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加备注失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 设置跟进信息
     */
    public Map<String, Object> setFollowUpInfo(Long appointmentId, Boolean followUpRequired, LocalDate followUpDate) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<ViewingAppointment> appointmentOptional = viewingAppointmentRepository.findById(appointmentId);
            if (appointmentOptional.isPresent()) {
                ViewingAppointment appointment = appointmentOptional.get();
                appointment.setFollowUpRequired(followUpRequired);
                appointment.setFollowUpDate(followUpDate);
                appointment.setUpdatedTime(LocalDateTime.now());

                viewingAppointmentRepository.save(appointment);

                result.put("success", true);
                result.put("message", "跟进信息设置成功");
            } else {
                result.put("success", false);
                result.put("message", "预约记录不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "设置跟进信息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取所有预约列表
     */
    public Map<String, Object> getAllAppointments() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ViewingAppointment> appointments = viewingAppointmentRepository.findRecentAppointments();
            List<Map<String, Object>> appointmentList = appointments.stream()
                    .map(this::convertToMap)
                    .toList();

            result.put("success", true);
            result.put("appointments", appointmentList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取预约列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取需要跟进的预约列表
     */
    public Map<String, Object> getFollowUpAppointments() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ViewingAppointment> appointments = viewingAppointmentRepository.findByFollowUpRequiredTrue();
            List<Map<String, Object>> appointmentList = appointments.stream()
                    .map(this::convertToMap)
                    .toList();

            result.put("success", true);
            result.put("appointments", appointmentList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取跟进预约列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据房东手机号获取预约列表（包含房屋信息）
     */
    public Map<String, Object> getAppointmentsByLandlordPhone(String landlordPhone) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ViewingAppointment> appointments = viewingAppointmentRepository.findByLandlordPhone(landlordPhone);
            List<Map<String, Object>> appointmentList = appointments.stream()
                    .map(appointment -> {
                        Map<String, Object> appointmentMap = convertToMap(appointment);
                        // 添加房屋信息
                        try {
                            Optional<RoomInfo> roomOptional = roomInfoRepository.findById(appointment.getRoomId());
                            if (roomOptional.isPresent()) {
                                RoomInfo room = roomOptional.get();
                                Map<String, Object> roomInfo = convertRoomToMap(room);
                                appointmentMap.put("roomInfo", roomInfo);
                            }
                        } catch (Exception e) {
                            // 如果获取房屋信息失败，不影响主要预约数据
                            System.err.println("获取房屋信息失败: " + e.getMessage());
                        }
                        return appointmentMap;
                    })
                    .toList();

            result.put("success", true);
            result.put("appointments", appointmentList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取预约列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据房东手机号和状态获取预约列表
     */
    public Map<String, Object> getAppointmentsByLandlordPhoneAndStatus(String landlordPhone, Integer status) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ViewingAppointment> appointments = viewingAppointmentRepository.findByLandlordPhoneAndStatus(landlordPhone, status);
            List<Map<String, Object>> appointmentList = appointments.stream()
                    .map(this::convertToMap)
                    .toList();

            result.put("success", true);
            result.put("appointments", appointmentList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取预约列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 生成预约编号
     */
    private String generateAppointmentNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "AP" + timestamp.substring(timestamp.length() - 6) + random;
    }

    /**
     * 将房屋实体转换为Map（只包含地址信息）
     */
    private Map<String, Object> convertRoomToMap(RoomInfo room) {
        Map<String, Object> roomMap = new HashMap<>();
        
        // 构建完整地址
        String address = buildFullAddress(room);
        roomMap.put("address", address);
        
        return roomMap;
    }

    /**
     * 构建完整地址
     */
    private String buildFullAddress(RoomInfo room) {
        StringBuilder address = new StringBuilder();
        
        if (room.getProvince() != null && !room.getProvince().isEmpty()) {
            address.append(room.getProvince());
        }
        if (room.getCity() != null && !room.getCity().isEmpty()) {
            address.append(room.getCity());
        }
        if (room.getDistrict() != null && !room.getDistrict().isEmpty()) {
            address.append(room.getDistrict());
        }
        if (room.getStreet() != null && !room.getStreet().isEmpty()) {
            address.append(room.getStreet());
        }
        if (room.getCommunityName() != null && !room.getCommunityName().isEmpty()) {
            address.append(room.getCommunityName());
        }
        if (room.getBuildingUnit() != null && !room.getBuildingUnit().isEmpty()) {
            address.append(room.getBuildingUnit());
        }
        if (room.getDoorNumber() != null && !room.getDoorNumber().isEmpty()) {
            address.append(room.getDoorNumber());
        }
        if (room.getRoomNumber() != null && !room.getRoomNumber().isEmpty()) {
            address.append(room.getRoomNumber());
        }
        
        return address.length() > 0 ? address.toString() : "未知地址";
    }

    /**
     * 将预约实体转换为Map
     */
    private Map<String, Object> convertToMap(ViewingAppointment appointment) {
        Map<String, Object> appointmentMap = new HashMap<>();
        appointmentMap.put("id", appointment.getId());
        appointmentMap.put("appointmentNumber", appointment.getAppointmentNumber());
        appointmentMap.put("roomId", appointment.getRoomId());
        appointmentMap.put("userId", appointment.getUserId());
        appointmentMap.put("preferredDate", appointment.getPreferredDate());
        appointmentMap.put("preferredTimeSlot", appointment.getPreferredTimeSlot());
        appointmentMap.put("actualDate", appointment.getActualDate());
        appointmentMap.put("actualTimeSlot", appointment.getActualTimeSlot());
        appointmentMap.put("appointmentType", appointment.getAppointmentType());
        appointmentMap.put("contactName", appointment.getContactName());
        appointmentMap.put("contactPhone", appointment.getContactPhone());
        appointmentMap.put("wechatId", appointment.getWechatId());
        appointmentMap.put("tenantCount", appointment.getTenantCount());
        appointmentMap.put("expectedMoveInDate", appointment.getExpectedMoveInDate());
        appointmentMap.put("rentalIntention", appointment.getRentalIntention());
        appointmentMap.put("status", appointment.getStatus());
        appointmentMap.put("cancellationReason", appointment.getCancellationReason());
        appointmentMap.put("assignedAgentId", appointment.getAssignedAgentId());
        appointmentMap.put("agentNotes", appointment.getAgentNotes());
        appointmentMap.put("adminNotes", appointment.getAdminNotes());
        appointmentMap.put("followUpRequired", appointment.getFollowUpRequired());
        appointmentMap.put("followUpDate", appointment.getFollowUpDate());
        appointmentMap.put("appointmentCreatedTime", appointment.getAppointmentCreatedTime());
        appointmentMap.put("confirmedTime", appointment.getConfirmedTime());
        appointmentMap.put("completedTime", appointment.getCompletedTime());
        appointmentMap.put("cancelledTime", appointment.getCancelledTime());
        appointmentMap.put("updatedTime", appointment.getUpdatedTime());

        return appointmentMap;
    }
}