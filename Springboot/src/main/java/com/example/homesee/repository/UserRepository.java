package com.example.homesee.repository;

import com.example.homesee.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 根据手机号查找用户
    Optional<User> findByPhone(String phone);
    
    // 根据用户名查找用户
    Optional<User> findByUsername(String username);
    
    // 根据邮箱查找用户
    Optional<User> findByEmail(String email);
    
    // 检查手机号是否已存在
    boolean existsByPhone(String phone);
    
    // 检查用户名是否已存在
    boolean existsByUsername(String username);
    
    // 检查邮箱是否已存在
    boolean existsByEmail(String email);
    
    // 根据手机号和密码查找用户（用于登录验证）
    @Query("SELECT u FROM User u WHERE u.phone = :phone AND u.password = :password AND u.status = 1")
    Optional<User> findByPhoneAndPassword(@Param("phone") String phone, @Param("password") String password);
    
    // 根据用户类型统计数量
    long countByUserType(Integer userType);
    
    // 根据状态统计数量
    long countByStatus(Integer status);
    
    // 根据用户类型查找用户
    List<User> findByUserType(Integer userType);
    
    // 根据状态查找用户
    List<User> findByStatus(Integer status);
    
    // 多字段搜索用户
    List<User> findByUsernameContainingOrRealNameContainingOrPhoneContainingOrEmailContaining(
        String username, String realName, String phone, String email);
}
