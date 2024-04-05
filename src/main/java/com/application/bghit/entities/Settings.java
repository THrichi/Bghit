package com.application.bghit.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name="app_user_settings")
public class Settings implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_email")
    @Builder.Default
    private boolean showEmail = false;

    @Column(name = "show_address")
    @Builder.Default
    private boolean showAddress = false;

    @Column(name = "show_phone")
    @Builder.Default
    private boolean showPhone = false;

    public enum Theme {
        DARK, LIGHT
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "theme")
    @Builder.Default
    private Theme theme = Theme.LIGHT;

    public enum Language {
        ar, fr, en
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    @Builder.Default
    private Language language = Language.fr;

    @Column(name = "activate_auto_location")
    @Builder.Default
    private boolean activateAutoLocation = false;

    @Column(name = "activate_message_sound")
    @Builder.Default
    private boolean activateMessageSound = true;

    @Column(name = "activate_notifications")
    @Builder.Default
    private boolean activateNotifications = true;


    // Relation avec l'entité User si nécessaire
}
