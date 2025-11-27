package com.example.homesee.repository;

import com.example.homesee.entity.SocialLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialLikeRepository extends JpaRepository<SocialLike, Long> {
    Optional<SocialLike> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
