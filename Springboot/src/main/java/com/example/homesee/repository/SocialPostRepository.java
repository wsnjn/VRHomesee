package com.example.homesee.repository;

import com.example.homesee.entity.SocialPost;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialPostRepository extends JpaRepository<SocialPost, Long> {
    List<SocialPost> findAll(Sort sort);

    List<SocialPost> findByUserId(Long userId);
}
