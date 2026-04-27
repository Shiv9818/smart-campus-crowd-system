package com.smartcampus.crowd.service;

import com.smartcampus.crowd.model.SystemMode;
import com.smartcampus.crowd.repository.SystemModeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemModeService {

    private final SystemModeRepository repository;

    public String getCurrentMode() {
        return repository.findFirstByOrderByIdAsc()
                .map(SystemMode::getMode)
                .orElse("SIMULATED");
    }

    public void setMode(String mode) {
        log.info("Mode change request: {}", mode);
        SystemMode systemMode = repository.findFirstByOrderByIdAsc()
                .orElse(new SystemMode());
        systemMode.setMode(mode);
        repository.save(systemMode);
        log.info("System mode successfully updated to: {}", mode);
    }
}
