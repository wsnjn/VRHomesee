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
