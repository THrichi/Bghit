package com.application.bghit.dtos;
import com.application.bghit.entities.Rating;
import com.application.bghit.entities.Search;
import com.application.bghit.entities.Settings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfilDto {
    private Long id;
    private String name;
    private String lastName;
    private LocalDate dateNaissance;
    private String picture;
    private String email;
    private boolean isEmailValidated;
    private String telephone;
    private String adresse;
    private Double latitude;
    private Double longitude;
    private Double rating; // avis
    private List<Rating> ratings;
    private Date dateInscription;
    private boolean isParticulier;
    private String aboutMe;
    private boolean profileCompleted;
    private int affairesConcluses;
    private boolean disponibleALHeure;
    private StatusDto status;
    private VerifiedDto verified;
    private List<Search> searchFavoris;
    private List<PhotoCollectionDto> photos;
    private List<DemandeListDto> demandeFavoris;
    private Settings settings;

    // Vous pouvez choisir d'ajouter d'autres champs ici selon les besoins
}

// Créez des DTOs supplémentaires pour Search, PhotoCollection, Verified, et Status si nécessaire

