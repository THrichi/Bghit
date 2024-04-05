package com.application.bghit.entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idDemande")
@Table(name = "demandes")
@Getter
@Setter
public class Demande  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Long idDemande;


    @Column(nullable = false, length = 2000)
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

    public enum DemandeStatus {
        CREATED,
        ONLINE,
        RESERVED,
        INPROGRESS,
        RATING,
        ARCHIVED,
        CLOSED
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DemandeStatus etat = DemandeStatus.CREATED; // État de la demande

    @Column(nullable = false, length = 255)
    private String lieu;

    @Builder.Default
    private Double latitude = 0.0;

    @Builder.Default
    private Double longitude = 0.0;

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
    @JsonBackReference
    private User user;


    @Column(name = "reserved_to_user")
    private Long reservedToIdUser;

    public enum DemandeType {
        SELL,
        SERVICE
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DemandeType type;

}
