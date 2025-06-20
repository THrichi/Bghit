package com.application.bghit.services;

import com.application.bghit.entities.Notification;
import com.application.bghit.entities.User;
import com.application.bghit.enums.NotificationType;
import com.application.bghit.repositories.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }


    public void sendNotification(Long userId, Notification notification) {
        simpMessagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", notification);
    }
    public Notification createNotification(Notification notification, User user) {
        notification.setDateCreated(LocalDateTime.now());
        notification.setUser(user);
        if(notification.getPicture() == null)notification.setPicture(getImageUrl(notification.getType()));
        Notification createdNotification = saveNotification(notification);
        sendNotification(user.getId(), createdNotification);
        return createdNotification;
    }
    public String getImageUrl(NotificationType type) {
        switch (type) {
            case MESSAGE:
                return "/assets/img/default/notifications/newMessage.png";
            case NEWS:
                return "/assets/img/default/notifications/news.png";
            case WARNING:
                return "/assets/img/default/notifications/warning.png";
            case REQUEST_ACCEPTANCE:
                return "/assets/img/default/notifications/accept.png";
            case PROGRESS:
                return "/assets/img/default/notifications/progress.png";
            default:
                throw new IllegalArgumentException("Invalid NotificationType: " + type);
        }
    }

    public void markAsRead(Notification notification) {
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public List<Notification> getAllNotificationsForUser(User user) {
        return notificationRepository.findAllByUserAndIsReadFalse(user);
    }

    public void newNotification(User user, String title, String message,NotificationType type, String url) {
        if(user.getSettings().isActivateNotifications())
        {
            Notification notification = new Notification();
            notification.setMessage(message);
            notification.setTitle(title);
            notification.setType(type);
            notification.setUrl(url);
            createNotification(notification,user);
        }
    }
}