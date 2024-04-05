package com.application.bghit.repositories;

import com.application.bghit.entities.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SettingsRepository extends JpaRepository<Settings, Long>, JpaSpecificationExecutor<Settings> {
}
