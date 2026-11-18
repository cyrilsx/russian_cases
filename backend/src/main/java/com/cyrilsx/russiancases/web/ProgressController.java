package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.service.ProgressService;
import com.cyrilsx.russiancases.web.dto.ProgressDto;
import com.cyrilsx.russiancases.web.dto.ProgressSummaryDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/me")
    public List<ProgressDto> getProgress(Locale locale, Authentication authentication) {
        return progressService.getProgress(authentication.getName(), locale.getLanguage());
    }

    @GetMapping("/me/summary")
    public ProgressSummaryDto getSummary(Locale locale, Authentication authentication) {
        return progressService.getSummary(authentication.getName(), locale.getLanguage());
    }
}
