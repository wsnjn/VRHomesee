package com.example.homesee.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_request")
public class MaintenanceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_management_id", nullable = false)
    private Long tenantManagementId;

    @Column(name = "request_title", nullable = false, length = 100)
    private String requestTitle;

    @Column(name = "request_description", nullable = false, columnDefinition = "TEXT")
    private String requestDescription;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "request_status", nullable = false)
    private Integer requestStatus = 0; // 0-待处理，1-处理中，2-已完成，3-已关闭

    @Column(name = "expected_fix_date")
    private LocalDate expectedFixDate;

    @Column(name = "actual_fix_date")
    private LocalDate actualFixDate;

    @Column(name = "repair_cost", precision = 8, scale = 2)
    private BigDecimal repairCost;

    @Column(name = "cost_bearer")
    private Integer costBearer; // 费用承担方：1-房东，2-租客，3-分摊

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime = LocalDateTime.now();

    // 构造函数
    public MaintenanceRequest() {
    }

    public MaintenanceRequest(Long tenantManagementId, String requestTitle, 
                             String requestDescription, LocalDateTime requestDate) {
        this.tenantManagementId = tenantManagementId;
        this.requestTitle = requestTitle;
        this.requestDescription = requestDescription;
        this.requestDate = requestDate;
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

    public String getRequestTitle() {
        return requestTitle;
    }

    public void setRequestTitle(String requestTitle) {
        this.requestTitle = requestTitle;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public Integer getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(Integer requestStatus) {
        this.requestStatus = requestStatus;
    }

    public LocalDate getExpectedFixDate() {
        return expectedFixDate;
    }

    public void setExpectedFixDate(LocalDate expectedFixDate) {
        this.expectedFixDate = expectedFixDate;
    }

    public LocalDate getActualFixDate() {
        return actualFixDate;
    }

    public void setActualFixDate(LocalDate actualFixDate) {
        this.actualFixDate = actualFixDate;
    }

    public BigDecimal getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(BigDecimal repairCost) {
        this.repairCost = repairCost;
    }

    public Integer getCostBearer() {
        return costBearer;
    }

    public void setCostBearer(Integer costBearer) {
        this.costBearer = costBearer;
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
