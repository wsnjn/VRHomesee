package com.example.homesee.controller;

import com.example.homesee.entity.RoomInfo;
import com.example.homesee.entity.TenantManagement;
import com.example.homesee.repository.RoomInfoRepository;
import com.example.homesee.repository.TenantManagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/landlord")
@CrossOrigin(origins = "http://localhost:5173")
public class LandlordHouseController {

    @Autowired
    private RoomInfoRepository roomInfoRepository;

    @Autowired
    private TenantManagementRepository tenantManagementRepository;

    /**
     * 获取房东的所有房屋列表
     * @param landlordPhone 房东手机号
     * @return 房屋列表
     */
    @GetMapping("/houses")
    public Map<String, Object> getLandlordHouses(@RequestParam String landlordPhone) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<RoomInfo> houses = roomInfoRepository.findByLandlordPhoneOrderByCreatedTimeDesc(landlordPhone);
            
            result.put("success", true);
            result.put("message", "获取房屋列表成功");
            result.put("houses", houses);
            result.put("total", houses.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取房屋列表失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 根据状态获取房东的房屋列表
     * @param landlordPhone 房东手机号
     * @param status 房屋状态
     * @return 房屋列表
     */
    @GetMapping("/houses/status/{status}")
    public Map<String, Object> getLandlordHousesByStatus(
            @RequestParam String landlordPhone, 
            @PathVariable Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<RoomInfo> houses = roomInfoRepository.findByLandlordPhoneAndStatusOrderByCreatedTimeDesc(landlordPhone, status);
            
            result.put("success", true);
            result.put("message", "获取房屋列表成功");
            result.put("houses", houses);
            result.put("total", houses.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取房屋列表失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取房东房屋统计信息
     * @param landlordPhone 房东手机号
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getLandlordStatistics(@RequestParam String landlordPhone) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long totalHouses = roomInfoRepository.countByLandlordPhone(landlordPhone);
            long rentedHouses = roomInfoRepository.countByLandlordPhoneAndStatus(landlordPhone, 1); // 已租
            long availableHouses = roomInfoRepository.countByLandlordPhoneAndStatus(landlordPhone, 0); // 可租
            long maintenanceHouses = roomInfoRepository.countByLandlordPhoneAndStatus(landlordPhone, 2); // 下架/维修中
            
            // 计算月收入（这里需要根据实际租金数据计算，暂时返回0）
            double monthlyIncome = 0.0;
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalHouses", totalHouses);
            statistics.put("rentedHouses", rentedHouses);
            statistics.put("availableHouses", availableHouses);
            statistics.put("maintenanceHouses", maintenanceHouses);
            statistics.put("monthlyIncome", monthlyIncome);
            
            result.put("success", true);
            result.put("message", "获取统计信息成功");
            result.put("statistics", statistics);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取房屋详细信息（包含租客信息）
     * @param houseId 房屋ID
     * @return 房屋详细信息
     */
    @GetMapping("/house/{houseId}")
    public Map<String, Object> getHouseDetail(@PathVariable Long houseId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            RoomInfo house = roomInfoRepository.findById(houseId).orElse(null);
            
            if (house == null) {
                result.put("success", false);
                result.put("message", "房屋不存在");
                return result;
            }
            
            // 获取房屋的租客信息
            List<TenantManagement> tenants = tenantManagementRepository.findByRoomIdAndContractStatus(houseId, 1); // 1-已签约
            
            // 构建房屋详情响应
            Map<String, Object> houseDetail = new HashMap<>();
            houseDetail.put("houseInfo", house);
            houseDetail.put("tenants", tenants);
            houseDetail.put("hasTenant", !tenants.isEmpty());
            houseDetail.put("tenantCount", tenants.size());
            
            result.put("success", true);
            result.put("message", "获取房屋详情成功");
            result.put("data", houseDetail);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取房屋详情失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 添加新房屋
     * @param houseData 房屋信息
     * @return 添加结果
     */
    @PostMapping("/house")
    public Map<String, Object> addHouse(@RequestBody Map<String, Object> houseData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            RoomInfo newHouse = new RoomInfo();
            
            // 设置地址信息
            newHouse.setProvince((String) houseData.get("province"));
            newHouse.setCity((String) houseData.get("city"));
            newHouse.setDistrict((String) houseData.get("district"));
            newHouse.setStreet((String) houseData.get("street"));
            newHouse.setCommunityName((String) houseData.get("communityName"));
            newHouse.setBuildingUnit((String) houseData.get("buildingUnit"));
            newHouse.setDoorNumber((String) houseData.get("doorNumber"));
            newHouse.setRoomNumber((String) houseData.get("roomNumber"));
            
            // 设置房屋基本信息
            if (houseData.get("roomArea") != null) {
                newHouse.setRoomArea(new BigDecimal(houseData.get("roomArea").toString()));
            }
            newHouse.setFloorInfo((String) houseData.get("floorInfo"));
            newHouse.setOrientation((String) houseData.get("orientation"));
            
            if (houseData.get("decoration") != null) {
                newHouse.setDecoration(Integer.parseInt(houseData.get("decoration").toString()));
            }
            if (houseData.get("hasElevator") != null) {
                newHouse.setHasElevator(Integer.parseInt(houseData.get("hasElevator").toString()));
            }
            
            // 设置价格信息
            if (houseData.get("rentPrice") != null) {
                newHouse.setRentPrice(new BigDecimal(houseData.get("rentPrice").toString()));
            }
            if (houseData.get("waterPrice") != null) {
                newHouse.setWaterPrice(new BigDecimal(houseData.get("waterPrice").toString()));
            }
            if (houseData.get("electricPrice") != null) {
                newHouse.setElectricPrice(new BigDecimal(houseData.get("electricPrice").toString()));
            }
            
            // 设置租赁信息
            if (houseData.get("rentalType") != null) {
                newHouse.setRentalType(Integer.parseInt(houseData.get("rentalType").toString()));
            }
            if (houseData.get("status") != null) {
                newHouse.setStatus(Integer.parseInt(houseData.get("status").toString()));
            }
            
            // 设置描述信息
            newHouse.setDescription((String) houseData.get("description"));
            
            // 设置房东信息
            newHouse.setLandlordPhone((String) houseData.get("landlordPhone"));
            
            // 保存房屋信息
            RoomInfo savedHouse = roomInfoRepository.save(newHouse);
            
            result.put("success", true);
            result.put("message", "房屋添加成功");
            result.put("houseId", savedHouse.getId());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加房屋失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * 更新房屋状态
     * @param houseId 房屋ID
     * @param status 新状态
     * @return 更新结果
     */
    @PutMapping("/house/{houseId}/status")
    public Map<String, Object> updateHouseStatus(@PathVariable Long houseId, @RequestBody Map<String, Integer> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Integer status = request.get("status");
            RoomInfo house = roomInfoRepository.findById(houseId).orElse(null);
            
            if (house == null) {
                result.put("success", false);
                result.put("message", "房屋不存在");
                return result;
            }
            
            house.setStatus(status);
            roomInfoRepository.save(house);
            
            result.put("success", true);
            result.put("message", "更新房屋状态成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新房屋状态失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新房屋信息
     * @param houseId 房屋ID
     * @param updateData 更新数据
     * @return 更新结果
     */
    @PutMapping("/house/{houseId}")
    public Map<String, Object> updateHouse(@PathVariable Long houseId, @RequestBody Map<String, Object> updateData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            RoomInfo house = roomInfoRepository.findById(houseId).orElse(null);
            
            if (house == null) {
                result.put("success", false);
                result.put("message", "房屋不存在");
                return result;
            }
            
            // 更新租金信息
            if (updateData.get("rentPrice") != null) {
                house.setRentPrice(new BigDecimal(updateData.get("rentPrice").toString()));
            }
            
            // 更新水电费信息
            if (updateData.get("waterPrice") != null) {
                house.setWaterPrice(new BigDecimal(updateData.get("waterPrice").toString()));
            }
            if (updateData.get("electricPrice") != null) {
                house.setElectricPrice(new BigDecimal(updateData.get("electricPrice").toString()));
            }
            
            // 更新房屋状态
            if (updateData.get("status") != null) {
                house.setStatus(Integer.parseInt(updateData.get("status").toString()));
            }
            
            // 更新其他基本信息
            if (updateData.get("roomArea") != null) {
                house.setRoomArea(new BigDecimal(updateData.get("roomArea").toString()));
            }
            if (updateData.get("floorInfo") != null) {
                house.setFloorInfo((String) updateData.get("floorInfo"));
            }
            if (updateData.get("orientation") != null) {
                house.setOrientation((String) updateData.get("orientation"));
            }
            if (updateData.get("decoration") != null) {
                house.setDecoration(Integer.parseInt(updateData.get("decoration").toString()));
            }
            if (updateData.get("hasElevator") != null) {
                house.setHasElevator(Integer.parseInt(updateData.get("hasElevator").toString()));
            }
            if (updateData.get("rentalType") != null) {
                house.setRentalType(Integer.parseInt(updateData.get("rentalType").toString()));
            }
            if (updateData.get("description") != null) {
                house.setDescription((String) updateData.get("description"));
            }
            
            // 保存更新后的房屋信息
            RoomInfo updatedHouse = roomInfoRepository.save(house);
            
            result.put("success", true);
            result.put("message", "房屋信息更新成功");
            result.put("house", updatedHouse);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新房屋信息失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * 获取即将到期的租约
     * @param landlordPhone 房东手机号
     * @return 即将到期的租约列表
     */
    @GetMapping("/expiring-contracts")
    public Map<String, Object> getExpiringContracts(@RequestParam String landlordPhone) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里应该从数据库查询即将到期的租约
            // 由于目前没有租约数据模型，先返回空数组
            List<Map<String, Object>> contracts = new ArrayList<>();
            
            result.put("success", true);
            result.put("message", "获取即将到期租约成功");
            result.put("contracts", contracts);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取即将到期租约失败: " + e.getMessage());
        }
        
        return result;
    }
}
