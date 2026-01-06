/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：ChatMessageRepository.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：聊天消息数据访问接口，支持按群组 ID 或点对点用户 ID 获取历史消息流，并支持按群组 ID 批量删除消息
 * 创建日期：2026-01-06
 */
package com.example.homesee.repository;

import com.example.homesee.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByGroupIdOrderByCreatedTimeAsc(Long groupId);

    List<ChatMessage> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedTimeAsc(
            Long senderId1, Long receiverId1, Long senderId2, Long receiverId2);

    void deleteByGroupId(Long groupId);
}
