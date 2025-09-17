package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.domain.AuditLog;
import com.example.demo.domain.AuditRepository;
import com.example.demo.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserCreation(User user) {
        log.info("AuditService.logUserCreation user: {}", user);
        AuditLog auditLog = AuditLog.builder()
            .name(user.getName())
            .build();
        auditRepository.save(auditLog);
    }
}
