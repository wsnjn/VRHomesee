/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：AdminUserController.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：管理员用户管理控制器，提供用户的增删改查、信息编辑、密码重置及账户状态控制等后端接口
 * 创建日期：2026-01-06
 */
package com.example.homesee.controller;

import com.example.homesee.entity.User;
import com.example.homesee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/user")
@CrossOrigin(origins = {
        "http://39.108.142.250:9999",
        "http://localhost:5173",
        "https://www.homesee.xyz",
        "https://homesee.xyz"
})

public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取所有用户列表
     * 
     * @return 用户列表
     */
    @GetMapping("/all")
    public Map<String, Object> getAllUsers() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<User> users = userRepository.findAll();
            result.put("success", true);
            result.put("users", users);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getUserDetail(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 隐藏敏感信息
                user.setPassword(null);
                result.put("success", true);
                result.put("user", user);
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户详情失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新用户信息 - 支持所有字段更新
     * 
     * @param userId  用户ID
     * @param request 更新数据
     * @return 更新结果
     */
    @PutMapping("/{userId}")
    public Map<String, Object> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // 更新所有字段 - 安全处理null值
                if (request.containsKey("username")) {
                    Object usernameObj = request.get("username");
                    user.setUsername(usernameObj != null ? usernameObj.toString() : null);
                }
                if (request.containsKey("phone")) {
                    Object phoneObj = request.get("phone");
                    user.setPhone(phoneObj != null ? phoneObj.toString() : null);
                }
                if (request.containsKey("email")) {
                    Object emailObj = request.get("email");
                    user.setEmail(emailObj != null ? emailObj.toString() : null);
                }
                if (request.containsKey("password")) {
                    Object passwordObj = request.get("password");
                    user.setPassword(passwordObj != null ? passwordObj.toString() : null);
                }
                if (request.containsKey("userType")) {
                    Object userTypeObj = request.get("userType");
                    if (userTypeObj != null && !userTypeObj.toString().isEmpty()) {
                        user.setUserType(Integer.valueOf(userTypeObj.toString()));
                    }
                }
                if (request.containsKey("realName")) {
                    Object realNameObj = request.get("realName");
                    user.setRealName(realNameObj != null ? realNameObj.toString() : null);
                }
                if (request.containsKey("gender")) {
                    Object genderObj = request.get("gender");
                    if (genderObj != null && !genderObj.toString().isEmpty()) {
                        user.setGender(Integer.valueOf(genderObj.toString()));
                    } else {
                        user.setGender(null);
                    }
                }
                if (request.containsKey("birthday")) {
                    Object birthdayObj = request.get("birthday");
                    if (birthdayObj != null && !birthdayObj.toString().isEmpty()) {
                        user.setBirthday(LocalDateTime.parse(birthdayObj.toString()));
                    } else {
                        user.setBirthday(null);
                    }
                }
                if (request.containsKey("idCard")) {
                    Object idCardObj = request.get("idCard");
                    user.setIdCard(idCardObj != null ? idCardObj.toString() : null);
                }
                if (request.containsKey("avatar")) {
                    Object avatarObj = request.get("avatar");
                    user.setAvatar(avatarObj != null ? avatarObj.toString() : null);
                }
                if (request.containsKey("wechat")) {
                    Object wechatObj = request.get("wechat");
                    user.setWechat(wechatObj != null ? wechatObj.toString() : null);
                }
                if (request.containsKey("qq")) {
                    Object qqObj = request.get("qq");
                    user.setQq(qqObj != null ? qqObj.toString() : null);
                }
                if (request.containsKey("job")) {
                    Object jobObj = request.get("job");
                    user.setJob(jobObj != null ? jobObj.toString() : null);
                }
                if (request.containsKey("company")) {
                    Object companyObj = request.get("company");
                    user.setCompany(companyObj != null ? companyObj.toString() : null);
                }
                if (request.containsKey("monthlyIncome")) {
                    Object incomeObj = request.get("monthlyIncome");
                    if (incomeObj != null && !incomeObj.toString().isEmpty()) {
                        user.setMonthlyIncome(new BigDecimal(incomeObj.toString()));
                    } else {
                        user.setMonthlyIncome(null);
                    }
                }
                if (request.containsKey("rentalBudgetMin")) {
                    Object budgetMinObj = request.get("rentalBudgetMin");
                    if (budgetMinObj != null && !budgetMinObj.toString().isEmpty()) {
                        user.setRentalBudgetMin(new BigDecimal(budgetMinObj.toString()));
                    } else {
                        user.setRentalBudgetMin(null);
                    }
                }
                if (request.containsKey("rentalBudgetMax")) {
                    Object budgetMaxObj = request.get("rentalBudgetMax");
                    if (budgetMaxObj != null && !budgetMaxObj.toString().isEmpty()) {
                        user.setRentalBudgetMax(new BigDecimal(budgetMaxObj.toString()));
                    } else {
                        user.setRentalBudgetMax(null);
                    }
                }
                if (request.containsKey("preferredDistricts")) {
                    Object districtsObj = request.get("preferredDistricts");
                    user.setPreferredDistricts(districtsObj != null ? districtsObj.toString() : null);
                }
                if (request.containsKey("houseRequirements")) {
                    Object requirementsObj = request.get("houseRequirements");
                    user.setHouseRequirements(requirementsObj != null ? requirementsObj.toString() : null);
                }
                if (request.containsKey("status")) {
                    Object statusObj = request.get("status");
                    if (statusObj != null && !statusObj.toString().isEmpty()) {
                        user.setStatus(Integer.valueOf(statusObj.toString()));
                    }
                }
                if (request.containsKey("creditScore")) {
                    Object creditScoreObj = request.get("creditScore");
                    if (creditScoreObj != null && !creditScoreObj.toString().isEmpty()) {
                        user.setCreditScore(Integer.valueOf(creditScoreObj.toString()));
                    } else {
                        user.setCreditScore(null);
                    }
                }
                if (request.containsKey("isCertified")) {
                    Object isCertifiedObj = request.get("isCertified");
                    if (isCertifiedObj != null && !isCertifiedObj.toString().isEmpty()) {
                        user.setIsCertified(Integer.valueOf(isCertifiedObj.toString()));
                    } else {
                        user.setIsCertified(null);
                    }
                }
                if (request.containsKey("certificationTime")) {
                    Object certTimeObj = request.get("certificationTime");
                    if (certTimeObj != null && !certTimeObj.toString().isEmpty()) {
                        user.setCertificationTime(LocalDateTime.parse(certTimeObj.toString()));
                    } else {
                        user.setCertificationTime(null);
                    }
                }

                user.setUpdatedTime(LocalDateTime.now());
                userRepository.save(user);

                result.put("success", true);
                result.put("message", "用户信息更新成功");
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新用户信息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> result = new HashMap<>();

        try {
            long totalUsers = userRepository.count();
            long landlordCount = userRepository.countByUserType(1); // 房东
            long tenantCount = userRepository.countByUserType(2); // 租客
            long activeUsers = userRepository.countByStatus(1); // 活跃用户

            result.put("success", true);
            result.put("totalUsers", totalUsers);
            result.put("landlordCount", landlordCount);
            result.put("tenantCount", tenantCount);
            result.put("activeUsers", activeUsers);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户统计失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据用户类型获取用户列表
     * 
     * @param userType 用户类型
     * @return 用户列表
     */
    @GetMapping("/type/{userType}")
    public Map<String, Object> getUsersByType(@PathVariable Integer userType) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<User> users = userRepository.findByUserType(userType);
            // 隐藏密码信息
            users.forEach(user -> user.setPassword(null));
            result.put("success", true);
            result.put("users", users);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据状态获取用户列表
     * 
     * @param status 状态
     * @return 用户列表
     */
    @GetMapping("/status/{status}")
    public Map<String, Object> getUsersByStatus(@PathVariable Integer status) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<User> users = userRepository.findByStatus(status);
            // 隐藏密码信息
            users.forEach(user -> user.setPassword(null));
            result.put("success", true);
            result.put("users", users);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新用户状态
     * 
     * @param userId  用户ID
     * @param request 状态更新请求
     * @return 更新结果
     */
    @PutMapping("/{userId}/status")
    public Map<String, Object> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Integer status = Integer.valueOf(request.get("status").toString());
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setStatus(status);
                user.setUpdatedTime(LocalDateTime.now());

                userRepository.save(user);

                result.put("success", true);
                result.put("message", "用户状态更新成功");
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新用户状态失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 搜索用户
     * 
     * @param keyword 关键词
     * @return 用户列表
     */
    @GetMapping("/search")
    public Map<String, Object> searchUsers(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<User> users = userRepository
                    .findByUsernameContainingOrRealNameContainingOrPhoneContainingOrEmailContaining(
                            keyword, keyword, keyword, keyword);
            // 隐藏密码信息
            users.forEach(user -> user.setPassword(null));
            result.put("success", true);
            result.put("users", users);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "搜索用户失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 创建用户（管理员创建）
     * 
     * @param request 用户数据
     * @return 创建结果
     */
    @PostMapping("/create")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查用户名是否已存在
            String username = (String) request.get("username");
            Optional<User> existingUserOptional = userRepository.findByUsername(username);
            if (existingUserOptional.isPresent()) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }

            // 创建新用户
            User user = new User();
            user.setUsername(username);
            user.setPassword((String) request.get("password")); // 实际项目中应该加密
            user.setPhone((String) request.get("phone"));
            user.setEmail((String) request.get("email"));
            user.setRealName((String) request.get("realName"));
            user.setIdCard((String) request.get("idCard"));
            user.setUserType(Integer.valueOf(request.get("userType").toString()));
            user.setStatus(1); // 默认激活状态

            User savedUser = userRepository.save(user);

            result.put("success", true);
            result.put("message", "用户创建成功");
            result.put("userId", savedUser.getId());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建用户失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 重置用户密码为默认密码 "123456"
     * 
     * @param userId 用户ID
     * @return 重置结果
     */
    @PutMapping("/{userId}/reset-password")
    public Map<String, Object> resetPassword(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 重置密码为 "123456"
                user.setPassword("e10adc3949ba59abbe56e057f20f883e");
                user.setUpdatedTime(LocalDateTime.now());

                userRepository.save(user);

                result.put("success", true);
                result.put("message", "用户密码已重置为 123456");
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重置密码失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{userId}")
    public Map<String, Object> deleteUser(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                userRepository.delete(user);

                result.put("success", true);
                result.put("message", "用户删除成功");
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除用户失败: " + e.getMessage());
        }

        return result;
    }
}
