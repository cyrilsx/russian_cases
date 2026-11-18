package com.cyrilsx.russiancases.service;

import com.cyrilsx.russiancases.model.LevelEntity;
import com.cyrilsx.russiancases.model.ProgressEntity;
import com.cyrilsx.russiancases.repository.LevelRepository;
import com.cyrilsx.russiancases.repository.ProgressRepository;
import com.cyrilsx.russiancases.repository.QuestionRepository;
import com.cyrilsx.russiancases.web.dto.LevelProgressDto;
import com.cyrilsx.russiancases.web.dto.ProgressDto;
import com.cyrilsx.russiancases.web.dto.ProgressSummaryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final LevelRepository levelRepository;
    private final QuestionRepository questionRepository;

    public ProgressService(ProgressRepository progressRepository,
                           LevelRepository levelRepository,
                           QuestionRepository questionRepository) {
        this.progressRepository = progressRepository;
        this.levelRepository = levelRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public List<ProgressDto> getProgress(String userId, String lang) {
        boolean fr = "fr".equals(lang);
        return progressRepository.findByUserIdOrderByLastAttemptDesc(userId).stream()
                .map(p -> new ProgressDto(
                        p.getQuestion().getId(),
                        fr ? p.getQuestion().getPromptFr() : p.getQuestion().getPromptEn(),
                        p.getAttempts(),
                        p.getCorrectCount(),
                        p.getLastAttempt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgressSummaryDto getSummary(String userId, String lang) {
        List<ProgressEntity> progress = progressRepository.findByUserIdOrderByLastAttemptDesc(userId);
        long totalAttempts = progress.stream().mapToLong(ProgressEntity::getAttempts).sum();
        long totalCorrect = progress.stream().mapToLong(ProgressEntity::getCorrectCount).sum();

        List<LevelProgressDto> levels = levelRepository.findAllByOrderByLevelNumberAsc().stream()
                .map(level -> toLevelProgress(level, progress, lang))
                .toList();

        return new ProgressSummaryDto(
                userId,
                totalAttempts,
                totalCorrect,
                totalAttempts == 0 ? 0 : (int) Math.round(100.0 * totalCorrect / totalAttempts),
                levels);
    }

    private LevelProgressDto toLevelProgress(LevelEntity level, List<ProgressEntity> progress, String lang) {
        long total = questionRepository.countByLevelId(level.getId());
        long correct = progress.stream()
                .filter(p -> p.getQuestion().getLevel().getId().equals(level.getId()))
                .mapToLong(ProgressEntity::getCorrectCount)
                .sum();
        return new LevelProgressDto(
                level.getId(),
                "fr".equals(lang) ? level.getTitleFr() : level.getTitleEn(),
                total,
                correct,
                total == 0 ? 0 : (int) Math.round(100.0 * correct / total));
    }
}
