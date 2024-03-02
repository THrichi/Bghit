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
    // Ajoutez ici les champs de recherche spécifiques
    // Autres attributs et annotations nécessaires
}
