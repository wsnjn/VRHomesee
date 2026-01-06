/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：ContractChange.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：合同变更记录实体类，记录合同生命周期中的关键变动（如续租、退租、转租及租金调整）及其原因和时间
 * 创建日期：2026-01-06
 */
package com.example.homesee.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_change")
public class ContractChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_management_id", nullable = false)
    private Long tenantManagementId;

    @Column(name = "change_type", nullable = false)
    private Integer changeType; // 变更类型：1-续租，2-退租，3-转租，4-租金调整

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500)
    private String newValue;

    @Column(name = "change_date", nullable = false)
    private LocalDate changeDate;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    // 构造函数
    public ContractChange() {
    }

    public ContractChange(Long tenantManagementId, Integer changeType,
            LocalDate changeDate) {
        this.tenantManagementId = tenantManagementId;
        this.changeType = changeType;
        this.changeDate = changeDate;
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

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDate getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDate changeDate) {
        this.changeDate = changeDate;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
