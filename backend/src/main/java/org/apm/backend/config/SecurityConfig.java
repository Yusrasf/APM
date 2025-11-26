package org.apm.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // FHIR server open
                        .requestMatchers("/fhir/**").permitAll()
                        // allow your JSON login endpoint for everyone
                        .requestMatchers("/api/auth/login").permitAll()
                        // (for now) everything else also allowed - easier while developing
                        .anyRequest().permitAll()
                )

                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
