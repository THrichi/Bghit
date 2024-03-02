package com.application.bghit.entities;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "chat_messages")
public class ChatMessage  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    @JsonBackReference
    private Room room;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "sender_id",nullable = false)
    private Long sender;


    @Column(nullable = false)
    private Date timestamp = new Date();

    // Enum for message status
    public enum MessageStatus {
        SENT, RECEIVED, READ
    }
    // Add a column for the message status
    @Enumerated(EnumType.STRING) // Store enum values as String
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SENT;
}
