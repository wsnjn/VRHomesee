/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：MaintenanceRepository.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：报修管理数据访问接口，支持按租约 ID、报修状态或租约 ID 集合进行工单检索，用于房东与租客的维修管理流程
 * 创建日期：2026-01-06
 */
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
