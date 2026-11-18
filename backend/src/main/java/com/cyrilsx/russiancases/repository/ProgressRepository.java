package com.cyrilsx.russiancases.repository;

import com.cyrilsx.russiancases.model.ProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<ProgressEntity, Long> {

    List<ProgressEntity> findByUserIdOrderByLastAttemptDesc(String userId);

    List<ProgressEntity> findAllByOrderByUserIdAsc();

    Optional<ProgressEntity> findByUserIdAndQuestionId(String userId, Long questionId);

    void deleteByUserId(String userId);

    void deleteByQuestionId(Long questionId);
}
