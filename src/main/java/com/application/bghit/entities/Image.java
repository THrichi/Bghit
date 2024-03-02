package com.application.bghit.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
public class Image  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String url; // URL de l'image stockée dans un système de fichiers ou un service de stockage externe

    @ManyToOne
    @JoinColumn(name = "id_demande", nullable = false)
    @JsonBackReference
    private Demande demande; // Référence arrière à Demande

    public Image(String url, Demande demande) {
        this.url = url;
        this.demande = demande;
    }
}
