package com.application.bghit.repositories;

import com.application.bghit.entities.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SearchRepository extends JpaRepository<Search, Long>, JpaSpecificationExecutor<Search> {
}
