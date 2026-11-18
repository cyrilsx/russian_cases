package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.model.GrammaticalCase;
import com.cyrilsx.russiancases.service.QuizService;
import com.cyrilsx.russiancases.web.dto.CaseDto;
import com.cyrilsx.russiancases.web.dto.RuleDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api")
public class RuleController {

    private final QuizService quizService;

    public RuleController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/cases")
    public List<CaseDto> getCases(Locale locale) {
        String lang = locale.getLanguage();
        return Arrays.stream(GrammaticalCase.values())
                .map(c -> new CaseDto(c.name(), c.label(lang)))
                .toList();
    }

    @GetMapping("/rules")
    public List<RuleDto> getRules(@RequestParam(name = "case", required = false) String caseName, Locale locale) {
        return quizService.getRules(caseName, locale.getLanguage());
    }

    @GetMapping("/rules/{id}")
    public RuleDto getRule(@PathVariable Long id, Locale locale) {
        return quizService.getRule(id, locale.getLanguage());
    }
}
