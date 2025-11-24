package org.apm.backend.service;

import org.apm.backend.dto.practitioner.LoginRequestDTO;
import org.apm.backend.dto.practitioner.LoginResponseDTO;
import org.apm.backend.dto.practitioner.PractitionerHeaderDTO;
import org.apm.backend.model.PractitionerEntity;
import org.apm.backend.repository.PractitionerRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- Нужен для проверки хеша
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService using real data access and password checking.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final PractitionerRepository practitionerRepository;
    private final PasswordEncoder passwordEncoder; // Внедряем PasswordEncoder

    public AuthServiceImpl(PractitionerRepository practitionerRepository, PasswordEncoder passwordEncoder) {
        this.practitionerRepository = practitionerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO authenticate(LoginRequestDTO request) {

        // 1. Поиск пользователя по идентификатору (логину)
        PractitionerEntity entity = practitionerRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Invalid credentials: Practitioner not found"));

        // 2. Проверка пароля
        // Сравниваем предоставленный пароль (raw) с хешем в БД
        if (!passwordEncoder.matches(request.getPassword(), entity.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials: Password mismatch");
        }

        // 3. Если аутентификация успешна, создаем DTO и JWT-токен

        // В реальном приложении: сгенерировать реальный JWT с использованием id пользователя (entity.getId())
        String realJwtToken = "real_jwt_token_for_user_" + entity.getId() + "_[СЕКРЕТНЫЙ_ХЕШ]";

        PractitionerHeaderDTO practitionerDetails = new PractitionerHeaderDTO(
                entity.getId(),
                // Собираем полное имя
                String.format("%s %s %s", entity.getPrefix(), entity.getGivenName(), entity.getFamilyName()),
                entity.getOrganizationName()
        );

        return new LoginResponseDTO(realJwtToken, practitionerDetails);
    }
}