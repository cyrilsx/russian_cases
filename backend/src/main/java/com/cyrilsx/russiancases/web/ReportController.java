package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.service.ReportService;
import com.cyrilsx.russiancases.web.dto.ReportDto;
import com.cyrilsx.russiancases.web.dto.ReportRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/questions/{questionId}/report")
    public ResponseEntity<ReportDto> report(@PathVariable Long questionId,
                                            @Valid @RequestBody ReportRequest request,
                                            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createReport(authentication.getName(), questionId, request));
    }

    @GetMapping("/api/reports/me")
    public List<ReportDto> myReports(Authentication authentication) {
        return reportService.listMine(authentication.getName());
    }
}
