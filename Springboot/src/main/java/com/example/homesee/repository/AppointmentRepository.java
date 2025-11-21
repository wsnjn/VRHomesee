package com.example.homesee.repository;

import com.example.homesee.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    /**
     * 根据用户ID查找预约记录
     * @param userId 用户ID
     * @return 预约列表
     */
    List<Appointment> findByUserId(Long userId);
    
    /**
     * 根据房屋ID查找预约记录
     * @param houseId 房屋ID
     * @return 预约列表
     */
    List<Appointment> findByHouseId(Long houseId);
    
    /**
     * 查找时间冲突的预约记录
     * @param houseId 房屋ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 冲突的预约列表
     */
    @Query("SELECT a FROM Appointment a WHERE a.houseId = :houseId " +
           "AND a.appointmentDateTime BETWEEN :startTime AND :endTime " +
           "AND a.status IN (0, 1)") // 只检查待确认和已确认的预约
    List<Appointment> findConflictingAppointments(@Param("houseId") Long houseId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据状态查找预约记录
     * @param status 状态
     * @return 预约列表
     */
    List<Appointment> findByStatus(Integer status);
    
    /**
     * 根据房屋ID和状态查找预约记录
     * @param houseId 房屋ID
     * @param status 状态
     * @return 预约列表
     */
    List<Appointment> findByHouseIdAndStatus(Long houseId, Integer status);
}
