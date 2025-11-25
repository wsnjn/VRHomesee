package com.example.homesee.repository;

import com.example.homesee.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    List<ChatGroup> findByGroupType(Integer groupType);

    List<ChatGroup> findByOwnerId(Long ownerId);
}
