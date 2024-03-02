package com.application.bghit.repositories;

import com.application.bghit.entities.PhotoCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PhotoCollectionRepository extends JpaRepository<PhotoCollection, Long>, JpaSpecificationExecutor<PhotoCollection> {
}
