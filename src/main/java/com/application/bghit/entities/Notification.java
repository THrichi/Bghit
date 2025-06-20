package com.application.bghit.entities;

import com.application.bghit.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Column(length = 1000)
    private String message;

    private LocalDateTime dateCreated;

    private boolean isRead;

    private String picture;

    private String url;

    @Override
    public String toString() {
        // Replace "user" with any other fields you want to display in toString()
        return "Notification{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", dateCreated=" + dateCreated +
                ", isRead=" + isRead +
                ", user=" + user +
                "}";
    }
}

