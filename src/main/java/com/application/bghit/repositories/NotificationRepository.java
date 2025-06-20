package com.application.bghit.repositories;

import com.application.bghit.entities.Notification;
import com.application.bghit.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserAndIsReadFalse(User user);
}