package org.apm.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;

// Исключаем класс, который генерирует пароль по умолчанию
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@ServletComponentScan
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}