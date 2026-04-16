package com.example.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        List<AppUser> users
) {

    public record AppUser(
            String username,
            String password,
            List<String> roles
    ) {
    }
}
