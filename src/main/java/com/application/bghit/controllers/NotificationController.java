package com.application.bghit.controllers;

import com.application.bghit.entities.Notification;
import com.application.bghit.entities.User;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.services.NotificationService;
import com.application.bghit.services.UserService;
import com.google.api.Http;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody Notification notification) throws AppException {
        String userEmail = UserService.getCurrentUserEmail();
        Optional<User> user = userService.findByEmail(userEmail);
        if (user.isEmpty()) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(notificationService.createNotification(notification,user.get()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotification(@PathVariable Long id) {
        return notificationService.getNotificationById(id)
                .map(notification -> new ResponseEntity<>(notification, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/markAsRead")
    public ResponseEntity<Void> markNotificationAsRead(@RequestParam("id") Long id) throws AppException {
        Notification notification = notificationService.getNotificationById(id)
                .orElseThrow(() -> new AppException("Notification not found", HttpStatus.NOT_FOUND));
        notificationService.markAsRead(notification);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/markAllAsRead")
    public ResponseEntity<Void> markAllNotificationsAsRead() throws AppException {
        String userEmail = UserService.getCurrentUserEmail();
        Optional<User> userOptional = userService.findByEmail(userEmail);
        if (userOptional.isEmpty()) throw new AppException("User not found", HttpStatus.NOT_FOUND);
        User user = userOptional.get();
        List<Notification> notifications = notificationService.getAllNotificationsForUser(user);
        notifications.forEach(notificationService::markAsRead);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}