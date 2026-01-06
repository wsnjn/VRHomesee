/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：RoomVrSceneRepository.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：VR 场景数据访问接口，支持按房屋 ID 获取该房源关联的所有 VR 全景场景（如客厅、卧室等）
 * 创建日期：2026-01-06
 */
package com.example.homesee.repository;

import com.example.homesee.entity.RoomVrScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomVrSceneRepository extends JpaRepository<RoomVrScene, Long> {
    List<RoomVrScene> findByRoomId(Long roomId);
}
