package com.example.homesee.service;

import com.example.homesee.entity.TenantManagement;
import com.example.homesee.repository.TenantManagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TenantManagementService {

    @Autowired
    private TenantManagementRepository tenantManagementRepository;

    /**
     * 创建租约
     * @param contractNumber 合同编号
     * @param roomId 房屋ID
     * @param landlordId 房东ID
     * @param tenantId 租客ID
     * @param contractStartDate 合同开始日期
     * @param contractEndDate 合同结束日期
     * @param monthlyRent 月租金
     * @param depositAmount 押金金额
     * @return 创建结果
     */
    public Map<String, Object> createTenantContract(String contractNumber, Long roomId, Long landlordId, 
                                                   Long tenantId, LocalDate contractStartDate, 
                                                   LocalDate contractEndDate, Double monthlyRent, 
                                                   Double depositAmount) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查合同编号是否已存在
            TenantManagement existingContract = tenantManagementRepository.findByContractNumber(contractNumber);
            if (existingContract != null) {
                result.put("success", false);
                result.put("message", "合同编号已存在");
                return result;
            }

            // 检查房屋是否已有活跃租约
            List<TenantManagement> activeContracts = tenantManagementRepository.findByRoomIdAndContractStatus(roomId, 1);
            if (!activeContracts.isEmpty()) {
                result.put("success", false);
                result.put("message", "该房屋已有活跃租约");
                return result;
            }

            // 创建租约
            TenantManagement contract = new TenantManagement(contractNumber, roomId, landlordId, tenantId,
                    contractStartDate, contractEndDate, 
                    java.math.BigDecimal.valueOf(monthlyRent), 
                    java.math.BigDecimal.valueOf(depositAmount));
            
            contract.setContractSignedTime(LocalDateTime.now());
            contract.setContractStatus(1); // 已签约
            
            TenantManagement savedContract = tenantManagementRepository.save(contract);
            
            result.put("success", true);
            result.put("message", "租约创建成功");
            result.put("contractId", savedContract.getId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建租约失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取租约详情
     * @param contractId 租约ID
     * @return 租约详情
     */
    public Map<String, Object> getContractDetail(Long contractId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<TenantManagement> contractOptional = tenantManagementRepository.findById(contractId);
            if (contractOptional.isPresent()) {
                TenantManagement contract = contractOptional.get();
                
                Map<String, Object> contractInfo = new HashMap<>();
                contractInfo.put("id", contract.getId());
                contractInfo.put("contractNumber", contract.getContractNumber());
                contractInfo.put("roomId", contract.getRoomId());
                contractInfo.put("landlordId", contract.getLandlordId());
                contractInfo.put("tenantId", contract.getTenantId());
                contractInfo.put("contractStartDate", contract.getContractStartDate());
                contractInfo.put("contractEndDate", contract.getContractEndDate());
                contractInfo.put("monthlyRent", contract.getMonthlyRent());
                contractInfo.put("depositAmount", contract.getDepositAmount());
                contractInfo.put("paymentCycle", contract.getPaymentCycle());
                contractInfo.put("contractStatus", contract.getContractStatus());
                contractInfo.put("rentStatus", contract.getRentStatus());
                contractInfo.put("depositStatus", contract.getDepositStatus());
                contractInfo.put("emergencyContact", contract.getEmergencyContact());
                contractInfo.put("emergencyPhone", contract.getEmergencyPhone());
                contractInfo.put("contractSignedTime", contract.getContractSignedTime());
                
                result.put("success", true);
                result.put("contract", contractInfo);
            } else {
                result.put("success", false);
                result.put("message", "租约不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取租约详情失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取所有租约列表
     * @return 租约列表
     */
    public Map<String, Object> getAllContracts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<TenantManagement> contracts = tenantManagementRepository.findAll();
            result.put("success", true);
            result.put("contracts", contracts);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取租约列表失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 根据状态获取租约列表
     * @param status 状态
     * @return 租约列表
     */
    public Map<String, Object> getContractsByStatus(Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<TenantManagement> contracts = tenantManagementRepository.findByContractStatus(status);
            result.put("success", true);
            result.put("contracts", contracts);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取租约列表失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新租约状态
     * @param contractId 租约ID
     * @param status 新状态
     * @return 更新结果
     */
    public Map<String, Object> updateContractStatus(Long contractId, Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<TenantManagement> contractOptional = tenantManagementRepository.findById(contractId);
            if (contractOptional.isPresent()) {
                TenantManagement contract = contractOptional.get();
                contract.setContractStatus(status);
                contract.setUpdatedTime(LocalDateTime.now());
                
                tenantManagementRepository.save(contract);
                
                result.put("success", true);
                result.put("message", "租约状态更新成功");
            } else {
                result.put("success", false);
                result.put("message", "租约不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新租约状态失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新租金状态
     * @param contractId 租约ID
     * @param rentStatus 租金状态
     * @return 更新结果
     */
    public Map<String, Object> updateRentStatus(Long contractId, Integer rentStatus) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<TenantManagement> contractOptional = tenantManagementRepository.findById(contractId);
            if (contractOptional.isPresent()) {
                TenantManagement contract = contractOptional.get();
                contract.setRentStatus(rentStatus);
                contract.setUpdatedTime(LocalDateTime.now());
                
                tenantManagementRepository.save(contract);
                
                result.put("success", true);
                result.put("message", "租金状态更新成功");
            } else {
                result.put("success", false);
                result.put("message", "租约不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新租金状态失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新押金状态
     * @param contractId 租约ID
     * @param depositStatus 押金状态
     * @return 更新结果
     */
    public Map<String, Object> updateDepositStatus(Long contractId, Integer depositStatus) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<TenantManagement> contractOptional = tenantManagementRepository.findById(contractId);
            if (contractOptional.isPresent()) {
                TenantManagement contract = contractOptional.get();
                contract.setDepositStatus(depositStatus);
                contract.setUpdatedTime(LocalDateTime.now());
                
                tenantManagementRepository.save(contract);
                
                result.put("success", true);
                result.put("message", "押金状态更新成功");
            } else {
                result.put("success", false);
                result.put("message", "租约不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新押金状态失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取租约统计信息
     * @return 统计信息
     */
    public Map<String, Object> getContractStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<TenantManagement> activeContracts = tenantManagementRepository.findActiveContracts();
            List<TenantManagement> expiringContracts = tenantManagementRepository.findExpiringContracts(LocalDate.now().plusMonths(1));
            List<Object[]> statusCounts = tenantManagementRepository.countByContractStatus();
            
            Map<Integer, Long> statusMap = new HashMap<>();
            for (Object[] statusCount : statusCounts) {
                statusMap.put((Integer) statusCount[0], (Long) statusCount[1]);
            }
            
            result.put("success", true);
            result.put("totalContracts", tenantManagementRepository.count());
            result.put("activeContracts", activeContracts.size());
            result.put("expiringContracts", expiringContracts.size());
            result.put("statusDistribution", statusMap);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取即将到期的租约
     * @param days 天数
     * @return 即将到期的租约列表
     */
    public Map<String, Object> getExpiringContracts(Integer days) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate endDate = LocalDate.now().plusDays(days);
            List<TenantManagement> expiringContracts = tenantManagementRepository.findExpiringContracts(endDate);
            
            result.put("success", true);
            result.put("contracts", expiringContracts);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取即将到期租约失败: " + e.getMessage());
        }
        
        return result;
    }
}
