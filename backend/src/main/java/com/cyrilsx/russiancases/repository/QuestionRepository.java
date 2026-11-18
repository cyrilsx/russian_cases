package com.cyrilsx.russiancases.repository;

import com.cyrilsx.russiancases.model.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    List<QuestionEntity> findByLevelIdOrderByIdAsc(Long levelId);

    long countByLevelId(Long levelId);
}
