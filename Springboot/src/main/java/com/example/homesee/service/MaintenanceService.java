package com.example.homesee.service;

import com.example.homesee.entity.MaintenanceRequest;
import com.example.homesee.entity.TenantManagement;
import com.example.homesee.repository.MaintenanceRepository;
import com.example.homesee.repository.TenantManagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private TenantManagementRepository tenantManagementRepository;

    public MaintenanceRequest createRequest(MaintenanceRequest request) {
        return maintenanceRepository.save(request);
    }

    public List<MaintenanceRequest> getRequestsByLeaseId(Long leaseId) {
        return maintenanceRepository.findByTenantManagementId(leaseId);
    }

    public MaintenanceRequest updateRequest(MaintenanceRequest request) {
        return maintenanceRepository.save(request);
    }

    public MaintenanceRequest getRequestById(Long id) {
        return maintenanceRepository.findById(id).orElse(null);
    }

    /**
     * 获取用户的活跃租约
     * @param userId 用户ID
     * @return 活跃租约信息
     */
    public Map<String, Object> getActiveLeaseByUserId(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取用户的所有租约
            List<TenantManagement> userLeases = tenantManagementRepository.findByTenantId(userId);
            
            // 查找当前有效的租约（状态为已签约或履行中，且租期未结束）
            TenantManagement activeLease = userLeases.stream()
                .filter(lease -> {
                    Integer status = lease.getContractStatus();
                    LocalDate endDate = lease.getContractEndDate();
                    LocalDate today = LocalDate.now();
                    
                    // 状态为已签约(1)或履行中(2)，且租期未结束
                    return (status == 1 || status == 2) && 
                           (endDate == null || endDate.isAfter(today) || endDate.isEqual(today));
                })
                .findFirst()
                .orElse(null);
            
            if (activeLease != null) {
                Map<String, Object> leaseInfo = new HashMap<>();
                leaseInfo.put("id", activeLease.getId());
                leaseInfo.put("contractNumber", activeLease.getContractNumber());
                leaseInfo.put("roomId", activeLease.getRoomId());
                leaseInfo.put("contractStartDate", activeLease.getContractStartDate());
                leaseInfo.put("contractEndDate", activeLease.getContractEndDate());
                leaseInfo.put("contractStatus", activeLease.getContractStatus());
                leaseInfo.put("monthlyRent", activeLease.getMonthlyRent());
                
                // 这里可以添加获取房间详细信息的逻辑
                leaseInfo.put("roomName", "房间 " + activeLease.getRoomId()); // 临时占位
                
                result.put("success", true);
                result.put("data", leaseInfo);
            } else {
                result.put("success", true);
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取租约信息失败: " + e.getMessage());
        }
        
        return result;
    }
}
