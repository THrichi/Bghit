package com.application.bghit.specification;


import com.application.bghit.entities.Demande;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class DemandeSpecification {

    public static Specification<Demande> withDynamicQuery(String titre,
                                                          String categorie,
                                                          String etat,
                                                          Double lat,
                                                          Double lng,
                                                          Double distance,
                                                          boolean gratuit,
                                                          Double prixMin,
                                                          Double prixMax,
                                                          boolean estPayant
                                                          ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(!estPayant)
            {
                predicates.add(cb.equal(root.get("estPayant"), estPayant));
            }

            // Gestion des intervalles de prix
            if (gratuit) {
                predicates.add(cb.equal(root.get("prix"), 0));
            } else {
                if (prixMin != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("prix"), prixMin));
                }
                if (prixMax != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("prix"), prixMax));
                }
            }
            if (lat != 0 && lng != 0 && distance != 0) {
                // Convertir la distance en degrés (approximation simple, valable principalement pour de courtes distances)
                double distanceInDegrees = distance / 111.32; // Cette approximation fonctionne mieux à l'équateur

                // Latitude
                Predicate latPredicate = cb.between(root.get("latitude"), lat - distanceInDegrees, lat + distanceInDegrees);

                // Longitude
                Predicate lngPredicate = cb.between(root.get("longitude"), lng - distanceInDegrees, lng + distanceInDegrees);

                predicates.add(cb.and(latPredicate, lngPredicate));
            }
            if (titre != null && !titre.isEmpty()) {

                Predicate titrePredicate = cb.like(cb.lower(root.get("titre")), "%" + titre.toLowerCase() + "%");

                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), "%" + titre.toLowerCase() + "%");

                Predicate categoryPredicate = cb.like(cb.lower(root.get("categorie")), "%" + titre.toLowerCase() + "%");
                // Combiner les prédicats titre et description avec un OU logique
                predicates.add(cb.or(titrePredicate, descriptionPredicate,categoryPredicate));
            }
            if (categorie != null && !categorie.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("categorie")), categorie.toLowerCase()));
            }
            if (etat != null && !etat.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("etat")), etat.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
