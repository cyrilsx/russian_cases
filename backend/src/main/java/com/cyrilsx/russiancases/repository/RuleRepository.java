package com.cyrilsx.russiancases.repository;

import com.cyrilsx.russiancases.model.GrammaticalCase;
import com.cyrilsx.russiancases.model.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RuleRepository extends JpaRepository<RuleEntity, Long> {

    Optional<RuleEntity> findByGrammaticalCase(GrammaticalCase grammaticalCase);
}
