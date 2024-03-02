package com.application.bghit.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifiedDto {
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean profileVerified;
    private boolean identityVerified;
}
