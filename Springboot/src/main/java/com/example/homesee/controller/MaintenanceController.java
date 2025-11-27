package com.example.homesee.controller;

import com.example.homesee.entity.MaintenanceRequest;
import com.example.homesee.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createRequest(@RequestBody MaintenanceRequest request) {
        MaintenanceRequest created = maintenanceService.createRequest(request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", created);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list/{leaseId}")
    public ResponseEntity<Map<String, Object>> listByLease(@PathVariable Long leaseId) {
        List<MaintenanceRequest> list = maintenanceService.getRequestsByLeaseId(leaseId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateRequest(@RequestBody MaintenanceRequest request) {
        MaintenanceRequest updated = maintenanceService.updateRequest(request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", updated);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active-lease/{userId}")
    public ResponseEntity<Map<String, Object>> getActiveLease(@PathVariable Long userId) {
        Map<String, Object> result = maintenanceService.getActiveLeaseByUserId(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<Map<String, Object>> getRequestsByLandlordId(@PathVariable Long landlordId) {
        List<Map<String, Object>> list = maintenanceService.getRequestsByLandlordId(landlordId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }
}
