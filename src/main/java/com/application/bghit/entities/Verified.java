package com.application.bghit.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Verified {
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean profileVerified;
    private boolean identityVerified;
}
