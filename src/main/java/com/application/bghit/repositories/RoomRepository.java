package com.application.bghit.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.application.bghit.entities.Room;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r WHERE (r.user1.id = :senderId AND r.user2.id = :recipientId) OR (r.user1.id = :recipientId AND r.user2.id = :senderId)")
    Optional<Room> findRoomByUsers(Long senderId, Long recipientId);
    @Query("SELECT r FROM Room r WHERE r.user1.id = :roomId  OR  r.user2.id = :roomId")
    List<Room> findRoomsByUser(Long roomId);
}