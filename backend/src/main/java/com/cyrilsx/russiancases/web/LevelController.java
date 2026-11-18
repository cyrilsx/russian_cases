package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.service.QuizService;
import com.cyrilsx.russiancases.web.dto.LevelDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/levels")
public class LevelController {

    private final QuizService quizService;

    public LevelController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public List<LevelDto> getLevels(Locale locale) {
        return quizService.getLevels(locale.getLanguage());
    }
}
