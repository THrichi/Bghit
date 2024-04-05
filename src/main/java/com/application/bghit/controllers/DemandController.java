package com.application.bghit.controllers;

import com.application.bghit.dtos.CategorieCountDTO;
import com.application.bghit.dtos.ConfirmationResponse;
import com.application.bghit.dtos.DemandeCreateDto;
import com.application.bghit.dtos.DemandeListDto;
import com.application.bghit.entities.Demande;
import com.application.bghit.entities.User;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.services.ChatService;
import com.application.bghit.services.DemandeService;
import com.application.bghit.services.UserService;
import com.application.bghit.specification.DemandeSpecification;
import com.sipios.springsearch.anotation.SearchSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class DemandController {

    private final  DemandeService demandeService;
    private final ChatService chatService;

    @PostMapping("/demande/create")
    public ResponseEntity<ConfirmationResponse> createDemande(@Validated @ModelAttribute DemandeCreateDto demandeDto) throws IOException, AppException {
       return ResponseEntity.ok(demandeService.createDemande(demandeDto));
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
                                                                   @RequestParam(required = false) Double lat,
                                                                   @RequestParam(required = false) Double lng,
                                                                   @RequestParam(required = false) Double distance,
                                                                   @RequestParam(required = false) Double prixMin,
                                                                   @RequestParam(required = false) Double prixMax,
                                                                   @RequestParam(required = false) boolean gratuit,
                                                                   @RequestParam(required = false) Boolean surDevis,
                                                                   @RequestParam(required = false) String etat,
                                                                   @RequestParam(required = false) Demande.DemandeType type,
                                                                   Pageable pageable) {
        List<Demande.DemandeStatus> etatList = null;
        if (etat != null && !etat.isEmpty()) {
            etatList = Arrays.stream(etat.split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(Demande.DemandeStatus::valueOf)
                    .toList();
        }


        Specification<Demande> specification = DemandeSpecification.withDynamicQuery(titre, categorie, lat, lng, distance,prixMin,prixMax,gratuit,surDevis,etatList,type);
        return ResponseEntity.ok(demandeService.findAll(specification, pageable));
    }

    @GetMapping("/public/demandes")
    public ResponseEntity<Page<DemandeListDto>> searchingAuthUsers(@RequestParam(required = false) String etat,
                                                                   @RequestParam(required = false) Demande.DemandeType type,
                                                                   Pageable pageable) {
        List<Demande.DemandeStatus> etatList = null;
        if (etat != null && !etat.isEmpty()) {
            etatList = Arrays.stream(etat.split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(Demande.DemandeStatus::valueOf)
                    .toList();
        }


        Specification<Demande> specification = DemandeSpecification.withDynamicQuery(null, null, null, null, null,null,null,false,null,etatList,type);

        return ResponseEntity.ok(demandeService.findAll(specification, pageable));
    }
    @DeleteMapping("/demande/remove")
    public ResponseEntity<?> removeDemande(@RequestParam("id") Long demandeId) throws AppException {
        demandeService.deleteDemande(UserService.getCurrentUserEmail(),demandeId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/demande/updateDemande/{demandeId}/status")
    public ResponseEntity<DemandeListDto> changeDemandeStatus(@PathVariable Long demandeId,
                                                              @RequestParam("status") Demande.DemandeStatus status,
                                                              @RequestParam(value = "reservedToIdUser",required = false) Long reservedToIdUser,
                                                              @RequestParam("roomId") Long roomId
                                                              ) throws AppException {
        Demande updated = demandeService.changeDemandeStatus(demandeId, status,reservedToIdUser);
        if(status.equals(Demande.DemandeStatus.ONLINE))chatService.addDemande(roomId,null);
        if (updated != null) {
            return ResponseEntity.ok(demandeService.convertToDto(updated));
        } else {
            throw new AppException("",HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/demande/top-categories")
    public ResponseEntity<List<CategorieCountDTO>> getTopCategories(@RequestParam("type") Demande.DemandeType type) {
        List<CategorieCountDTO> topCategories = demandeService.getTop5Categories(type);
        return ResponseEntity.ok(topCategories);
    }
}
