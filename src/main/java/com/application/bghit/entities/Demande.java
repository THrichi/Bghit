package com.application.bghit.entities;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "demandes")
@Getter
@Setter
@NoArgsConstructor
public class Demande  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Long idDemande;

    @Column(nullable = false, length = 100)
    private String titre;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Date dateCreation = new Date();

    @Column(nullable = false)
    private boolean estPayant;

    @Column(nullable = false)
    private boolean surDevis;

    @Column(nullable = true)
    private Double prix; // Peut être 0 pour indiquer que c'est gratuit

    @Column(nullable = false)
    private String categorie; // Catégorie de la demande

    @Column(nullable = false)
    private String etat; // État de la demande

    @Column(nullable = false, length = 255)
    private String lieu;

    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    @Column(nullable = false)
    private int nombreDeVues;

    @Column(nullable = false)
    private int nombreDeReponses;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Image> images = new ArrayList<>();

    // Méthodes pour gérer les images
    public void addImage(Image image) {
        images.add(image);
        image.setDemande(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.setDemande(null);
    }


    @Column(nullable = false, length = 50)
    private String theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
