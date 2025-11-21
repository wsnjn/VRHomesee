package com.example.homesee.controller;

import com.example.homesee.service.TenantManagementService;
import com.example.homesee.repository.UserRepository;
import com.example.homesee.repository.RoomInfoRepository;
import com.example.homesee.entity.User;
import com.example.homesee.entity.RoomInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/landlord/matching")
@CrossOrigin(origins = "http://localhost:5173")
public class LandlordTenantMatchingController {

    @Autowired
    private TenantManagementService tenantManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomInfoRepository roomInfoRepository;

    /**
     * 房东创建租约合同
     * @param request 租约请求数据
     * @param landlordPhone 房东手机号
     * @return 创建结果
     */
    @PostMapping("/create")
    public Map<String, Object> createTenantContract(@RequestBody Map<String, Object> request, 
                                                   @RequestParam String landlordPhone) {
        try {
            // 通过房东手机号获取房东用户信息
            Optional<User> landlordOptional = userRepository.findByPhone(landlordPhone);
            if (landlordOptional.isEmpty()) {
                return Map.of(
                    "success", false,
                    "message", "房东用户信息不存在"
                );
            }
            User landlord = landlordOptional.get();
            Long landlordId = landlord.getId();

            // 解析请求参数
            String contractNumber = (String) request.get("contractNumber");
            Long roomId = Long.valueOf(request.get("roomId").toString());
            Long tenantId = Long.valueOf(request.get("tenantId").toString());
            String contractStartDateStr = (String) request.get("contractStartDate");
            String contractEndDateStr = (String) request.get("contractEndDate");
            Double monthlyRent = Double.valueOf(request.get("monthlyRent").toString());
            Double depositAmount = Double.valueOf(request.get("depositAmount").toString());

            // 验证房屋所有权
            Optional<RoomInfo> roomOptional = roomInfoRepository.findById(roomId);
            if (roomOptional.isEmpty()) {
                return Map.of(
                    "success", false,
                    "message", "房屋信息不存在"
                );
            }
            
            RoomInfo room = roomOptional.get();
            if (!landlordPhone.equals(room.getLandlordPhone())) {
                return Map.of(
                    "success", false,
                    "message", "您没有权限管理此房屋"
                );
            }

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
                "message", "请求参数错误: " + e.getMessage()
            );
        }
    }

    /**
     * 根据房东手机号获取租约列表
     * @param landlordPhone 房东手机号
     * @return 租约列表
     */
    @GetMapping("/contracts")
    public Map<String, Object> getContractsByLandlord(@RequestParam String landlordPhone) {
        try {
            // 通过房东手机号获取房东用户信息
            Optional<User> landlordOptional = userRepository.findByPhone(landlordPhone);
            if (landlordOptional.isEmpty()) {
                return Map.of(
                    "success", false,
                    "message", "房东用户信息不存在"
                );
            }
            User landlord = landlordOptional.get();
            Long landlordId = landlord.getId();

            // 调用服务层获取租约列表
            return tenantManagementService.getContractsByLandlordId(landlordId);
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "获取租约列表失败: " + e.getMessage()
            );
        }
    }

    /**
     * 根据房东手机号和状态获取租约列表
     * @param landlordPhone 房东手机号
     * @param status 状态
     * @return 租约列表
     */
    @GetMapping("/contracts/status/{status}")
    public Map<String, Object> getContractsByLandlordAndStatus(@RequestParam String landlordPhone, 
                                                              @PathVariable Integer status) {
        try {
            // 通过房东手机号获取房东用户信息
            Optional<User> landlordOptional = userRepository.findByPhone(landlordPhone);
            if (landlordOptional.isEmpty()) {
                return Map.of(
                    "success", false,
                    "message", "房东用户信息不存在"
                );
            }
            User landlord = landlordOptional.get();
            Long landlordId = landlord.getId();

            // 调用服务层获取租约列表
            return tenantManagementService.getContractsByLandlordIdAndStatus(landlordId, status);
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "获取租约列表失败: " + e.getMessage()
            );
        }
    }
}
