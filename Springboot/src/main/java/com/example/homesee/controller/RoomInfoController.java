/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：RoomInfoController.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：房源信息管理控制器，提供房源的增删改查、状态管理、价格更新以及城市分布统计等功能
 * 创建日期：2026-01-06
 */
package com.example.homesee.controller;

import com.example.homesee.entity.RoomInfo;
import com.example.homesee.repository.RoomInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomInfoController {

    @Autowired
    private RoomInfoRepository roomInfoRepository;

    // 获取所有房屋列表
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllRooms() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<RoomInfo> rooms = roomInfoRepository.findAll();
            response.put("success", true);
            response.put("rooms", rooms);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取房屋列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取待出租房屋列表（status = 0）
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableRooms() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<RoomInfo> availableRooms = roomInfoRepository.findByStatus(0);
            response.put("success", true);
            response.put("rooms", availableRooms);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取待出租房屋列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 根据ID获取房屋详情
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoomById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomInfo room = roomInfoRepository.findById(id).orElse(null);
            if (room != null) {
                response.put("success", true);
                response.put("room", room);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取房屋详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 更新房屋状态
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateRoomStatus(@PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomInfo room = roomInfoRepository.findById(id).orElse(null);
            if (room != null) {
                Integer status = (Integer) request.get("status");
                if (status != null) {
                    room.setStatus(status);
                    roomInfoRepository.save(room);
                    response.put("success", true);
                    response.put("message", "房屋状态更新成功");
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "状态不能为空");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新房屋状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取房屋统计信息
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRoomStatistics() {
        Map<String, Object> response = new HashMap<>();
        try {
            long totalRooms = roomInfoRepository.count();
            long availableRooms = roomInfoRepository.countByStatus(0);
            long rentedRooms = roomInfoRepository.countByStatus(1);
            long maintenanceRooms = roomInfoRepository.countByStatus(2);

            response.put("success", true);
            response.put("totalHouses", totalRooms);
            response.put("availableHouses", availableRooms);
            response.put("rentedHouses", rentedRooms);
            response.put("maintenanceHouses", maintenanceRooms);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取房屋统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取房屋数量统计（兼容前端调用）
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getRoomCount() {
        Map<String, Object> response = new HashMap<>();
        try {
            long totalRooms = roomInfoRepository.count();
            long availableRooms = roomInfoRepository.countByStatus(0);
            long rentedRooms = roomInfoRepository.countByStatus(1);
            long maintenanceRooms = roomInfoRepository.countByStatus(2);

            response.put("success", true);
            response.put("total", totalRooms);
            response.put("available", availableRooms);
            response.put("rented", rentedRooms);
            response.put("maintenance", maintenanceRooms);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取房屋数量统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 添加新房屋
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addRoom(@RequestBody RoomInfo roomInfo) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 设置创建和更新时间
            roomInfo.setCreatedTime(java.time.LocalDateTime.now());
            roomInfo.setUpdatedTime(java.time.LocalDateTime.now());

            RoomInfo savedRoom = roomInfoRepository.save(roomInfo);
            response.put("success", true);
            response.put("message", "房屋添加成功");
            response.put("room", savedRoom);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加房屋失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 更新房屋信息
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateRoom(@PathVariable Long id, @RequestBody RoomInfo roomInfo) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomInfo existingRoom = roomInfoRepository.findById(id).orElse(null);
            if (existingRoom != null) {
                // 更新字段
                existingRoom.setProvince(roomInfo.getProvince());
                existingRoom.setCity(roomInfo.getCity());
                existingRoom.setDistrict(roomInfo.getDistrict());
                existingRoom.setStreet(roomInfo.getStreet());
                existingRoom.setCommunityName(roomInfo.getCommunityName());
                existingRoom.setBuildingUnit(roomInfo.getBuildingUnit());
                existingRoom.setDoorNumber(roomInfo.getDoorNumber());
                existingRoom.setRoomNumber(roomInfo.getRoomNumber());
                existingRoom.setHasElevator(roomInfo.getHasElevator());
                existingRoom.setRoomArea(roomInfo.getRoomArea());
                existingRoom.setFloorInfo(roomInfo.getFloorInfo());
                existingRoom.setOrientation(roomInfo.getOrientation());
                existingRoom.setDecoration(roomInfo.getDecoration());
                existingRoom.setRentPrice(roomInfo.getRentPrice());
                existingRoom.setWaterPrice(roomInfo.getWaterPrice());
                existingRoom.setElectricPrice(roomInfo.getElectricPrice());
                existingRoom.setRentalType(roomInfo.getRentalType());
                existingRoom.setLandlordPhone(roomInfo.getLandlordPhone());
                existingRoom.setStatus(roomInfo.getStatus());
                existingRoom.setDescription(roomInfo.getDescription());
                existingRoom.setLatitude(roomInfo.getLatitude());
                existingRoom.setLongitude(roomInfo.getLongitude());
                existingRoom.setUpdatedTime(java.time.LocalDateTime.now());

                RoomInfo updatedRoom = roomInfoRepository.save(existingRoom);
                response.put("success", true);
                response.put("message", "房屋信息更新成功");
                response.put("room", updatedRoom);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新房屋信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 删除房屋
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoom(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomInfo existingRoom = roomInfoRepository.findById(id).orElse(null);
            if (existingRoom != null) {
                roomInfoRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "房屋删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除房屋失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 更新房屋价格
    @PutMapping("/{id}/price")
    public ResponseEntity<Map<String, Object>> updateRoomPrice(@PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomInfo room = roomInfoRepository.findById(id).orElse(null);
            if (room != null) {
                BigDecimal rentPrice = new BigDecimal(request.get("rentPrice").toString());
                if (rentPrice != null && rentPrice.compareTo(BigDecimal.ZERO) > 0) {
                    room.setRentPrice(rentPrice);
                    room.setUpdatedTime(java.time.LocalDateTime.now());
                    roomInfoRepository.save(room);
                    response.put("success", true);
                    response.put("message", "房屋价格更新成功");
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "租金价格必须大于0");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新房屋价格失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 批量更新房屋状态
    @PutMapping("/batch/status")
    public ResponseEntity<Map<String, Object>> batchUpdateRoomStatus(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> ids = (List<Long>) request.get("ids");
            Integer status = (Integer) request.get("status");

            if (ids != null && !ids.isEmpty() && status != null) {
                List<RoomInfo> rooms = roomInfoRepository.findAllById(ids);
                for (RoomInfo room : rooms) {
                    room.setStatus(status);
                    room.setUpdatedTime(java.time.LocalDateTime.now());
                }
                roomInfoRepository.saveAll(rooms);
                response.put("success", true);
                response.put("message", "批量更新房屋状态成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "参数不完整");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量更新房屋状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取房屋城市分布
    @GetMapping("/distribution/city")
    public ResponseEntity<Map<String, Object>> getRoomCityDistribution() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Object[]> distribution = roomInfoRepository.countByCity();
            Map<String, Long> cityMap = new HashMap<>();

            for (Object[] row : distribution) {
                String city = (String) row[0];
                Long count = (Long) row[1];
                cityMap.put(city, count);
            }

            response.put("success", true);
            response.put("distribution", cityMap);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取城市分布失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
