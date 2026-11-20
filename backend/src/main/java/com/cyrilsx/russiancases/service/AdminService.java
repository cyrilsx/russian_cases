package com.cyrilsx.russiancases.service;

import com.cyrilsx.russiancases.model.GrammaticalCase;
import com.cyrilsx.russiancases.model.LevelEntity;
import com.cyrilsx.russiancases.model.ProgressEntity;
import com.cyrilsx.russiancases.model.QuestionEntity;
import com.cyrilsx.russiancases.model.Role;
import com.cyrilsx.russiancases.model.RuleEntity;
import com.cyrilsx.russiancases.model.UserEntity;
import com.cyrilsx.russiancases.repository.AnswerReportRepository;
import com.cyrilsx.russiancases.repository.LevelRepository;
import com.cyrilsx.russiancases.repository.ProgressRepository;
import com.cyrilsx.russiancases.repository.QuestionRepository;
import com.cyrilsx.russiancases.repository.RuleRepository;
import com.cyrilsx.russiancases.repository.UserRepository;
import com.cyrilsx.russiancases.web.dto.AdminLevelDto;
import com.cyrilsx.russiancases.web.dto.AdminProgressDto;
import com.cyrilsx.russiancases.web.dto.AdminQuestionDto;
import com.cyrilsx.russiancases.web.dto.AdminRuleDto;
import com.cyrilsx.russiancases.web.dto.LevelUpsertRequest;
import com.cyrilsx.russiancases.web.dto.QuestionUpsertRequest;
import com.cyrilsx.russiancases.web.dto.RuleUpsertRequest;
import com.cyrilsx.russiancases.web.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {

    private final LevelRepository levelRepository;
    private final QuestionRepository questionRepository;
    private final RuleRepository ruleRepository;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final AnswerReportRepository reportRepository;

    public AdminService(LevelRepository levelRepository,
                        QuestionRepository questionRepository,
                        RuleRepository ruleRepository,
                        UserRepository userRepository,
                        ProgressRepository progressRepository,
                        AnswerReportRepository reportRepository) {
        this.levelRepository = levelRepository;
        this.questionRepository = questionRepository;
        this.ruleRepository = ruleRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminLevelDto> listLevels() {
        return levelRepository.findAllByOrderByLevelNumberAsc().stream()
                .map(l -> toLevelDto(l, questionRepository.countByLevelId(l.getId())))
                .toList();
    }

    @Transactional
    public AdminLevelDto createLevel(LevelUpsertRequest request) {
        LevelEntity level = new LevelEntity();
        applyLevel(level, request);
        level = levelRepository.save(level);
        return toLevelDto(level, 0);
    }

    @Transactional
    public AdminLevelDto updateLevel(Long id, LevelUpsertRequest request) {
        LevelEntity level = findLevel(id);
        applyLevel(level, request);
        level = levelRepository.save(level);
        return toLevelDto(level, questionRepository.countByLevelId(id));
    }

    @Transactional
    public void deleteLevel(Long id) {
        LevelEntity level = findLevel(id);
        if (questionRepository.countByLevelId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
        }
        levelRepository.delete(level);
    }

    @Transactional(readOnly = true)
    public List<AdminQuestionDto> listQuestions(Long levelId) {
        List<QuestionEntity> questions = levelId == null
                ? questionRepository.findAll()
                : questionRepository.findByLevelIdOrderByIdAsc(levelId);
        return questions.stream().map(AdminService::toQuestionDto).toList();
    }

    @Transactional
    public AdminQuestionDto createQuestion(QuestionUpsertRequest request) {
        QuestionEntity question = new QuestionEntity();
        applyQuestion(question, request);
        return toQuestionDto(questionRepository.save(question));
    }

    @Transactional
    public AdminQuestionDto updateQuestion(Long id, QuestionUpsertRequest request) {
        QuestionEntity question = findQuestion(id);
        applyQuestion(question, request);
        return toQuestionDto(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        QuestionEntity question = findQuestion(id);
        progressRepository.deleteByQuestionId(id);
        reportRepository.deleteByQuestionId(id);
        questionRepository.delete(question);
    }

    @Transactional(readOnly = true)
    public List<AdminRuleDto> listRules() {
        return ruleRepository.findAll().stream().map(AdminService::toRuleDto).toList();
    }

    @Transactional
    public AdminRuleDto createRule(RuleUpsertRequest request) {
        GrammaticalCase grammaticalCase = parseCase(request.caseName());
        if (ruleRepository.findByGrammaticalCase(grammaticalCase).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
        }
        RuleEntity rule = new RuleEntity();
        applyRule(rule, grammaticalCase, request);
        return toRuleDto(ruleRepository.save(rule));
    }

    @Transactional
    public AdminRuleDto updateRule(Long id, RuleUpsertRequest request) {
        RuleEntity rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("rule", id));
        GrammaticalCase grammaticalCase = parseCase(request.caseName());
        ruleRepository.findByGrammaticalCase(grammaticalCase)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
                });
        applyRule(rule, grammaticalCase, request);
        return toRuleDto(ruleRepository.save(rule));
    }

    @Transactional
    public void deleteRule(Long id) {
        RuleEntity rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("rule", id));
        for (QuestionEntity question : questionRepository.findByRuleId(id)) {
            question.setRule(null);
            questionRepository.save(question);
        }
        ruleRepository.delete(rule);
    }

    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userRepository.findAll().stream().map(AdminService::toUserDto).toList();
    }

    @Transactional
    public UserDto setRole(Long id, Role role, String currentUsername) {
        UserEntity user = findUser(id);
        if (user.getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
        }
        if (user.getRole() == Role.ADMIN && role == Role.USER && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
        }
        user.setRole(role);
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id, String currentUsername) {
        UserEntity user = findUser(id);
        if (user.getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
        }
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
        }
        progressRepository.deleteByUserId(user.getUsername());
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public List<AdminProgressDto> listProgress() {
        return progressRepository.findAllByOrderByUserIdAsc().stream()
                .map(p -> new AdminProgressDto(
                        p.getUserId(),
                        p.getQuestion().getId(),
                        p.getAttempts(),
                        p.getCorrectCount(),
                        p.getLastAttempt()))
                .toList();
    }

    @Transactional
    public void resetProgress(String userId) {
        progressRepository.deleteByUserId(userId);
    }

    private LevelEntity findLevel(Long id) {
        return levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("level", id));
    }

    private QuestionEntity findQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("question", id));
    }

    private UserEntity findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user", id));
    }

    private void applyLevel(LevelEntity level, LevelUpsertRequest request) {
        level.setLevelNumber(request.levelNumber());
        level.setCode(request.code());
        level.setTitleFr(request.titleFr());
        level.setTitleEn(request.titleEn());
        level.setDescriptionFr(request.descriptionFr());
        level.setDescriptionEn(request.descriptionEn());
    }

    private void applyQuestion(QuestionEntity question, QuestionUpsertRequest request) {
        LevelEntity level = findLevel(request.levelId());
        GrammaticalCase grammaticalCase = parseCase(request.caseName());
        if (request.optionsFr().size() != request.optionsEn().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badrequest");
        }
        if (request.correctIndex() >= request.optionsFr().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badrequest");
        }
        RuleEntity rule = null;
        if (request.ruleId() != null) {
            rule = ruleRepository.findById(request.ruleId())
                    .orElseThrow(() -> new ResourceNotFoundException("rule", request.ruleId()));
        }
        question.setLevel(level);
        question.setGrammaticalCase(grammaticalCase);
        question.setPromptFr(request.promptFr());
        question.setPromptEn(request.promptEn());
        question.setOptionsFr(request.optionsFr());
        question.setOptionsEn(request.optionsEn());
        question.setCorrectIndex(request.correctIndex());
        question.setHintFr(request.hintFr());
        question.setHintEn(request.hintEn());
        question.setRule(rule);
    }

    private void applyRule(RuleEntity rule, GrammaticalCase grammaticalCase, RuleUpsertRequest request) {
        rule.setGrammaticalCase(grammaticalCase);
        rule.setTitleFr(request.titleFr());
        rule.setTitleEn(request.titleEn());
        rule.setContentFr(request.contentFr());
        rule.setContentEn(request.contentEn());
    }

    private static GrammaticalCase parseCase(String caseName) {
        try {
            return GrammaticalCase.valueOf(caseName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badrequest");
        }
    }

    private static AdminLevelDto toLevelDto(LevelEntity level, long questionCount) {
        return new AdminLevelDto(
                level.getId(),
                level.getLevelNumber(),
                level.getCode(),
                level.getTitleFr(),
                level.getTitleEn(),
                level.getDescriptionFr(),
                level.getDescriptionEn(),
                questionCount);
    }

    private static AdminQuestionDto toQuestionDto(QuestionEntity q) {
        return new AdminQuestionDto(
                q.getId(),
                q.getLevel().getId(),
                q.getGrammaticalCase().name(),
                q.getPromptFr(),
                q.getPromptEn(),
                q.getOptionsFr(),
                q.getOptionsEn(),
                q.getCorrectIndex(),
                q.getHintFr(),
                q.getHintEn(),
                q.getRule() == null ? null : q.getRule().getId());
    }

    private static AdminRuleDto toRuleDto(RuleEntity r) {
        return new AdminRuleDto(
                r.getId(),
                r.getGrammaticalCase().name(),
                r.getTitleFr(),
                r.getTitleEn(),
                r.getContentFr(),
                r.getContentEn());
    }

    private static UserDto toUserDto(UserEntity u) {
        return new UserDto(u.getId(), u.getUsername(), u.getRole().name(), u.getCreatedAt());
    }
}
