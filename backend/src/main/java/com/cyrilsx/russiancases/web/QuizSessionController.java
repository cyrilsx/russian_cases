package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.service.QuizSessionService;
import com.cyrilsx.russiancases.web.dto.QuizSessionDto;
import com.cyrilsx.russiancases.web.dto.SessionAnswerRequest;
import com.cyrilsx.russiancases.web.dto.SessionAnswerResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class QuizSessionController {

    private final QuizSessionService sessionService;

    public QuizSessionController(QuizSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/api/levels/{levelId}/sessions")
    public ResponseEntity<QuizSessionDto> createSession(@PathVariable Long levelId,
                                                        Locale locale,
                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSession(authentication.getName(), levelId, locale.getLanguage()));
    }

    @GetMapping("/api/sessions/{id}")
    public QuizSessionDto getSession(@PathVariable Long id, Locale locale, Authentication authentication) {
        return sessionService.getSession(id, authentication.getName(), locale.getLanguage());
    }

    @PostMapping("/api/sessions/{id}/answer")
    public SessionAnswerResponse answer(@PathVariable Long id,
                                        @Valid @RequestBody SessionAnswerRequest request,
                                        Locale locale,
                                        Authentication authentication) {
        return sessionService.answer(id, authentication.getName(), request, locale.getLanguage());
    }
}
