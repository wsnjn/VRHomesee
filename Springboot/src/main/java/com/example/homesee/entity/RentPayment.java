package com.example.homesee.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rent_payment")
public class RentPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_management_id", nullable = false)
    private Long tenantManagementId;

    @Column(name = "payment_period", nullable = false, length = 20)
    private String paymentPeriod;

    @Column(name = "payable_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payableAmount;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_method")
    private Integer paymentMethod; // 1-微信，2-支付宝，3-银行卡，4-现金

    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus = 0; // 0-未支付，1-已支付，2-部分支付

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "late_fee", precision = 8, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime = LocalDateTime.now();

    // 构造函数
    public RentPayment() {
    }

    public RentPayment(Long tenantManagementId, String paymentPeriod, 
                      BigDecimal payableAmount, LocalDate dueDate) {
        this.tenantManagementId = tenantManagementId;
        this.paymentPeriod = paymentPeriod;
        this.payableAmount = payableAmount;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantManagementId() {
        return tenantManagementId;
    }

    public void setTenantManagementId(Long tenantManagementId) {
        this.tenantManagementId = tenantManagementId;
    }

    public String getPaymentPeriod() {
        return paymentPeriod;
    }

    public void setPaymentPeriod(String paymentPeriod) {
        this.paymentPeriod = paymentPeriod;
    }

    public BigDecimal getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(BigDecimal payableAmount) {
        this.payableAmount = payableAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Integer getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Integer paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
