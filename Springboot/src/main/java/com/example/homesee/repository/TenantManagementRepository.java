package com.example.homesee.repository;

import com.example.homesee.entity.TenantManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TenantManagementRepository extends JpaRepository<TenantManagement, Long> {
    
    /**
     * 根据合同编号查找租约
     * @param contractNumber 合同编号
     * @return 租约信息
     */
    TenantManagement findByContractNumber(String contractNumber);
    
    /**
     * 根据房屋ID查找租约
     * @param roomId 房屋ID
     * @return 租约列表
     */
    List<TenantManagement> findByRoomId(Long roomId);
    
    /**
     * 根据房东ID查找租约
     * @param landlordId 房东ID
     * @return 租约列表
     */
    List<TenantManagement> findByLandlordId(Long landlordId);
    
    /**
     * 根据租客ID查找租约
     * @param tenantId 租客ID
     * @return 租约列表
     */
    List<TenantManagement> findByTenantId(Long tenantId);
    
    /**
     * 根据合同状态查找租约
     * @param contractStatus 合同状态
     * @return 租约列表
     */
    List<TenantManagement> findByContractStatus(Integer contractStatus);
    
    /**
     * 查找即将到期的租约
     * @param endDate 截止日期
     * @return 即将到期的租约列表
     */
    @Query("SELECT t FROM TenantManagement t WHERE t.contractEndDate <= :endDate AND t.contractStatus IN (1, 2)")
    List<TenantManagement> findExpiringContracts(@Param("endDate") LocalDate endDate);
    
    /**
     * 查找活跃的租约（已签约或履行中）
     * @return 活跃租约列表
     */
    @Query("SELECT t FROM TenantManagement t WHERE t.contractStatus IN (1, 2)")
    List<TenantManagement> findActiveContracts();
    
    /**
     * 根据房屋ID和合同状态查找租约
     * @param roomId 房屋ID
     * @param contractStatus 合同状态
     * @return 租约列表
     */
    List<TenantManagement> findByRoomIdAndContractStatus(Long roomId, Integer contractStatus);
    
    /**
     * 根据房东ID和合同状态查找租约
     * @param landlordId 房东ID
     * @param contractStatus 合同状态
     * @return 租约列表
     */
    List<TenantManagement> findByLandlordIdAndContractStatus(Long landlordId, Integer contractStatus);
    
    /**
     * 统计各状态的租约数量
     * @return 状态统计
     */
    @Query("SELECT t.contractStatus, COUNT(t) FROM TenantManagement t GROUP BY t.contractStatus")
    List<Object[]> countByContractStatus();
    
    /**
     * 检查合同编号是否已存在
     * @param contractNumber 合同编号
     * @return 是否存在
     */
    boolean existsByContractNumber(String contractNumber);
}
