/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：ChatHistoryRepository.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：对话历史数据访问接口，用于按用户 ID 获取与 AI 助手的历史聊天记录，并按创建时间升序排列
 * 创建日期：2026-01-06
 */
package com.example.homesee.repository;

import com.example.homesee.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUserIdOrderByCreatedTimeAsc(Long userId);
}
