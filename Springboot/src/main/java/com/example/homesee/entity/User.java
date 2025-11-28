package com.example.homesee.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "phone", unique = true, nullable = false, length = 20)
    private String phone;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "user_type", nullable = false)
    private Integer userType = 1;

    @Column(name = "register_time", nullable = false)
    private LocalDateTime registerTime = LocalDateTime.now();

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime = LocalDateTime.now();

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "birthday")
    private LocalDateTime birthday;

    @Column(name = "id_card", unique = true, length = 18)
    private String idCard;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "wechat", length = 50)
    private String wechat;

    @Column(name = "qq", length = 20)
    private String qq;

    @Column(name = "job", length = 50)
    private String job;

    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "monthly_income", precision = 10, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "rental_budget_min", precision = 8, scale = 2)
    private BigDecimal rentalBudgetMin;

    @Column(name = "rental_budget_max", precision = 8, scale = 2)
    private BigDecimal rentalBudgetMax;

    @Column(name = "preferred_districts", length = 500)
    private String preferredDistricts;

    @Column(name = "house_requirements", columnDefinition = "TEXT")
    private String houseRequirements;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "credit_score", nullable = false)
    private Integer creditScore = 100;

    @Column(name = "is_certified", nullable = false)
    private Integer isCertified = 0;

    @Column(name = "certification_time")
    private LocalDateTime certificationTime;

    @Column(name = "chat_muted_until")
    private LocalDateTime chatMutedUntil;

    @Column(name = "social_banned_until")
    private LocalDateTime socialBannedUntil;

    // 构造函数
    public User() {
    }

    public User(String username, String phone, String password, String realName) {
        this.username = username;
        this.phone = phone;
        this.password = password;
        this.realName = realName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getRentalBudgetMin() {
        return rentalBudgetMin;
    }

    public void setRentalBudgetMin(BigDecimal rentalBudgetMin) {
        this.rentalBudgetMin = rentalBudgetMin;
    }

    public BigDecimal getRentalBudgetMax() {
        return rentalBudgetMax;
    }

    public void setRentalBudgetMax(BigDecimal rentalBudgetMax) {
        this.rentalBudgetMax = rentalBudgetMax;
    }

    public String getPreferredDistricts() {
        return preferredDistricts;
    }

    public void setPreferredDistricts(String preferredDistricts) {
        this.preferredDistricts = preferredDistricts;
    }

    public String getHouseRequirements() {
        return houseRequirements;
    }

    public void setHouseRequirements(String houseRequirements) {
        this.houseRequirements = houseRequirements;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public Integer getIsCertified() {
        return isCertified;
    }

    public void setIsCertified(Integer isCertified) {
        this.isCertified = isCertified;
    }

    public LocalDateTime getCertificationTime() {
        return certificationTime;
    }

    public void setCertificationTime(LocalDateTime certificationTime) {
        this.certificationTime = certificationTime;
    }

    public LocalDateTime getChatMutedUntil() {
        return chatMutedUntil;
    }

    public void setChatMutedUntil(LocalDateTime chatMutedUntil) {
        this.chatMutedUntil = chatMutedUntil;
    }

    public LocalDateTime getSocialBannedUntil() {
        return socialBannedUntil;
    }

    public void setSocialBannedUntil(LocalDateTime socialBannedUntil) {
        this.socialBannedUntil = socialBannedUntil;
    }
}
