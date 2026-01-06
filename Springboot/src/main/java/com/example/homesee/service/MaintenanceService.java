/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：MaintenanceService.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：报修服务类，提供租约校验、报修申请创建、历史工单查询及房东视角下的全量工单综合展示逻辑实现
 * 创建日期：2026-01-06
 */
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

    @Autowired
    private com.example.homesee.repository.UserRepository userRepository;

    @Autowired
    private com.example.homesee.repository.RoomInfoRepository roomInfoRepository;

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
        if (id == null) {
            return null;
        }
        return maintenanceRepository.findById(id).orElse(null);
    }

    /**
     * 获取用户的活跃租约
     * 
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

    /**
     * 获取房东的所有维修工单
     * 
     * @param landlordId 房东ID
     * @return 维修工单列表（包含租户和房屋信息）
     */
    public List<Map<String, Object>> getRequestsByLandlordId(Long landlordId) {
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        // 1. 获取房东的所有租约
        List<TenantManagement> contracts = tenantManagementRepository.findByLandlordId(landlordId);
        if (contracts.isEmpty()) {
            return result;
        }

        List<Long> contractIds = contracts.stream()
                .map(TenantManagement::getId)
                .collect(java.util.stream.Collectors.toList());

        // 2. 获取这些租约的所有维修工单
        List<MaintenanceRequest> requests = maintenanceRepository.findByTenantManagementIdIn(contractIds);

        // 3. 组装数据，补充租户和房屋信息
        for (MaintenanceRequest req : requests) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", req.getId());
            map.put("tenantManagementId", req.getTenantManagementId());
            map.put("requestTitle", req.getRequestTitle());
            map.put("requestDescription", req.getRequestDescription());
            map.put("requestStatus", req.getRequestStatus());
            map.put("requestDate", req.getRequestDate());
            map.put("expectedFixDate", req.getExpectedFixDate());
            map.put("actualFixDate", req.getActualFixDate());
            map.put("repairCost", req.getRepairCost());
            map.put("costBearer", req.getCostBearer());
            map.put("fixNotes", req.getFixNotes());
            map.put("createdTime", req.getCreatedTime());
            map.put("updatedTime", req.getUpdatedTime());

            // 查找对应的租约
            TenantManagement contract = contracts.stream()
                    .filter(c -> c.getId().equals(req.getTenantManagementId()))
                    .findFirst()
                    .orElse(null);

            if (contract != null) {
                // 补充租户信息
                Long tenantId = contract.getTenantId();
                map.put("tenantId", tenantId);
                if (tenantId != null) {
                    userRepository.findById(tenantId).ifPresent(user -> {
                        map.put("tenantName", user.getRealName() != null ? user.getRealName() : user.getUsername());
                        map.put("tenantPhone", user.getPhone());
                    });
                }

                // 补充房屋信息
                map.put("roomId", contract.getRoomId());
                map.put("contractNumber", contract.getContractNumber());
                roomInfoRepository.findById(contract.getRoomId()).ifPresent(room -> {
                    String address = String.format("%s%s%s%s %s %s-%s-%s",
                            room.getProvince(), room.getCity(), room.getDistrict(), room.getStreet(),
                            room.getCommunityName(), room.getBuildingUnit(), room.getDoorNumber(),
                            room.getRoomNumber());
                    map.put("roomAddress", address);
                    map.put("communityName", room.getCommunityName());
                });
            }

            result.add(map);
        }

        // 按申请时间倒序排序
        result.sort((a, b) -> {
            java.time.LocalDateTime t1 = (java.time.LocalDateTime) a.get("createdTime");
            java.time.LocalDateTime t2 = (java.time.LocalDateTime) b.get("createdTime");
            return t2.compareTo(t1);
        });

        return result;
    }
}
