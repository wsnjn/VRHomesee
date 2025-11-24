package com.example.homesee.service;

import com.example.homesee.entity.User;
import com.example.homesee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityService communityService;

    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param phone    手机号
     * @param password 密码
     * @param realName 真实姓名
     * @return 注册结果
     */
    public Map<String, Object> register(String username, String phone, String password, String realName) {
        Map<String, Object> result = new HashMap<>();

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        // 检查手机号是否已存在
        if (userRepository.existsByPhone(phone)) {
            result.put("success", false);
            result.put("message", "手机号已注册");
            return result;
        }

        // 密码加密（这里使用MD5加密，生产环境建议使用更安全的加密方式）
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());

        // 创建新用户
        User user = new User(username, phone, encryptedPassword, realName);
        user.setRegisterTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        try {
            User savedUser = userRepository.save(user);

            // 自动加入全局群组
            try {
                var globalGroups = communityService.getGlobalGroups();
                if (!globalGroups.isEmpty()) {
                    communityService.joinGroup(globalGroups.get(0).getId(), savedUser.getId(), 0);
                }
            } catch (Exception e) {
                System.err.println("Auto-join global group failed: " + e.getMessage());
            }

            result.put("success", true);
            result.put("message", "注册成功");
            result.put("userId", savedUser.getId());
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "注册失败，请稍后重试");
            return result;
        }
    }

    /**
     * 用户登录
     * 
     * @param phone    手机号
     * @param password 密码
     * @return 登录结果
     */
    public Map<String, Object> login(String phone, String password) {
        Map<String, Object> result = new HashMap<>();

        // 密码加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());

        // 查找用户
        Optional<User> userOptional = userRepository.findByPhoneAndPassword(phone, encryptedPassword);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);

            result.put("success", true);
            result.put("message", "登录成功");
            result.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "phone", user.getPhone(),
                    "realName", user.getRealName(),
                    "userType", user.getUserType()));
        } else {
            result.put("success", false);
            result.put("message", "手机号或密码错误");
        }

        return result;
    }

    /**
     * 检查手机号是否已注册
     * 
     * @param phone 手机号
     * @return 检查结果
     */
    public Map<String, Object> checkPhone(String phone) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = userRepository.existsByPhone(phone);
        result.put("exists", exists);
        result.put("message", exists ? "手机号已注册" : "手机号可用");
        return result;
    }

    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 检查结果
     */
    public Map<String, Object> checkUsername(String username) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = userRepository.existsByUsername(username);
        result.put("exists", exists);
        result.put("message", exists ? "用户名已存在" : "用户名可用");
        return result;
    }

    /**
     * 获取用户详细信息
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     */
    public Map<String, Object> getUserInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            User user = userOptional.get();

            // 构建完整的用户信息返回给前端
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("phone", user.getPhone());
            userInfo.put("realName", user.getRealName());
            userInfo.put("userType", user.getUserType());
            userInfo.put("gender", user.getGender());
            userInfo.put("birthday", user.getBirthday());
            userInfo.put("idCard", user.getIdCard());
            userInfo.put("email", user.getEmail());
            userInfo.put("wechat", user.getWechat());
            userInfo.put("qq", user.getQq());
            userInfo.put("job", user.getJob());
            userInfo.put("company", user.getCompany());
            userInfo.put("monthlyIncome", user.getMonthlyIncome());
            userInfo.put("rentalBudgetMin", user.getRentalBudgetMin());
            userInfo.put("rentalBudgetMax", user.getRentalBudgetMax());
            userInfo.put("preferredDistricts", user.getPreferredDistricts());
            userInfo.put("houseRequirements", user.getHouseRequirements());
            userInfo.put("isCertified", user.getIsCertified());
            userInfo.put("certificationTime", user.getCertificationTime());
            userInfo.put("creditScore", user.getCreditScore());
            userInfo.put("status", user.getStatus());
            userInfo.put("registerTime", user.getRegisterTime());
            userInfo.put("lastLoginTime", user.getLastLoginTime());
            userInfo.put("updatedTime", user.getUpdatedTime());
            userInfo.put("avatar", user.getAvatar());

            result.put("success", true);
            result.put("message", "获取用户信息成功");
            result.put("user", userInfo);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户信息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新用户信息
     * 
     * @param userId     用户ID
     * @param updateData 更新数据
     * @return 更新结果
     */
    public Map<String, Object> updateUserInfo(Long userId, Map<String, Object> updateData) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            User user = userOptional.get();

            // 更新允许修改的字段
            if (updateData.containsKey("realName")) {
                user.setRealName(getStringValue(updateData.get("realName")));
            }
            if (updateData.containsKey("gender")) {
                user.setGender(getIntegerValue(updateData.get("gender")));
            }
            if (updateData.containsKey("birthday")) {
                // 处理生日日期格式
                String birthdayStr = getStringValue(updateData.get("birthday"));
                if (birthdayStr != null && !birthdayStr.isEmpty()) {
                    user.setBirthday(LocalDateTime.parse(birthdayStr + "T00:00:00"));
                } else {
                    user.setBirthday(null);
                }
            }
            if (updateData.containsKey("idCard")) {
                user.setIdCard(getStringValue(updateData.get("idCard")));
            }
            if (updateData.containsKey("email")) {
                String email = getStringValue(updateData.get("email"));
                if (email != null && !email.isEmpty() && userRepository.existsByEmail(email) &&
                        !email.equals(user.getEmail())) {
                    result.put("success", false);
                    result.put("message", "邮箱已被使用");
                    return result;
                }
                user.setEmail(email);
            }
            if (updateData.containsKey("wechat")) {
                user.setWechat(getStringValue(updateData.get("wechat")));
            }
            if (updateData.containsKey("qq")) {
                user.setQq(getStringValue(updateData.get("qq")));
            }
            if (updateData.containsKey("job")) {
                user.setJob(getStringValue(updateData.get("job")));
            }
            if (updateData.containsKey("company")) {
                user.setCompany(getStringValue(updateData.get("company")));
            }
            if (updateData.containsKey("monthlyIncome")) {
                String incomeStr = getStringValue(updateData.get("monthlyIncome"));
                if (incomeStr != null && !incomeStr.isEmpty()) {
                    try {
                        user.setMonthlyIncome(new BigDecimal(incomeStr));
                    } catch (NumberFormatException e) {
                        // 如果数字格式错误，设置为null
                        user.setMonthlyIncome(null);
                    }
                } else {
                    user.setMonthlyIncome(null);
                }
            }
            if (updateData.containsKey("rentalBudgetMin")) {
                String minStr = getStringValue(updateData.get("rentalBudgetMin"));
                if (minStr != null && !minStr.isEmpty()) {
                    try {
                        user.setRentalBudgetMin(new BigDecimal(minStr));
                    } catch (NumberFormatException e) {
                        user.setRentalBudgetMin(null);
                    }
                } else {
                    user.setRentalBudgetMin(null);
                }
            }
            if (updateData.containsKey("rentalBudgetMax")) {
                String maxStr = getStringValue(updateData.get("rentalBudgetMax"));
                if (maxStr != null && !maxStr.isEmpty()) {
                    try {
                        user.setRentalBudgetMax(new BigDecimal(maxStr));
                    } catch (NumberFormatException e) {
                        user.setRentalBudgetMax(null);
                    }
                } else {
                    user.setRentalBudgetMax(null);
                }
            }
            if (updateData.containsKey("preferredDistricts")) {
                user.setPreferredDistricts(getStringValue(updateData.get("preferredDistricts")));
            }
            if (updateData.containsKey("houseRequirements")) {
                user.setHouseRequirements(getStringValue(updateData.get("houseRequirements")));
            }

            // 更新最后修改时间
            user.setUpdatedTime(LocalDateTime.now());

            User savedUser = userRepository.save(user);

            // 构建完整的用户信息返回给前端
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", savedUser.getId());
            userInfo.put("username", savedUser.getUsername());
            userInfo.put("phone", savedUser.getPhone());
            userInfo.put("realName", savedUser.getRealName());
            userInfo.put("userType", savedUser.getUserType());
            userInfo.put("gender", savedUser.getGender());
            userInfo.put("birthday", savedUser.getBirthday());
            userInfo.put("idCard", savedUser.getIdCard());
            userInfo.put("email", savedUser.getEmail());
            userInfo.put("wechat", savedUser.getWechat());
            userInfo.put("qq", savedUser.getQq());
            userInfo.put("job", savedUser.getJob());
            userInfo.put("company", savedUser.getCompany());
            userInfo.put("monthlyIncome", savedUser.getMonthlyIncome());
            userInfo.put("rentalBudgetMin", savedUser.getRentalBudgetMin());
            userInfo.put("rentalBudgetMax", savedUser.getRentalBudgetMax());
            userInfo.put("preferredDistricts", savedUser.getPreferredDistricts());
            userInfo.put("houseRequirements", savedUser.getHouseRequirements());
            userInfo.put("isCertified", savedUser.getIsCertified());
            userInfo.put("certificationTime", savedUser.getCertificationTime());
            userInfo.put("creditScore", savedUser.getCreditScore());
            userInfo.put("status", savedUser.getStatus());
            userInfo.put("registerTime", savedUser.getRegisterTime());
            userInfo.put("lastLoginTime", savedUser.getLastLoginTime());
            userInfo.put("updatedTime", savedUser.getUpdatedTime());
            userInfo.put("avatar", savedUser.getAvatar());

            result.put("success", true);
            result.put("message", "信息更新成功");
            result.put("user", userInfo);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(Object value) {
        if (value == null)
            return null;
        return value.toString();
    }

    /**
     * 安全获取整数值
     */
    private Integer getIntegerValue(Object value) {
        if (value == null)
            return 0;
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else {
                return Integer.parseInt(value.toString());
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 上传用户头像
     * 
     * @param avatar 头像文件
     * @param userId 用户ID
     * @return 上传结果
     */
    public Map<String, Object> uploadAvatar(MultipartFile avatar, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查用户是否存在
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            User user = userOptional.get();

            // 检查文件类型
            if (avatar.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择头像文件");
                return result;
            }

            String contentType = avatar.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "请选择图片文件");
                return result;
            }

            // 检查文件大小（限制为5MB）
            if (avatar.getSize() > 5 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "图片大小不能超过5MB");
                return result;
            }

            // 生成文件名 - 使用用户ID作为文件名
            String originalFilename = avatar.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = userId.toString() + fileExtension;

            // 创建保存目录 - 保存到前端项目的assets/image目录
            Path uploadPath = Paths.get("../Vue3/frontend/src/assets/image");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(avatar.getInputStream(), filePath);

            // 更新用户头像信息
            user.setAvatar(fileName);
            user.setUpdatedTime(LocalDateTime.now());
            userRepository.save(user);

            result.put("success", true);
            result.put("message", "头像上传成功");
            result.put("avatar", fileName);

        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "文件保存失败: " + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
        }

        return result;
    }
}
