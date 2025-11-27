package com.example.homesee.repository;

import com.example.homesee.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceRequest, Long> {
    List<MaintenanceRequest> findByTenantManagementId(Long tenantManagementId);

    List<MaintenanceRequest> findByRequestStatus(Integer requestStatus);

    List<MaintenanceRequest> findByTenantManagementIdIn(List<Long> tenantManagementIds);
}
