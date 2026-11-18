package com.cyrilsx.russiancases.service;

import com.cyrilsx.russiancases.model.LevelEntity;
import com.cyrilsx.russiancases.model.QuestionEntity;
import com.cyrilsx.russiancases.model.QuizSessionEntity;
import com.cyrilsx.russiancases.repository.LevelRepository;
import com.cyrilsx.russiancases.repository.QuestionRepository;
import com.cyrilsx.russiancases.repository.QuizSessionRepository;
import com.cyrilsx.russiancases.web.dto.AnswerRequest;
import com.cyrilsx.russiancases.web.dto.AnswerResponse;
import com.cyrilsx.russiancases.web.dto.QuizSessionDto;
import com.cyrilsx.russiancases.web.dto.SessionAnswerRequest;
import com.cyrilsx.russiancases.web.dto.SessionAnswerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class QuizSessionService {

    private final QuizSessionRepository sessionRepository;
    private final LevelRepository levelRepository;
    private final QuestionRepository questionRepository;
    private final QuizService quizService;
    private final int questionsPerSession;

    public QuizSessionService(QuizSessionRepository sessionRepository,
                              LevelRepository levelRepository,
                              QuestionRepository questionRepository,
                              QuizService quizService,
                              @Value("${app.quiz.questions-per-session:8}") int questionsPerSession) {
        this.sessionRepository = sessionRepository;
        this.levelRepository = levelRepository;
        this.questionRepository = questionRepository;
        this.quizService = quizService;
        this.questionsPerSession = questionsPerSession;
    }

    @Transactional
    public QuizSessionDto createSession(String userId, Long levelId, String lang) {
        LevelEntity level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("level", levelId));
        List<QuestionEntity> all = questionRepository.findByLevelIdOrderByIdAsc(level.getId());
        if (all.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badrequest");
        }
        List<QuestionEntity> shuffled = new ArrayList<>(all);
        Collections.shuffle(shuffled);
        List<QuestionEntity> picked = shuffled.stream()
                .limit(Math.min(questionsPerSession, shuffled.size()))
                .toList();

        sessionRepository.deleteByCreatedAtBefore(Instant.now().minus(Duration.ofHours(24)));

        QuizSessionEntity session = new QuizSessionEntity();
        session.setUserId(userId);
        session.setLevelId(level.getId());
        session.setQuestionIds(picked.stream().map(QuestionEntity::getId).toList());
        session.setCreatedAt(Instant.now());
        session = sessionRepository.save(session);
        return toDto(session, picked, lang);
    }

    @Transactional(readOnly = true)
    public QuizSessionDto getSession(Long id, String userId, String lang) {
        QuizSessionEntity session = findOwned(id, userId);
        List<QuestionEntity> questions = session.getQuestionIds().stream()
                .map(questionRepository::findById)
                .map(q -> q.orElseThrow(() -> new ResourceNotFoundException("question", id)))
                .toList();
        return toDto(session, questions, lang);
    }

    @Transactional
    public SessionAnswerResponse answer(Long sessionId, String userId, SessionAnswerRequest request, String lang) {
        QuizSessionEntity session = findOwned(sessionId, userId);
        if (!session.getQuestionIds().contains(request.questionId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badrequest");
        }
        if (session.getAnsweredIds().contains(request.questionId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
        }
        session.getAnsweredIds().add(request.questionId());
        sessionRepository.save(session);

        AnswerResponse result = quizService.answer(
                request.questionId(), new AnswerRequest(request.optionIndex()), userId, lang);
        return new SessionAnswerResponse(
                result.correct(),
                result.correctIndex(),
                result.hint(),
                result.ruleTitle(),
                result.ruleContent(),
                session.getAnsweredIds().size(),
                session.getQuestionIds().size());
    }

    private QuizSessionEntity findOwned(Long id, String userId) {
        QuizSessionEntity session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("session", id));
        if (!session.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("session", id);
        }
        return session;
    }

    private static QuizSessionDto toDto(QuizSessionEntity session, List<QuestionEntity> questions, String lang) {
        return new QuizSessionDto(
                session.getId(),
                session.getLevelId(),
                session.getQuestionIds().size(),
                session.getAnsweredIds().size(),
                questions.stream().map(q -> QuizService.toDto(q, lang)).toList());
    }
}
