package com.application.bghit.dtos;

import com.application.bghit.entities.Demande;
import com.application.bghit.entities.User;

import java.util.Date;
import java.util.List;

public record DemandeListDto(
        Long idDemande,
        String titre,
        String description,
        Date dateCreation,
        boolean estPayant,
        boolean surDevis,
        Double prix,
        String categorie,
        Demande.DemandeStatus etat,
        String lieu,
        Double latitude,
        Double longitude,
        int nombreDeVues,
        int nombreDeReponses,
        List<String> images, // Assurez-vous d'avoir une liste d'URLs pour les images
        String theme,
        UserDto user, // Email de l'utilisateur comme identifiant simplifié

        Long reservedToIdUser
) {
}
