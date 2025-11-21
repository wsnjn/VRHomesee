package com.example.homesee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(name = "appointment_type", nullable = false)
    private Integer appointmentType; // 1: 线下看房, 2: 签合同

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0: 待确认, 1: 已确认, 2: 已完成, 3: 已取消

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    // 构造函数
    public Appointment() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    public Appointment(Long houseId, Long userId, String contactName, String contactPhone, 
                      LocalDateTime appointmentDateTime, Integer appointmentType, String remarks) {
        this();
        this.houseId = houseId;
        this.userId = userId;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.appointmentDateTime = appointmentDateTime;
        this.appointmentType = appointmentType;
        this.remarks = remarks;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public Integer getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(Integer appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
