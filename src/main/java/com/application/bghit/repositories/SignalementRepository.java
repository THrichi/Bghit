package com.application.bghit.repositories;

import com.application.bghit.entities.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long>, JpaSpecificationExecutor<Signalement> {
    // Trouver les signalements par leur type
    List<Signalement> findByReportType(Signalement.ReportType reportType);

    // Trouver les signalements par leur statut
    List<Signalement> findByStatus(Signalement.ReportStatus status);
    @Query("SELECT COUNT(s) > 0 FROM Signalement s WHERE s.idOwner = :idOwner AND s.idReportedUser = :idReportedUser")
    boolean existsByOwnerAndReportedUser(@Param("idOwner") Long idOwner, @Param("idReportedUser") Long idReportedUser);

    @Query("SELECT COUNT(s) > 0 FROM Signalement s WHERE s.idOwner = :idOwner AND s.idReportedDemande = :idReportedDemande")
    boolean existsByOwnerAndReportedDemande(@Param("idOwner") Long idOwner, @Param("idReportedDemande") Long idReportedDemande);
}