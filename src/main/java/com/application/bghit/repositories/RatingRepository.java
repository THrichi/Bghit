package com.application.bghit.repositories;

import com.application.bghit.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query("SELECT COUNT(r) > 0 FROM Rating r WHERE r.raterId = :raterId AND r.userId = :userId")
    boolean existsByRaterIdAndUserId(Long raterId, Long userId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.userId = :userId")
    Double findAverageRating(Long userId);
}
