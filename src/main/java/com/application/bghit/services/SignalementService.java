package com.application.bghit.services;

import com.application.bghit.entities.Signalement;
import com.application.bghit.repositories.SignalementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SignalementService {

    private final SignalementRepository signalementRepository;

    // Ajouter un signalement
    public Signalement addSignalement(Signalement signalement) {
        return signalementRepository.save(signalement);
    }

    // Mettre à jour un signalement
    public Signalement updateSignalement(Signalement signalement) {
        if (signalement.getId() == null || !signalementRepository.existsById(signalement.getId())) {
            throw new IllegalArgumentException("Le signalement avec cet ID n'existe pas.");
        }
        return signalementRepository.save(signalement);
    }

    // Récupérer un signalement par ID
    public Optional<Signalement> getSignalementById(Long id) {
        return signalementRepository.findById(id);
    }

    // Récupérer tous les signalements
    public List<Signalement> getAllSignalements() {
        return signalementRepository.findAll();
    }

    // Récupérer les signalements par type
    public List<Signalement> getSignalementsByType(Signalement.ReportType reportType) {
        return signalementRepository.findByReportType(reportType);
    }

    // Récupérer les signalements par statut
    public List<Signalement> getSignalementsByStatus(Signalement.ReportStatus status) {
        return signalementRepository.findByStatus(status);
    }

    // Supprimer un signalement
    public void deleteSignalement(Long id) {
        signalementRepository.deleteById(id);
    }

    public boolean canCreateSignalement(Signalement signalement) {
        if (signalement.getIdReportedUser() != null) {
            return !signalementRepository.existsByOwnerAndReportedUser(signalement.getIdOwner(), signalement.getIdReportedUser());
        } else if (signalement.getIdReportedDemande() != null) {
            return !signalementRepository.existsByOwnerAndReportedDemande(signalement.getIdOwner(), signalement.getIdReportedDemande());
        }
        return false;
    }
}