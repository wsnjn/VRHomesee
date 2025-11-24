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
