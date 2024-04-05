package com.application.bghit.repositories;


import com.application.bghit.dtos.CategorieCountDTO;
import com.application.bghit.entities.Demande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> , JpaSpecificationExecutor<Demande> {
    @Query("SELECT d FROM Demande d WHERE d.user.email = :email")
    List<Demande> findByUserEmail(@Param("email") String email);

    @Query("SELECT d FROM Demande d WHERE d.user.id = :id")
    List<Demande> findByUserId(@Param("id") Long id);
    Page<Demande> findAll(Specification specification, Pageable pageable);
    @Query("SELECT new com.application.bghit.dtos.CategorieCountDTO(d.categorie, COUNT(d)) " +
            "FROM Demande d WHERE d.etat = 'ONLINE' AND d.type = :type GROUP BY d.categorie " +
            "ORDER BY COUNT(d) DESC")
    List<CategorieCountDTO> findTopCategories(@Param("type") Demande.DemandeType type, Pageable pageable);

}
