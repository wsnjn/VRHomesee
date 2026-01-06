/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：MaintenanceRequest.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：报修申请实体类，记录租客提交的维修请求，包含故障描述、处理状态、维修费用及其承担方信息
 * 创建日期：2026-01-06
 */
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

    @Column(name = "request_title", nullable = false)
    private String requestTitle;

    @Column(name = "request_description", nullable = false, columnDefinition = "TEXT")
    private String requestDescription;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "request_status", nullable = false)
    private Integer requestStatus = 0; // 0-待处理, 1-处理中, 2-已完成, 3-已关闭

    @Column(name = "expected_fix_date")
    private LocalDate expectedFixDate;

    @Column(name = "actual_fix_date")
    private LocalDate actualFixDate;

    @Column(name = "repair_cost")
    private BigDecimal repairCost;

    @Column(name = "cost_bearer")
    private Integer costBearer; // 1-房东承担, 2-租客承担, 3-双方分担

    @Column(name = "fix_notes", columnDefinition = "TEXT")
    private String fixNotes;

    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    // Getter 和 Setter 方法
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

    public String getFixNotes() {
        return fixNotes;
    }

    public void setFixNotes(String fixNotes) {
        this.fixNotes = fixNotes;
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
