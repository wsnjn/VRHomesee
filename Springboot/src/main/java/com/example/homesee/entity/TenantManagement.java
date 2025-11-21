package com.example.homesee.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_management")
public class TenantManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_number", unique = true, nullable = false, length = 50)
    private String contractNumber;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "landlord_id", nullable = false)
    private Long landlordId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    // 租约信息
    @Column(name = "contract_start_date", nullable = false)
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date", nullable = false)
    private LocalDate contractEndDate;

    @Column(name = "actual_move_in_date")
    private LocalDate actualMoveInDate;

    @Column(name = "actual_move_out_date")
    private LocalDate actualMoveOutDate;

    @Column(name = "monthly_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "payment_cycle", nullable = false)
    private Integer paymentCycle = 1; // 1-月付，2-季付，3-年付

    // 租约状态
    @Column(name = "contract_status", nullable = false)
    private Integer contractStatus = 0; // 0-待签约，1-已签约，2-履行中，3-已到期，4-提前解约，5-已退租

    @Column(name = "rent_status", nullable = false)
    private Integer rentStatus = 0; // 0-未付款，1-已付款，2-逾期，3-部分付款

    @Column(name = "deposit_status", nullable = false)
    private Integer depositStatus = 0; // 0-未付，1-已付，2-已退，3-抵扣中

    // 费用信息
    @Column(name = "water_initial_reading", precision = 8, scale = 2)
    private BigDecimal waterInitialReading;

    @Column(name = "electric_initial_reading", precision = 8, scale = 2)
    private BigDecimal electricInitialReading;

    @Column(name = "last_water_reading", precision = 8, scale = 2)
    private BigDecimal lastWaterReading;

    @Column(name = "last_electric_reading", precision = 8, scale = 2)
    private BigDecimal lastElectricReading;

    // 联系信息
    @Column(name = "emergency_contact", length = 50)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    // 时间戳
    @Column(name = "contract_signed_time")
    private LocalDateTime contractSignedTime;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime = LocalDateTime.now();

    // 构造函数
    public TenantManagement() {
    }

    public TenantManagement(String contractNumber, Long roomId, Long landlordId, Long tenantId, 
                          LocalDate contractStartDate, LocalDate contractEndDate, 
                          BigDecimal monthlyRent, BigDecimal depositAmount) {
        this.contractNumber = contractNumber;
        this.roomId = roomId;
        this.landlordId = landlordId;
        this.tenantId = tenantId;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.monthlyRent = monthlyRent;
        this.depositAmount = depositAmount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(Long landlordId) {
        this.landlordId = landlordId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDate getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(LocalDate contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public LocalDate getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public LocalDate getActualMoveInDate() {
        return actualMoveInDate;
    }

    public void setActualMoveInDate(LocalDate actualMoveInDate) {
        this.actualMoveInDate = actualMoveInDate;
    }

    public LocalDate getActualMoveOutDate() {
        return actualMoveOutDate;
    }

    public void setActualMoveOutDate(LocalDate actualMoveOutDate) {
        this.actualMoveOutDate = actualMoveOutDate;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Integer getPaymentCycle() {
        return paymentCycle;
    }

    public void setPaymentCycle(Integer paymentCycle) {
        this.paymentCycle = paymentCycle;
    }

    public Integer getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(Integer contractStatus) {
        this.contractStatus = contractStatus;
    }

    public Integer getRentStatus() {
        return rentStatus;
    }

    public void setRentStatus(Integer rentStatus) {
        this.rentStatus = rentStatus;
    }

    public Integer getDepositStatus() {
        return depositStatus;
    }

    public void setDepositStatus(Integer depositStatus) {
        this.depositStatus = depositStatus;
    }

    public BigDecimal getWaterInitialReading() {
        return waterInitialReading;
    }

    public void setWaterInitialReading(BigDecimal waterInitialReading) {
        this.waterInitialReading = waterInitialReading;
    }

    public BigDecimal getElectricInitialReading() {
        return electricInitialReading;
    }

    public void setElectricInitialReading(BigDecimal electricInitialReading) {
        this.electricInitialReading = electricInitialReading;
    }

    public BigDecimal getLastWaterReading() {
        return lastWaterReading;
    }

    public void setLastWaterReading(BigDecimal lastWaterReading) {
        this.lastWaterReading = lastWaterReading;
    }

    public BigDecimal getLastElectricReading() {
        return lastElectricReading;
    }

    public void setLastElectricReading(BigDecimal lastElectricReading) {
        this.lastElectricReading = lastElectricReading;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public LocalDateTime getContractSignedTime() {
        return contractSignedTime;
    }

    public void setContractSignedTime(LocalDateTime contractSignedTime) {
        this.contractSignedTime = contractSignedTime;
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
