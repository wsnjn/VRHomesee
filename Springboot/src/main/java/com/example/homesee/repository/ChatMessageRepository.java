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
}
