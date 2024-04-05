package com.application.bghit.dtos;

import java.time.LocalDate;

public record UserUpdateDto(
        String password,
        String name,
        String lastName,
        String image,
        String telephone,
        LocalDate dateNaissance,
        String adresse,
        Double latitude,
        Double longitude,
        boolean disponibleALHeure
) {
}
