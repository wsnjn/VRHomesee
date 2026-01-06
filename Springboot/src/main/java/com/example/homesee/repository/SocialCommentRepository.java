/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：SocialCommentRepository.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：动态评论数据访问接口，支持按动态 ID 获取评论列表及统计评论总数
 * 创建日期：2026-01-06
 */
package com.example.homesee.repository;

import com.example.homesee.entity.SocialComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialCommentRepository extends JpaRepository<SocialComment, Long> {
    List<SocialComment> findByPostIdOrderByCreatedTimeAsc(Long postId);

    long countByPostId(Long postId);
}
