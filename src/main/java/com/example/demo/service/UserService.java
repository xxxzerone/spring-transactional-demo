package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.controller.request.RequestJoin;
import com.example.demo.domain.AuditLog;
import com.example.demo.domain.AuditRepository;
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
    private final AuditRepository auditRepository;

    @Transactional
    public void join(RequestJoin requestJoin) throws InterruptedException {
        log.info("UserService.join requestJoin: {}", requestJoin);
        User user = User.builder()
            .name(requestJoin.name())
            .email(requestJoin.email())
            .build();        

        log.info("Waiting for a time-consuming task that does not need a database connection ...");
        
        log.info("Thread.sleep start >>");
        Thread.sleep(40_000);
        log.info("Thread.sleep end >>");

        log.info("Done, now query the database ...");
        log.info("The database connection should be acquired now ...");

        userRepository.save(user);

        Thread.sleep(40_000);
        log.info("userRepository.save() done.");

        // auditService.logUserCreation(user);
        // persistAuditLog(user);
        // notiftyAuditLog(user);
    }    

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected long persistAuditLog(User user) {
        log.info("UserService.persistAuditLog user: {}", user);
        auditRepository.save(
            AuditLog.builder()
                .name(user.getName())
                .build()
        );
        return auditRepository.count();
    }

    private void notiftyAuditLog(User user) {
        log.info("UserService.notiftyAuditLog user: {}", user);
    }
}
