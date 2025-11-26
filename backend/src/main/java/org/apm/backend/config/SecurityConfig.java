package org.apm.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Basic Spring Security configuration for the backend.
 *
 * Opens the FHIR endpoints and the JSON login endpoint, and disables
 * default form login / HTTP Basic while in development.
 */

@Configuration
public class SecurityConfig {
    /// Defines the main Spring Security filter chain.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                /// Configure which requests are allowed without authentication.
                .authorizeHttpRequests(auth -> auth
                        /// FHIR server open
                        .requestMatchers("/fhir/**").permitAll()
                        /// allow your JSON login endpoint for everyone
                        .requestMatchers("/api/auth/login").permitAll()
                        /// (for now) everything else also allowed - easier while developing
                        .anyRequest().permitAll()
                )
                /// Disable Spring's default HTML form login (/login)
                .formLogin(form -> form.disable())
                /// Disable HTTP Basic auth (no browser username/password dialog).
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
