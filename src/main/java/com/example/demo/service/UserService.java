package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.controller.request.RequestJoin;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    // @Transactional
    public void join(RequestJoin requestJoin) {
        log.info("UserService.join requestJoin: {}", requestJoin);
        User user = User.builder()
            .name(requestJoin.name())
            .email(requestJoin.email())
            .build();
        userRepository.save(user);

        auditService.logUserCreation(user);
    }
}
