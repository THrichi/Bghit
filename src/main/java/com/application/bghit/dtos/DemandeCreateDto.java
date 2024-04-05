package com.application.bghit.dtos;

import com.application.bghit.entities.Demande;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.multipart.MultipartFile;
public record DemandeCreateDto(
        @NotBlank String description,
        boolean estPayant,
        boolean surDevis,
        @PositiveOrZero Double prix,
        @NotBlank String categorie,
        @NotBlank String lieu,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @Email String userEmail,
        MultipartFile[] images,
        @NotNull Demande.DemandeType type
) {
}