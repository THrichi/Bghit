package com.application.bghit.dtos;

import org.springframework.web.multipart.MultipartFile;

public record DemandeCreateDto(
        String titre,
        String description,
        boolean estPayant,
        boolean surDevis,
        Double prix,
        String categorie,
        String lieu,
        Double latitude,
        Double longitude,
        String userEmail,
        MultipartFile[] images

) {
}
