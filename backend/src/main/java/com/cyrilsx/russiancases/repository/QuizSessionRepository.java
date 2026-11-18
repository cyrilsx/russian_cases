package com.cyrilsx.russiancases.repository;

import com.cyrilsx.russiancases.model.QuizSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface QuizSessionRepository extends JpaRepository<QuizSessionEntity, Long> {

    void deleteByCreatedAtBefore(Instant cutoff);
}
