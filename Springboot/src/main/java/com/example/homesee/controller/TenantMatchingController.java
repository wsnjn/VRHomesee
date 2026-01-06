/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：TenantMatchingController.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：管理员租客匹配控制器，处理房源选择、租客匹配以及租赁合同的创建与管理逻辑
 * 创建日期：2026-01-06
 */
package com.example.homesee.controller;

import com.example.homesee.entity.TenantManagement;
import com.example.homesee.entity.RoomInfo;
import com.example.homesee.entity.User;
import com.example.homesee.repository.TenantManagementRepository;
import com.example.homesee.repository.RoomInfoRepository;
import com.example.homesee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/matching")
@CrossOrigin(origins = "http://39.108.142.250:9999,http://localhost:5173", allowCredentials = "true")
public class TenantMatchingController {

    @Autowired
    private TenantManagementRepository tenantManagementRepository;

    @Autowired
    private RoomInfoRepository roomInfoRepository;

    @Autowired
    private UserRepository userRepository;

    // 获取所有房屋列表（供前端显示）
    @GetMapping("/houses")
    public ResponseEntity<Map<String, Object>> getAllHouses() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<RoomInfo> rooms = roomInfoRepository.findAll();

            // 转换为前端需要的格式
            List<Map<String, Object>> houseList = rooms.stream().map(room -> {
                Map<String, Object> houseMap = new HashMap<>();
                houseMap.put("id", room.getId());
                houseMap.put("roomName", room.getCommunityName() + " " + room.getDoorNumber()
                        + (room.getRoomNumber() != null ? "-" + room.getRoomNumber() : ""));
                houseMap.put("address", room.getProvince() + room.getCity() + room.getDistrict() + room.getStreet()
                        + room.getCommunityName());
                houseMap.put("area", room.getRoomArea());
                houseMap.put("monthlyRent", room.getRentPrice());
                houseMap.put("landlordId", getUserIdByPhone(room.getLandlordPhone()));
                houseMap.put("status", room.getStatus());
                houseMap.put("province", room.getProvince());
                houseMap.put("city", room.getCity());
                houseMap.put("district", room.getDistrict());
                houseMap.put("street", room.getStreet());
                houseMap.put("communityName", room.getCommunityName());
                houseMap.put("doorNumber", room.getDoorNumber());
                houseMap.put("roomNumber", room.getRoomNumber());
                houseMap.put("rentPrice", room.getRentPrice());
                houseMap.put("landlordPhone", room.getLandlordPhone());
                return houseMap;
            }).collect(Collectors.toList());

            response.put("success", true);
            response.put("houses", houseList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取房屋列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 根据用户手机号获取用户ID
    private Long getUserIdByPhone(String phone) {
        Optional<User> user = userRepository.findByPhone(phone);
        return user.isPresent() ? user.get().getId() : null;
    }

    // 创建租赁合同
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTenantContract(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 调试：打印接收到的请求数据
            System.out.println("=== 接收到的请求数据 ===");
            for (Map.Entry<String, Object> entry : request.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("=====================");

            // 简化验证：只验证最核心的字段
            if (!request.containsKey("contractNumber") || request.get("contractNumber") == null
                    || request.get("contractNumber").toString().isEmpty()) {
                response.put("success", false);
                response.put("message", "合同编号不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (!request.containsKey("roomId") || request.get("roomId") == null) {
                response.put("success", false);
                response.put("message", "房屋ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (!request.containsKey("tenantId") || request.get("tenantId") == null) {
                response.put("success", false);
                response.put("message", "租客ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查房屋是否存在
            Long roomId = Long.valueOf(request.get("roomId").toString());
            RoomInfo room = roomInfoRepository.findById(roomId).orElse(null);
            if (room == null) {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查租客是否存在
            Long tenantId = Long.valueOf(request.get("tenantId").toString());
            User tenant = userRepository.findById(tenantId).orElse(null);
            if (tenant == null) {
                response.put("success", false);
                response.put("message", "租客不存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 打印选择的租客信息
            System.out.println("=== 选择的租客信息 ===");
            System.out.println("租客ID: " + tenant.getId());
            System.out.println("租客姓名: " + (tenant.getRealName() != null ? tenant.getRealName() : tenant.getUsername()));
            System.out.println("租客手机: " + tenant.getPhone());
            System.out.println("租客类型: " + tenant.getUserType());
            System.out.println("=====================");

            // 检查合同编号是否已存在
            String contractNumber = request.get("contractNumber").toString();
            if (tenantManagementRepository.existsByContractNumber(contractNumber)) {
                response.put("success", false);
                response.put("message", "合同编号已存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 创建租赁合同
            TenantManagement tenantManagement = new TenantManagement();
            tenantManagement.setContractNumber(contractNumber);
            tenantManagement.setRoomId(roomId);

            // 自动获取房东ID（从房屋信息中）
            Long landlordId = getUserIdByPhone(room.getLandlordPhone());
            if (landlordId == null) {
                response.put("success", false);
                response.put("message", "无法找到对应的房东信息，请确保用户表中存在手机号为 " + room.getLandlordPhone() + " 的房东用户");
                return ResponseEntity.badRequest().body(response);
            }
            tenantManagement.setLandlordId(landlordId);

            // 打印选择的房东信息
            System.out.println("=== 选择的房东信息 ===");
            System.out.println("房东ID: " + landlordId);
            System.out.println("房东手机: " + room.getLandlordPhone());
            System.out.println("=====================");

            tenantManagement.setTenantId(Long.valueOf(request.get("tenantId").toString()));

            // 设置日期（使用默认值或提供的值）
            if (request.containsKey("contractStartDate") && request.get("contractStartDate") != null
                    && !request.get("contractStartDate").toString().isEmpty()) {
                tenantManagement.setContractStartDate(LocalDate.parse(request.get("contractStartDate").toString()));
            } else {
                tenantManagement.setContractStartDate(LocalDate.now());
            }

            if (request.containsKey("contractEndDate") && request.get("contractEndDate") != null
                    && !request.get("contractEndDate").toString().isEmpty()) {
                tenantManagement.setContractEndDate(LocalDate.parse(request.get("contractEndDate").toString()));
            } else {
                tenantManagement.setContractEndDate(LocalDate.now().plusYears(1));
            }

            // 设置金额（使用默认值或提供的值）
            if (request.containsKey("monthlyRent") && request.get("monthlyRent") != null) {
                tenantManagement.setMonthlyRent(new BigDecimal(request.get("monthlyRent").toString()));
            } else {
                tenantManagement.setMonthlyRent(new BigDecimal("0"));
            }

            if (request.containsKey("depositAmount") && request.get("depositAmount") != null) {
                tenantManagement.setDepositAmount(new BigDecimal(request.get("depositAmount").toString()));
            } else {
                tenantManagement.setDepositAmount(new BigDecimal("0"));
            }

            // 设置付款周期（使用默认值或提供的值）
            if (request.containsKey("paymentCycle") && request.get("paymentCycle") != null
                    && !request.get("paymentCycle").toString().isEmpty()) {
                tenantManagement.setPaymentCycle(Integer.valueOf(request.get("paymentCycle").toString()));
            } else {
                tenantManagement.setPaymentCycle(1); // 默认月付
            }

            // 设置默认状态
            tenantManagement.setContractStatus(0); // 待签约
            tenantManagement.setRentStatus(0); // 未付款
            tenantManagement.setDepositStatus(0); // 未付

            // 设置时间戳
            tenantManagement.setCreatedTime(LocalDateTime.now());
            tenantManagement.setUpdatedTime(LocalDateTime.now());

            // 保存租赁合同
            TenantManagement savedContract = tenantManagementRepository.save(tenantManagement);

            // 更新房屋状态为已出租

            room.setStatus(1); // 已出租
            roomInfoRepository.save(room);

            response.put("success", true);
            response.put("message", "租赁合同创建成功");
            response.put("contract", savedContract);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建合同失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}