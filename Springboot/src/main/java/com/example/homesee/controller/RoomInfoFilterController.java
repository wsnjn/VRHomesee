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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/room-info")
public class RoomInfoFilterController {

    @Autowired
    private RoomInfoRepository roomInfoRepository;

    // 获取筛选选项
    @GetMapping("/filter-options")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取所有省份列表
            List<String> provinces = roomInfoRepository.findDistinctProvinces();
            
            // 租赁类型映射
            Map<String, String> rentalTypes = new HashMap<>();
            rentalTypes.put("0", "整租");
            rentalTypes.put("1", "合租");
            rentalTypes.put("2", "单间");
            
            // 装修程度映射
            Map<String, String> decorationTypes = new HashMap<>();
            decorationTypes.put("1", "毛坯");
            decorationTypes.put("2", "简装");
            decorationTypes.put("3", "精装");
            decorationTypes.put("4", "豪装");
            
            // 电梯选项
            Map<String, String> elevatorOptions = new HashMap<>();
            elevatorOptions.put("0", "无电梯");
            elevatorOptions.put("1", "有电梯");
            
            // 朝向选项
            Map<String, String> orientationOptions = new HashMap<>();
            orientationOptions.put("南", "南");
            orientationOptions.put("北", "北");
            orientationOptions.put("东", "东");
            orientationOptions.put("西", "西");
            orientationOptions.put("南北", "南北");
            orientationOptions.put("东西", "东西");
            orientationOptions.put("东南", "东南");
            orientationOptions.put("西南", "西南");
            orientationOptions.put("东北", "东北");
            orientationOptions.put("西北", "西北");
            
            response.put("provinces", provinces);
            response.put("rentalTypes", rentalTypes);
            response.put("decorationTypes", decorationTypes);
            response.put("elevatorOptions", elevatorOptions);
            response.put("orientationOptions", orientationOptions);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取筛选选项失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 根据省份获取城市列表
    @GetMapping("/cities/{province}")
    public ResponseEntity<List<String>> getCitiesByProvince(@PathVariable String province) {
        try {
            List<String> cities = roomInfoRepository.findDistinctCitiesByProvince(province);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 根据城市获取区县列表
    @GetMapping("/districts/{city}")
    public ResponseEntity<List<String>> getDistrictsByCity(@PathVariable String city) {
        try {
            List<String> districts = roomInfoRepository.findDistinctDistrictsByCity(city);
            return ResponseEntity.ok(districts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 根据区县获取街道列表
    @GetMapping("/streets/{district}")
    public ResponseEntity<List<String>> getStreetsByDistrict(@PathVariable String district) {
        try {
            List<String> streets = roomInfoRepository.findDistinctStreetsByDistrict(district);
            return ResponseEntity.ok(streets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 根据街道获取小区列表
    @GetMapping("/communities/{street}")
    public ResponseEntity<List<String>> getCommunitiesByStreet(@PathVariable String street) {
        try {
            List<String> communities = roomInfoRepository.findDistinctCommunitiesByStreet(street);
            return ResponseEntity.ok(communities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
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

    // 筛选房屋
    @GetMapping("/filter")
    public ResponseEntity<List<RoomInfo>> filterRooms(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String street,
            @RequestParam(required = false) String communityName,
            @RequestParam(required = false) Integer rentalType,
            @RequestParam(required = false) Integer decoration,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea) {
        
        try {
            List<RoomInfo> rooms = roomInfoRepository.findAll();
            
            // 过滤状态为0（在租）的房屋
            rooms = rooms.stream()
                    .filter(room -> room.getStatus() == 0)
                    .collect(Collectors.toList());
            
            // 根据筛选条件过滤
            if (province != null && !province.isEmpty()) {
                rooms = rooms.stream()
                        .filter(room -> province.equals(room.getProvince()))
                        .collect(Collectors.toList());
            }
            
            if (city != null && !city.isEmpty()) {
                rooms = rooms.stream()
                        .filter(room -> city.equals(room.getCity()))
                        .collect(Collectors.toList());
            }
            
            if (district != null && !district.isEmpty()) {
                rooms = rooms.stream()
                        .filter(room -> district.equals(room.getDistrict()))
                        .collect(Collectors.toList());
            }
            
            if (street != null && !street.isEmpty()) {
                rooms = rooms.stream()
                        .filter(room -> street.equals(room.getStreet()))
                        .collect(Collectors.toList());
            }
            
            if (communityName != null && !communityName.isEmpty()) {
                rooms = rooms.stream()
                        .filter(room -> communityName.equals(room.getCommunityName()))
                        .collect(Collectors.toList());
            }
            
            if (rentalType != null) {
                rooms = rooms.stream()
                        .filter(room -> rentalType.equals(room.getRentalType()))
                        .collect(Collectors.toList());
            }
            
            if (decoration != null) {
                rooms = rooms.stream()
                        .filter(room -> decoration.equals(room.getDecoration()))
                        .collect(Collectors.toList());
            }
            
            if (minPrice != null) {
                rooms = rooms.stream()
                        .filter(room -> room.getRentPrice().compareTo(BigDecimal.valueOf(minPrice)) >= 0)
                        .collect(Collectors.toList());
            }
            
            if (maxPrice != null) {
                rooms = rooms.stream()
                        .filter(room -> room.getRentPrice().compareTo(BigDecimal.valueOf(maxPrice)) <= 0)
                        .collect(Collectors.toList());
            }
            
            if (minArea != null) {
                rooms = rooms.stream()
                        .filter(room -> room.getRoomArea() != null && 
                                room.getRoomArea().compareTo(BigDecimal.valueOf(minArea)) >= 0)
                        .collect(Collectors.toList());
            }
            
            if (maxArea != null) {
                rooms = rooms.stream()
                        .filter(room -> room.getRoomArea() != null && 
                                room.getRoomArea().compareTo(BigDecimal.valueOf(maxArea)) <= 0)
                        .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
