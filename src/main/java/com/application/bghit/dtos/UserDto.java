package com.application.bghit.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String lastName;
    private String picture;
    private String email;
    private String telephone;
    private String token;
    private boolean profilCompleted;
    private boolean emailVerified;
    private Double rating;
    private Date dateInscription;
    private int affairesConcluses;
}
