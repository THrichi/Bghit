package com.application.bghit.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name="app_signalement")
public class Signalement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,  name = "id_owner")
    private Long idOwner;

    @Column(nullable = true,  name = "id_reported_user")
    private Long idReportedUser;

    @Column(nullable = true,  name = "id_reported_demande")
    private Long idReportedDemande;


    public enum ReportType {
        SPAM,
        HARASSMENT,
        INAPPROPRIATE_CONTENT,
        FRAUD,
        COPYRIGHT_INFRINGEMENT,
        FAKE_SERVICE,
        PRIVACY_VIOLATION,
        OTHER
    }

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType = ReportType.OTHER;

    @Builder.Default
    private String description="";

    public enum ReportStatus {
        PENDING,    // En attente de révision
        REVIEWING,  // En cours de révision
        RESOLVED,   // Résolu
        REJECTED    // Rejeté
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reportDate = new Date();
}


