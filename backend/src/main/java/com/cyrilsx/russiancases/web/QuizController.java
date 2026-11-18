package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.service.QuizService;
import com.cyrilsx.russiancases.web.dto.AnswerRequest;
import com.cyrilsx.russiancases.web.dto.AnswerResponse;
import com.cyrilsx.russiancases.web.dto.QuestionDto;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/levels/{levelId}/questions")
    public List<QuestionDto> getQuestions(@PathVariable Long levelId, Locale locale) {
        return quizService.getQuestions(levelId, locale.getLanguage());
    }

    @GetMapping("/questions/{id}")
    public QuestionDto getQuestion(@PathVariable Long id, Locale locale) {
        return quizService.getQuestion(id, locale.getLanguage());
    }

    @PostMapping("/questions/{id}/answer")
    public AnswerResponse answer(@PathVariable Long id,
                                 @Valid @RequestBody AnswerRequest request,
                                 Locale locale,
                                 Authentication authentication) {
        return quizService.answer(id, request, authentication.getName(), locale.getLanguage());
    }
}
