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
