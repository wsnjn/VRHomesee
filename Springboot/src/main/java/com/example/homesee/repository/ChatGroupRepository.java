/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：ChatGroupRepository.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：聊天群组数据访问接口，支持按群组类型（如全局、租客群）、所有者 ID 及唯一群组名进行查询
 * 创建日期：2026-01-06
 */
package com.example.homesee.repository;

import com.example.homesee.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    List<ChatGroup> findByGroupType(Integer groupType);

    List<ChatGroup> findByOwnerId(Long ownerId);

    java.util.Optional<ChatGroup> findByGroupName(String groupName);
}
