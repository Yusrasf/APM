package org.apm.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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
                        // everything else must be authenticated
                        .anyRequest().authenticated()
                )

                // ✅ enable default Spring login page at /login
                .formLogin(Customizer.withDefaults())

                // (optional) enable logout at /logout
                .logout(Customizer.withDefaults());

        return http.build();
    }
}
