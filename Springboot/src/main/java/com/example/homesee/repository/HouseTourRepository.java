/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：HouseTourRepository.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：房屋导览数据访问接口，支持按导览标题或描述内容的模糊匹配查询，用于 VR 带看与房源导览详情检索
 * 创建日期：2026-01-06
 */
package com.example.homesee.repository;

import com.example.homesee.entity.HouseTour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseTourRepository extends JpaRepository<HouseTour, Long> {

    // 根据标题查找房屋导览
    List<HouseTour> findByTitleContaining(String title);

    // 根据描述查找房屋导览
    List<HouseTour> findByDescriptionContaining(String description);
}
