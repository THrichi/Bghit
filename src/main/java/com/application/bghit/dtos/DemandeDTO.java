package com.application.bghit.dtos;

import java.util.Date;

public class DemandeDTO {
    private Long idDemande;
    private String description;
    private Date dateCreation;
    private boolean estPayant;
    private boolean surDevis;
    private Double prix;
    private String categorie;
    private String lieu;
    private Double latitude;
    private Double longitude;
    private int nombreDeVues;
    private int nombreDeReponses;
    private String theme;
    // Si tu souhaites inclure des informations de l'utilisateur, utilise un UserDTO

    // Constructeurs, Getters et Setters
}
