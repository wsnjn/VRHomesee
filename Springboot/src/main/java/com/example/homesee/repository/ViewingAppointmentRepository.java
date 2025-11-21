package com.example.homesee.repository;

import com.example.homesee.entity.ViewingAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ViewingAppointmentRepository extends JpaRepository<ViewingAppointment, Long> {
    
    // 根据预约编号查找
    Optional<ViewingAppointment> findByAppointmentNumber(String appointmentNumber);
    
    // 根据用户ID查找预约列表
    List<ViewingAppointment> findByUserId(Long userId);
    
    // 根据房间ID查找预约列表
    List<ViewingAppointment> findByRoomId(Long roomId);
    
    // 根据状态查找预约列表
    List<ViewingAppointment> findByStatus(Integer status);
    
    // 根据业务员ID查找预约列表
    List<ViewingAppointment> findByAssignedAgentId(Long assignedAgentId);
    
    // 查找需要跟进的预约
    List<ViewingAppointment> findByFollowUpRequiredTrue();
    
    // 查找特定日期的预约
    @Query("SELECT va FROM ViewingAppointment va WHERE va.preferredDate = :date")
    List<ViewingAppointment> findByPreferredDate(@Param("date") LocalDate date);
    
    // 查找特定日期和时间段的预约
    @Query("SELECT va FROM ViewingAppointment va WHERE va.preferredDate = :date AND va.preferredTimeSlot = :timeSlot")
    List<ViewingAppointment> findByPreferredDateAndTimeSlot(@Param("date") LocalDate date, @Param("timeSlot") String timeSlot);
    
    // 查找时间冲突的预约（同一房间在同一时间段）
    @Query("SELECT va FROM ViewingAppointment va WHERE va.roomId = :roomId AND va.preferredDate = :date AND va.preferredTimeSlot = :timeSlot AND va.status IN (0, 1)")
    List<ViewingAppointment> findConflictingAppointments(@Param("roomId") Long roomId, @Param("date") LocalDate date, @Param("timeSlot") String timeSlot);
    
    // 查找过期的预约（状态为待确认或已确认，但预约日期已过）
    @Query("SELECT va FROM ViewingAppointment va WHERE va.status IN (0, 1) AND va.preferredDate < :today")
    List<ViewingAppointment> findExpiredAppointments(@Param("today") LocalDate today);
    
    // 统计用户的总预约数量
    @Query("SELECT COUNT(va) FROM ViewingAppointment va WHERE va.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    // 统计房间的总预约数量
    @Query("SELECT COUNT(va) FROM ViewingAppointment va WHERE va.roomId = :roomId")
    Long countByRoomId(@Param("roomId") Long roomId);
    
    // 查找最近创建的预约
    @Query("SELECT va FROM ViewingAppointment va ORDER BY va.appointmentCreatedTime DESC")
    List<ViewingAppointment> findRecentAppointments();
}
