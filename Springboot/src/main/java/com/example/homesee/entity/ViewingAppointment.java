package com.example.homesee.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "viewing_appointment")
public class ViewingAppointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_number", nullable = false, unique = true, length = 50)
    private String appointmentNumber;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 预约信息
    @Column(name = "preferred_date", nullable = false)
    private LocalDate preferredDate;

    @Column(name = "preferred_time_slot", nullable = false, length = 20)
    private String preferredTimeSlot;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Column(name = "actual_time_slot", length = 20)
    private String actualTimeSlot;

    @Column(name = "appointment_type", nullable = false)
    private Integer appointmentType = 1; // 1-现场看房，2-视频看房

    // 用户信息
    @Column(name = "contact_name", nullable = false, length = 50)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "wechat_id", length = 50)
    private String wechatId;

    @Column(name = "tenant_count")
    private Integer tenantCount;

    @Column(name = "expected_move_in_date")
    private LocalDate expectedMoveInDate;

    @Column(name = "rental_intention", columnDefinition = "TEXT")
    private String rentalIntention;

    // 预约状态
    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0-待确认，1-已确认，2-已完成，3-已取消，4-已过期，5-用户爽约

    @Column(name = "cancellation_reason", length = 200)
    private String cancellationReason;

    // 管理信息
    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Column(name = "agent_notes", columnDefinition = "TEXT")
    private String agentNotes;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "follow_up_required", nullable = false)
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    // 时间戳
    @Column(name = "appointment_created_time", nullable = false)
    private LocalDateTime appointmentCreatedTime = LocalDateTime.now();

    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @Column(name = "cancelled_time")
    private LocalDateTime cancelledTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime = LocalDateTime.now();

    // 构造函数
    public ViewingAppointment() {
    }

    public ViewingAppointment(String appointmentNumber, Long roomId, Long userId, 
                            LocalDate preferredDate, String preferredTimeSlot, 
                            Integer appointmentType, String contactName, String contactPhone) {
        this.appointmentNumber = appointmentNumber;
        this.roomId = roomId;
        this.userId = userId;
        this.preferredDate = preferredDate;
        this.preferredTimeSlot = preferredTimeSlot;
        this.appointmentType = appointmentType;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.appointmentCreatedTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(LocalDate preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getPreferredTimeSlot() {
        return preferredTimeSlot;
    }

    public void setPreferredTimeSlot(String preferredTimeSlot) {
        this.preferredTimeSlot = preferredTimeSlot;
    }

    public LocalDate getActualDate() {
        return actualDate;
    }

    public void setActualDate(LocalDate actualDate) {
        this.actualDate = actualDate;
    }

    public String getActualTimeSlot() {
        return actualTimeSlot;
    }

    public void setActualTimeSlot(String actualTimeSlot) {
        this.actualTimeSlot = actualTimeSlot;
    }

    public Integer getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(Integer appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getWechatId() {
        return wechatId;
    }

    public void setWechatId(String wechatId) {
        this.wechatId = wechatId;
    }

    public Integer getTenantCount() {
        return tenantCount;
    }

    public void setTenantCount(Integer tenantCount) {
        this.tenantCount = tenantCount;
    }

    public LocalDate getExpectedMoveInDate() {
        return expectedMoveInDate;
    }

    public void setExpectedMoveInDate(LocalDate expectedMoveInDate) {
        this.expectedMoveInDate = expectedMoveInDate;
    }

    public String getRentalIntention() {
        return rentalIntention;
    }

    public void setRentalIntention(String rentalIntention) {
        this.rentalIntention = rentalIntention;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Long getAssignedAgentId() {
        return assignedAgentId;
    }

    public void setAssignedAgentId(Long assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
    }

    public String getAgentNotes() {
        return agentNotes;
    }

    public void setAgentNotes(String agentNotes) {
        this.agentNotes = agentNotes;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public Boolean getFollowUpRequired() {
        return followUpRequired;
    }

    public void setFollowUpRequired(Boolean followUpRequired) {
        this.followUpRequired = followUpRequired;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDate followUpDate) {
        this.followUpDate = followUpDate;
    }

    public LocalDateTime getAppointmentCreatedTime() {
        return appointmentCreatedTime;
    }

    public void setAppointmentCreatedTime(LocalDateTime appointmentCreatedTime) {
        this.appointmentCreatedTime = appointmentCreatedTime;
    }

    public LocalDateTime getConfirmedTime() {
        return confirmedTime;
    }

    public void setConfirmedTime(LocalDateTime confirmedTime) {
        this.confirmedTime = confirmedTime;
    }

    public LocalDateTime getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }

    public LocalDateTime getCancelledTime() {
        return cancelledTime;
    }

    public void setCancelledTime(LocalDateTime cancelledTime) {
        this.cancelledTime = cancelledTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
