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

    @Autowired
    private CommunityService communityService;

    @Autowired
    private com.example.homesee.repository.UserRepository userRepository;

    @Autowired
    private com.example.homesee.repository.RoomInfoRepository roomInfoRepository;

    /**
     * 创建租约
     * 
     * @param contractNumber    合同编号
     * @param roomId            房屋ID
     * @param landlordId        房东ID
     * @param tenantId          租客ID
     * @param contractStartDate 合同开始日期
     * @param contractEndDate   合同结束日期
     * @param monthlyRent       月租金
     * @param depositAmount     押金金额
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
            List<TenantManagement> activeContracts = tenantManagementRepository.findByRoomIdAndContractStatus(roomId,
                    1);
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

            // 自动加入租客群组 (假设租客群组Type=2)
            try {
                List<com.example.homesee.entity.ChatGroup> tenantGroups = communityService.getAllGroups().stream()
                        .filter(g -> g.getGroupType() == 2)
                        .toList();

                if (!tenantGroups.isEmpty()) {
                    communityService.joinGroup(tenantGroups.get(0).getId(), tenantId, 0);
                }
            } catch (Exception e) {
                System.err.println("Auto-join tenant group failed: " + e.getMessage());
            }

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
     * 
     * @param contractId 租约ID
     * @return 租约详情
     */
    public Map<String, Object> getContractDetail(Long contractId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<TenantManagement> contractOptional = tenantManagementRepository.findById(contractId);
            if (contractOptional.isPresent()) {
                TenantManagement contract = contractOptional.get();

                Map<String, Object> contractInfo = convertToMap(contract);

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
     * 
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
     * 
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
     * 
     * @param contractId 租约ID
     * @param status     新状态
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
     * 
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
     * 
     * @param contractId    租约ID
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
     * 
     * @return 统计信息
     */
    public Map<String, Object> getContractStatistics() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<TenantManagement> activeContracts = tenantManagementRepository.findActiveContracts();
            List<TenantManagement> expiringContracts = tenantManagementRepository
                    .findExpiringContracts(LocalDate.now().plusMonths(1));
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
     * 
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

    /**
     * 根据房东ID获取租约列表
     * 
     * @param landlordId 房东ID
     * @return 租约列表
     */
    public Map<String, Object> getContractsByLandlordId(Long landlordId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<TenantManagement> contracts = tenantManagementRepository.findByLandlordId(landlordId);
            List<Map<String, Object>> contractList = contracts.stream()
                    .map(this::convertToMap)
                    .toList();

            result.put("success", true);
            result.put("contracts", contractList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取租约列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据房东ID和状态获取租约列表
     * 
     * @param landlordId 房东ID
     * @param status     状态
     * @return 租约列表
     */
    public Map<String, Object> getContractsByLandlordIdAndStatus(Long landlordId, Integer status) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<TenantManagement> contracts = tenantManagementRepository.findByLandlordIdAndContractStatus(landlordId,
                    status);
            List<Map<String, Object>> contractList = contracts.stream()
                    .map(this::convertToMap)
                    .toList();

            result.put("success", true);
            result.put("contracts", contractList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取租约列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据租客ID获取租约列表
     * 
     * @param tenantId 租客ID
     * @return 租约列表
     */
    public Map<String, Object> getContractsByTenantId(Long tenantId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<TenantManagement> contracts = tenantManagementRepository.findByTenantId(tenantId);
            List<Map<String, Object>> contractList = contracts.stream()
                    .map(this::convertToMap)
                    .toList();

            result.put("success", true);
            result.put("contracts", contractList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取租约列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新水电读数
     * 
     * @param contractId      租约ID
     * @param waterReading    水表读数
     * @param electricReading 电表读数
     * @return 更新结果
     */
    public Map<String, Object> updateMeterReadings(Long contractId, Double waterReading, Double electricReading) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<TenantManagement> contractOptional = tenantManagementRepository.findById(contractId);
            if (contractOptional.isPresent()) {
                TenantManagement contract = contractOptional.get();

                if (waterReading != null) {
                    contract.setLastWaterReading(java.math.BigDecimal.valueOf(waterReading));
                }

                if (electricReading != null) {
                    contract.setLastElectricReading(java.math.BigDecimal.valueOf(electricReading));
                }

                contract.setUpdatedTime(LocalDateTime.now());
                tenantManagementRepository.save(contract);

                result.put("success", true);
                result.put("message", "水电读数更新成功");
            } else {
                result.put("success", false);
                result.put("message", "租约不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新水电读数失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 将租约实体转换为Map
     */
    private Map<String, Object> convertToMap(TenantManagement contract) {
        Map<String, Object> contractMap = new HashMap<>();
        contractMap.put("id", contract.getId());
        contractMap.put("contractNumber", contract.getContractNumber());
        contractMap.put("roomId", contract.getRoomId());
        contractMap.put("landlordId", contract.getLandlordId());
        contractMap.put("tenantId", contract.getTenantId());
        contractMap.put("contractStartDate", contract.getContractStartDate());
        contractMap.put("contractEndDate", contract.getContractEndDate());
        contractMap.put("actualMoveInDate", contract.getActualMoveInDate());
        contractMap.put("actualMoveOutDate", contract.getActualMoveOutDate());
        contractMap.put("monthlyRent", contract.getMonthlyRent());
        contractMap.put("depositAmount", contract.getDepositAmount());
        contractMap.put("paymentCycle", contract.getPaymentCycle());
        contractMap.put("contractStatus", contract.getContractStatus());
        contractMap.put("rentStatus", contract.getRentStatus());
        contractMap.put("depositStatus", contract.getDepositStatus());
        contractMap.put("waterInitialReading", contract.getWaterInitialReading());
        contractMap.put("electricInitialReading", contract.getElectricInitialReading());
        contractMap.put("lastWaterReading", contract.getLastWaterReading());
        contractMap.put("lastElectricReading", contract.getLastElectricReading());
        contractMap.put("emergencyContact", contract.getEmergencyContact());
        contractMap.put("emergencyPhone", contract.getEmergencyPhone());
        contractMap.put("contractSignedTime", contract.getContractSignedTime());
        contractMap.put("createdTime", contract.getCreatedTime());
        contractMap.put("updatedTime", contract.getUpdatedTime());

        // Enrich with tenant info
        try {
            userRepository.findById(contract.getTenantId()).ifPresent(user -> {
                contractMap.put("tenantName", user.getRealName() != null ? user.getRealName() : user.getUsername());
                contractMap.put("tenantPhone", user.getPhone());
            });
        } catch (Exception e) {
            // Ignore if user not found or error
        }

        // Enrich with room info
        try {
            roomInfoRepository.findById(contract.getRoomId()).ifPresent(room -> {
                contractMap.put("province", room.getProvince());
                contractMap.put("city", room.getCity());
                contractMap.put("district", room.getDistrict());
                contractMap.put("street", room.getStreet());
                contractMap.put("communityName", room.getCommunityName());
                contractMap.put("buildingUnit", room.getBuildingUnit());
                contractMap.put("doorNumber", room.getDoorNumber());
                contractMap.put("roomNumber", room.getRoomNumber());
                contractMap.put("roomArea", room.getRoomArea());
                contractMap.put("orientation", room.getOrientation());
                contractMap.put("decoration", room.getDecoration());
                contractMap.put("floorInfo", room.getFloorInfo());
                contractMap.put("hasElevator", room.getHasElevator());
                contractMap.put("rentalType", room.getRentalType());
                contractMap.put("waterPrice", room.getWaterPrice());
                contractMap.put("electricPrice", room.getElectricPrice());
            });
        } catch (Exception e) {
            // Ignore if room not found or error
        }

        return contractMap;
    }

    /**
     * 获取租约趋势（最近7天）
     * 
     * @return 日期和数量列表
     */
    public Map<String, Object> getContractTrends() {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<Object[]> trends = tenantManagementRepository.countByContractSignedTimeAfter(sevenDaysAgo);

            Map<String, Long> trendMap = new HashMap<>();
            // 初始化最近7天的数据为0
            for (int i = 0; i < 7; i++) {
                String date = LocalDate.now().minusDays(i).toString();
                trendMap.put(date, 0L);
            }

            // 填充实际数据
            for (Object[] row : trends) {
                String date = row[0].toString();
                Long count = (Long) row[1];
                trendMap.put(date, count);
            }

            result.put("success", true);
            result.put("trends", trendMap);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取租约趋势失败: " + e.getMessage());
        }
        return result;
    }
}