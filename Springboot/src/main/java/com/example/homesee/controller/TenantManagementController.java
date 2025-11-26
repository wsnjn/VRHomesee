package com.example.homesee.controller;

import com.example.homesee.service.TenantManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tenant")
@CrossOrigin(origins = "http://39.108.142.250:9999")
public class TenantManagementController {

    @Autowired
    private TenantManagementService tenantManagementService;

    /**
     * 创建租约
     * 
     * @param request 租约请求数据
     * @return 创建结果
     */
    @PostMapping("/create")
    public Map<String, Object> createTenantContract(@RequestBody Map<String, Object> request) {
        try {
            String contractNumber = (String) request.get("contractNumber");
            Long roomId = Long.valueOf(request.get("roomId").toString());
            Long landlordId = Long.valueOf(request.get("landlordId").toString());
            Long tenantId = Long.valueOf(request.get("tenantId").toString());
            String contractStartDateStr = (String) request.get("contractStartDate");
            String contractEndDateStr = (String) request.get("contractEndDate");
            Double monthlyRent = Double.valueOf(request.get("monthlyRent").toString());
            Double depositAmount = Double.valueOf(request.get("depositAmount").toString());

            // 解析日期
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate contractStartDate = LocalDate.parse(contractStartDateStr, formatter);
            LocalDate contractEndDate = LocalDate.parse(contractEndDateStr, formatter);

            return tenantManagementService.createTenantContract(contractNumber, roomId, landlordId,
                    tenantId, contractStartDate, contractEndDate,
                    monthlyRent, depositAmount);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 获取租约详情
     * 
     * @param contractId 租约ID
     * @return 租约详情
     */
    @GetMapping("/{contractId}")
    public Map<String, Object> getContractDetail(@PathVariable Long contractId) {
        return tenantManagementService.getContractDetail(contractId);
    }

    /**
     * 获取所有租约列表
     * 
     * @return 租约列表
     */
    @GetMapping("/all")
    public Map<String, Object> getAllContracts() {
        return tenantManagementService.getAllContracts();
    }

    /**
     * 根据状态获取租约列表
     * 
     * @param status 状态
     * @return 租约列表
     */
    @GetMapping("/status/{status}")
    public Map<String, Object> getContractsByStatus(@PathVariable Integer status) {
        return tenantManagementService.getContractsByStatus(status);
    }

    /**
     * 更新租约状态
     * 
     * @param contractId 租约ID
     * @param request    状态更新请求
     * @return 更新结果
     */
    @PutMapping("/{contractId}/status")
    public Map<String, Object> updateContractStatus(@PathVariable Long contractId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer status = Integer.valueOf(request.get("status").toString());
            return tenantManagementService.updateContractStatus(contractId, status);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 更新租金状态
     * 
     * @param contractId 租约ID
     * @param request    租金状态更新请求
     * @return 更新结果
     */
    @PutMapping("/{contractId}/rent-status")
    public Map<String, Object> updateRentStatus(@PathVariable Long contractId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer rentStatus = Integer.valueOf(request.get("rentStatus").toString());
            return tenantManagementService.updateRentStatus(contractId, rentStatus);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 更新押金状态
     * 
     * @param contractId 租约ID
     * @param request    押金状态更新请求
     * @return 更新结果
     */
    @PutMapping("/{contractId}/deposit-status")
    public Map<String, Object> updateDepositStatus(@PathVariable Long contractId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer depositStatus = Integer.valueOf(request.get("depositStatus").toString());
            return tenantManagementService.updateDepositStatus(contractId, depositStatus);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "请求参数错误: " + e.getMessage());
        }
    }

    /**
     * 获取租约统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getContractStatistics() {
        return tenantManagementService.getContractStatistics();
    }

    /**
     * 获取即将到期的租约
     * 
     * @param days 天数
     * @return 即将到期的租约列表
     */
    @GetMapping("/expiring/{days}")
    public Map<String, Object> getExpiringContracts(@PathVariable Integer days) {
        return tenantManagementService.getExpiringContracts(days);
    }

    /**
     * 根据租客ID获取租约列表
     * 
     * @param tenantId 租客ID
     * @return 租约列表
     */
    @GetMapping("/tenant/{tenantId}")
    public Map<String, Object> getContractsByTenantId(@PathVariable Long tenantId) {
        return tenantManagementService.getContractsByTenantId(tenantId);
    }

    /**
     * 获取租约趋势（最近7天）
     * 
     * @return 趋势数据
     */
    @GetMapping("/trends")
    public Map<String, Object> getContractTrends() {
        return tenantManagementService.getContractTrends();
    }
}
