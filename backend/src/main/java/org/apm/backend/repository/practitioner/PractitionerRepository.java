package org.apm.backend.repository.practitioner;

import org.apm.backend.entity.practitioner.PractitionerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PractitionerRepository extends JpaRepository<PractitionerEntity, Long> {

    /**
     * Finds a practitioner by their unique login identifier (e.g. DOC123).
     */
    Optional<PractitionerEntity> findByIdentifier(String identifier);
}
