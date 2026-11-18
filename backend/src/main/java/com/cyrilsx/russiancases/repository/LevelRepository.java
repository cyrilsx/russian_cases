package com.cyrilsx.russiancases.repository;

import com.cyrilsx.russiancases.model.LevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelRepository extends JpaRepository<LevelEntity, Long> {

    List<LevelEntity> findAllByOrderByLevelNumberAsc();
}
