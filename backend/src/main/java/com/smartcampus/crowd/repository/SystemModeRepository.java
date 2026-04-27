package com.smartcampus.crowd.repository;

import com.smartcampus.crowd.model.SystemMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemModeRepository extends JpaRepository<SystemMode, Long> {
    Optional<SystemMode> findFirstByOrderByIdAsc();
}
