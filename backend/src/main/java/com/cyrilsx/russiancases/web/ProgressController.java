package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.service.ProgressService;
import com.cyrilsx.russiancases.web.dto.ProgressDto;
import com.cyrilsx.russiancases.web.dto.ProgressSummaryDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{userId}")
    public List<ProgressDto> getProgress(@PathVariable String userId, Locale locale) {
        return progressService.getProgress(userId, locale.getLanguage());
    }

    @GetMapping("/{userId}/summary")
    public ProgressSummaryDto getSummary(@PathVariable String userId, Locale locale) {
        return progressService.getSummary(userId, locale.getLanguage());
    }
}
