package com.application.bghit.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.application.bghit.entities.ChatMessage;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT c FROM ChatMessage c WHERE c.room.id = :roomId")
    List<ChatMessage> findByRoomId(Long roomId);

    @Query("SELECT c FROM ChatMessage c WHERE c.room.id = :roomId AND c.status = :status AND c.sender != :userId")
    List<ChatMessage> findByRoomIdAndStatus(Long userId,Long roomId, ChatMessage.MessageStatus status);



}
