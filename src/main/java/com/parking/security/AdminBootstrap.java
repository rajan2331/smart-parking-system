package com.parking.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.parking.user.entity.Role;
import com.parking.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * On startup, promotes the already-registered user whose email is in ADMIN_EMAIL to ADMIN.
 * Admin can never be obtained through the public register endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }
        userRepository.findByEmail(adminEmail.trim()).ifPresentOrElse(user -> {
            if (user.getRole() != Role.ADMIN) {
                user.setRole(Role.ADMIN);
                userRepository.save(user);
                log.info("Promoted {} to ADMIN", user.getEmail());
            }
        }, () -> log.warn("ADMIN_EMAIL is set but no user is registered with {}", adminEmail));
    }
}
