package com.application.bghit.controllers;

import com.application.bghit.entities.Signalement;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.services.SignalementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signalements")
@RequiredArgsConstructor
public class SignalementController {

    private final SignalementService signalementService;

    // Créer un nouveau signalement
    @PostMapping("/create")
    public ResponseEntity<Signalement> createSignalement(@RequestBody Signalement signalement) throws AppException {
        if(!signalementService.canCreateSignalement(signalement))throw new AppException("Duplicated", HttpStatus.BAD_REQUEST);
        return ResponseEntity.ok(signalementService.addSignalement(signalement));
    }
}
