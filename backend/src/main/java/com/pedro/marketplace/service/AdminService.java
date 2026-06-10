package com.pedro.marketplace.service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminService {

    private final Set<String> adminEmails;

    public AdminService(Environment environment) {
        this.adminEmails = normalizarEmails(
                environment.getProperty("app.admin.emails", ""),
                environment.getProperty("APP_ADMIN_EMAILS", ""),
                System.getenv("APP_ADMIN_EMAILS")
        );
    }

    public boolean ehAdmin(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return adminEmails.contains(email.trim().toLowerCase());
    }

    private Set<String> normalizarEmails(String... fontes) {
        return Arrays.stream(fontes)
                .flatMap(fonte -> fonte == null ? Stream.empty() : Arrays.stream(fonte.split(",")))
                .map(email -> email.trim().toLowerCase())
                .filter(email -> !email.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
