package com.example.homesee.controller;

import com.example.homesee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://39.108.142.250:9999,http://localhost:5173") // Vue开发服务器地址
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * 
     * @param request 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String phone = request.get("phone");
        String password = request.get("password");
        String realName = request.get("realName");

        return userService.register(username, phone, password, realName);
    }

    /**
     * 用户登录接口
     * 
     * @param request 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String password = request.get("password");

        return userService.login(phone, password);
    }

    /**
     * 检查手机号是否已注册
     * 
     * @param phone 手机号
     * @return 检查结果
     */
    @GetMapping("/check-phone/{phone}")
    public Map<String, Object> checkPhone(@PathVariable String phone) {
        return userService.checkPhone(phone);
    }

    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username/{username}")
    public Map<String, Object> checkUsername(@PathVariable String username) {
        return userService.checkUsername(username);
    }

    /**
     * 获取用户详细信息
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getUserInfo(@PathVariable Long userId) {
        return userService.getUserInfo(userId);
    }

    /**
     * 上传用户头像
     * 
     * @param avatar 头像文件
     * @param userId 用户ID
     * @return 上传结果
     */
    @PostMapping("/upload-avatar")
    public Map<String, Object> uploadAvatar(@RequestParam("avatar") MultipartFile avatar,
            @RequestParam("userId") Long userId) {
        return userService.uploadAvatar(avatar, userId);
    }

    /**
     * 更新用户信息
     * 
     * @param userId     用户ID
     * @param updateData 更新数据
     * @return 更新结果
     */
    @PutMapping("/update/{userId}")
    public Map<String, Object> updateUserInfo(@PathVariable Long userId, @RequestBody Map<String, Object> updateData) {
        return userService.updateUserInfo(userId, updateData);
    }

    /**
     * 通过手机号搜索用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    @GetMapping("/search/phone")
    public Map<String, Object> searchByPhone(@RequestParam String phone) {
        return userService.searchByPhone(phone);
    }
}
