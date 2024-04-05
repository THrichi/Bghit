package com.application.bghit.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_demande_search")
@Data
@NoArgsConstructor
public class Search {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = true)
    private String keyword;

    @Column(nullable = true)
    private String searchCategory;

    @Column(nullable = true)
    private String searchAdresse;

    @Column(nullable = false)
    private Demande.DemandeType type;

    @Column(nullable = true)
    private int searchDistance;

    @Column(nullable = true)
    private Double searchLatitude;

    @Column(nullable = true)
    private Double searchLongitude;

    @Column(nullable = true)
    private Double searchPriceMin;

    @Column(nullable = true)
    private Double searchPriceMax;

    @Column(nullable = true)
    private boolean searchGratuit;

    @Column(nullable = true)
    private boolean searchSurDevis;

    @Column(nullable = true)
    private String nickName;

}
