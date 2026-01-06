/**
 * 项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
 * 文件名称：FriendshipRepository.java
 * 开发者：牛迦楠
 * 专业：软件工程（中外合作办学）
 * 学校：东华理工大学
 * 功能描述：好友关系数据访问接口，支持按状态查询好友请求、双方 ID 匹配以及获取已建立的好友列表（状态为 1）
 * 创建日期：2026-01-06
 */
package com.example.homesee.repository;

import com.example.homesee.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByUserIdAndStatus(Long userId, Integer status);

    List<Friendship> findByFriendIdAndStatus(Long friendId, Integer status);

    @Query("SELECT f FROM Friendship f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = 1")
    List<Friendship> findAcceptedFriendships(Long userId);

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
}
