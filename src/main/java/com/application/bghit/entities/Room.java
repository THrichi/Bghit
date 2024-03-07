package com.application.bghit.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "rooms")
public class Room  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_1")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user_2")
    private User user2;


    @Column(name = "blocked_user")
    private Long blockedUser;

    @OneToMany(mappedBy = "room")
    @JsonManagedReference
    private List<ChatMessage> messages = new ArrayList<>();

    public enum RoomStatus {
        ACTIF,
        BLOCKED,
        ARCHIVED,
        CLOSED,
    }
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.ACTIF;

    @Column(name = "archived_id_1")
    private Long archivedUserId1;

    @Column(name = "archived_id_2")
    private Long archivedUserId2;

    @ManyToOne
    @JoinColumn(name = "demande_id")
    private Demande demande;
}