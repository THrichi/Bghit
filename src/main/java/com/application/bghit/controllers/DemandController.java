package com.application.bghit.controllers;

import com.application.bghit.dtos.DemandeCreateDto;
import com.application.bghit.dtos.DemandeListDto;
import com.application.bghit.entities.Demande;
import com.application.bghit.entities.User;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.services.DemandeService;
import com.application.bghit.services.UserService;
import com.application.bghit.specification.DemandeSpecification;
import com.sipios.springsearch.anotation.SearchSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class DemandController {

    private final  DemandeService demandeService;

    @PostMapping("/demande/create")
    public ResponseEntity<Demande> createDemande(@ModelAttribute DemandeCreateDto demandeDto) throws IOException, AppException {
        System.out.println("demande :" + demandeDto);
        Demande demande = demandeService.createDemande(demandeDto);
        return ResponseEntity.ok(demande);
    }
    @GetMapping("/demande/by-email")
    public ResponseEntity<List<DemandeListDto>> getDemandesByUserEmail(@RequestParam String email) {
        List<Demande> demandes = demandeService.findDemandesByUserEmail(email);
        if (demandes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<DemandeListDto> result = demandes.stream()
                .map(demandeService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/demande/by-id")
    public ResponseEntity<List<DemandeListDto>> getDemandesByUserId(@RequestParam Long id) {
        List<Demande> demandes = demandeService.findDemandesByUserId(id);
        if (demandes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<DemandeListDto> result = demandes.stream()
                .map(demandeService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/demande/by-demande-id")
    public ResponseEntity<DemandeListDto> getDemandesById(@RequestParam Long id) {
        Optional<Demande> demande = demandeService.findDemandeById(id);
        return demande.map(value -> ResponseEntity.ok(demandeService.convertToDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/demandes")
    public ResponseEntity<Page<DemandeListDto>> searchingAuthUsers(@RequestParam(required = false) String titre,
                                                                   @RequestParam(required = false) String categorie,
                                                                   @RequestParam(required = false) String etat,
                                                                   @RequestParam(required = false) Double lat,
                                                                   @RequestParam(required = false) Double lng,
                                                                   @RequestParam(required = false) Double distance,
                                                                   @RequestParam(required = false) Double prixMin,
                                                                   @RequestParam(required = false) Double prixMax,
                                                                   @RequestParam(required = false) boolean gratuit,
                                                                   @RequestParam(required = false) boolean estPayant,
                                                                   Pageable pageable) {
        Specification<Demande> specification = DemandeSpecification.withDynamicQuery(titre, categorie, etat, lat, lng, distance,gratuit,prixMin,prixMax,estPayant);
        return ResponseEntity.ok(demandeService.findAll(specification, pageable));
    }
    @DeleteMapping("/demande/remove")
    public ResponseEntity<?> removeDemande(@RequestParam("id") Long demandeId) throws AppException {
        demandeService.deleteDemande(UserService.getCurrentUserEmail(),demandeId);
        return ResponseEntity.ok().build();
    }

}
