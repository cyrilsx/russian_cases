package com.cyrilsx.russiancases.service;

import com.cyrilsx.russiancases.model.LevelEntity;
import com.cyrilsx.russiancases.model.ProgressEntity;
import com.cyrilsx.russiancases.model.QuestionEntity;
import com.cyrilsx.russiancases.model.RuleEntity;
import com.cyrilsx.russiancases.repository.LevelRepository;
import com.cyrilsx.russiancases.repository.ProgressRepository;
import com.cyrilsx.russiancases.repository.QuestionRepository;
import com.cyrilsx.russiancases.repository.RuleRepository;
import com.cyrilsx.russiancases.web.dto.AnswerRequest;
import com.cyrilsx.russiancases.web.dto.AnswerResponse;
import com.cyrilsx.russiancases.web.dto.LevelDto;
import com.cyrilsx.russiancases.web.dto.QuestionDto;
import com.cyrilsx.russiancases.web.dto.RuleDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class QuizService {

    private final LevelRepository levelRepository;
    private final QuestionRepository questionRepository;
    private final RuleRepository ruleRepository;
    private final ProgressRepository progressRepository;

    public QuizService(LevelRepository levelRepository,
                       QuestionRepository questionRepository,
                       RuleRepository ruleRepository,
                       ProgressRepository progressRepository) {
        this.levelRepository = levelRepository;
        this.questionRepository = questionRepository;
        this.ruleRepository = ruleRepository;
        this.progressRepository = progressRepository;
    }

    @Transactional(readOnly = true)
    public List<LevelDto> getLevels(String lang) {
        return levelRepository.findAllByOrderByLevelNumberAsc().stream()
                .map(level -> new LevelDto(
                        level.getId(),
                        level.getLevelNumber(),
                        level.getCode(),
                        "fr".equals(lang) ? level.getTitleFr() : level.getTitleEn(),
                        "fr".equals(lang) ? level.getDescriptionFr() : level.getDescriptionEn(),
                        questionRepository.countByLevelId(level.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestions(Long levelId, String lang) {
        LevelEntity level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("level", levelId));
        return questionRepository.findByLevelIdOrderByIdAsc(level.getId()).stream()
                .map(q -> toDto(q, lang))
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionDto getQuestion(Long questionId, String lang) {
        return toDto(findQuestion(questionId), lang);
    }

    @Transactional
    public AnswerResponse answer(Long questionId, AnswerRequest request, String lang) {
        QuestionEntity question = findQuestion(questionId);
        boolean correct = request.optionIndex() != null
                && request.optionIndex() >= 0
                && request.optionIndex() < question.getOptionsFr().size()
                && request.optionIndex() == question.getCorrectIndex();

        ProgressEntity progress = progressRepository
                .findByUserIdAndQuestionId(request.userId(), question.getId())
                .orElseGet(() -> {
                    ProgressEntity p = new ProgressEntity();
                    p.setUserId(request.userId());
                    p.setQuestion(question);
                    p.setAttempts(0);
                    p.setCorrectCount(0);
                    return p;
                });
        progress.setAttempts(progress.getAttempts() + 1);
        if (correct) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
        }
        progress.setLastAttempt(Instant.now());
        progressRepository.save(progress);

        RuleEntity rule = question.getRule();
        return new AnswerResponse(
                correct,
                question.getCorrectIndex(),
                "fr".equals(lang) ? question.getHintFr() : question.getHintEn(),
                rule == null ? null : ("fr".equals(lang) ? rule.getTitleFr() : rule.getTitleEn()),
                rule == null ? null : ("fr".equals(lang) ? rule.getContentFr() : rule.getContentEn()));
    }

    @Transactional(readOnly = true)
    public List<RuleDto> getRules(String caseName, String lang) {
        if (caseName == null || caseName.isBlank()) {
            return ruleRepository.findAll().stream()
                    .map(r -> toDto(r, lang))
                    .toList();
        }
        return ruleRepository.findByGrammaticalCase(com.cyrilsx.russiancases.model.GrammaticalCase.valueOf(caseName.toUpperCase()))
                .map(r -> List.of(toDto(r, lang)))
                .orElseThrow(() -> new ResourceNotFoundException("case", caseName));
    }

    @Transactional(readOnly = true)
    public RuleDto getRule(Long id, String lang) {
        return ruleRepository.findById(id)
                .map(r -> toDto(r, lang))
                .orElseThrow(() -> new ResourceNotFoundException("rule", id));
    }

    private QuestionEntity findQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("question", id));
    }

    private static QuestionDto toDto(QuestionEntity q, String lang) {
        boolean fr = "fr".equals(lang);
        return new QuestionDto(
                q.getId(),
                q.getLevel().getId(),
                q.getGrammaticalCase().name(),
                q.getGrammaticalCase().label(lang),
                fr ? q.getPromptFr() : q.getPromptEn(),
                fr ? q.getOptionsFr() : q.getOptionsEn(),
                fr ? q.getHintFr() : q.getHintEn());
    }

    private static RuleDto toDto(RuleEntity r, String lang) {
        return new RuleDto(
                r.getId(),
                r.getGrammaticalCase().name(),
                r.getGrammaticalCase().label(lang),
                "fr".equals(lang) ? r.getTitleFr() : r.getTitleEn(),
                "fr".equals(lang) ? r.getContentFr() : r.getContentEn());
    }
}
