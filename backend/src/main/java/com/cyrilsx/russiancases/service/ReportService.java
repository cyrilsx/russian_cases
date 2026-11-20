package com.cyrilsx.russiancases.service;

import com.cyrilsx.russiancases.model.AnswerReportEntity;
import com.cyrilsx.russiancases.model.QuestionEntity;
import com.cyrilsx.russiancases.model.ReportStatus;
import com.cyrilsx.russiancases.repository.AnswerReportRepository;
import com.cyrilsx.russiancases.repository.QuestionRepository;
import com.cyrilsx.russiancases.web.dto.ReportDto;
import com.cyrilsx.russiancases.web.dto.ReportRequest;
import com.cyrilsx.russiancases.web.dto.ResolveReportRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class ReportService {

    private final AnswerReportRepository reportRepository;
    private final QuestionRepository questionRepository;

    public ReportService(AnswerReportRepository reportRepository, QuestionRepository questionRepository) {
        this.reportRepository = reportRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public ReportDto createReport(String userId, Long questionId, ReportRequest request) {
        QuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("question", questionId));
        if (request.claimedCorrectIndex() != null && request.claimedCorrectIndex() >= question.getOptionsFr().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badrequest");
        }
        if (reportRepository.existsByUserIdAndQuestionIdAndStatus(userId, questionId, ReportStatus.OPEN)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
        }
        AnswerReportEntity report = new AnswerReportEntity();
        report.setUserId(userId);
        report.setQuestionId(questionId);
        report.setClaimedCorrectIndex(request.claimedCorrectIndex());
        report.setJustification(request.justification());
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(Instant.now());
        return toDto(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReportDto> listMine(String userId) {
        return reportRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReportService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportDto> listForAdmin(ReportStatus status) {
        List<AnswerReportEntity> reports = status == null
                ? reportRepository.findAllByOrderByCreatedAtDesc()
                : reportRepository.findByStatusOrderByCreatedAtAsc(status);
        return reports.stream().map(ReportService::toDto).toList();
    }

    @Transactional
    public ReportDto resolve(Long id, ResolveReportRequest request, String adminUsername) {
        if (request.status() == ReportStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badrequest");
        }
        AnswerReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("report", id));
        if (report.getStatus() != ReportStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.inuse");
        }
        if (request.status() == ReportStatus.ACCEPTED && request.correctIndex() != null) {
            QuestionEntity question = questionRepository.findById(report.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("question", report.getQuestionId()));
            if (request.correctIndex() >= question.getOptionsFr().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badrequest");
            }
            question.setCorrectIndex(request.correctIndex());
            questionRepository.save(question);
        }
        report.setStatus(request.status());
        report.setResolvedBy(adminUsername);
        report.setResolvedAt(Instant.now());
        report.setResolutionNote(request.note());
        return toDto(reportRepository.save(report));
    }

    private static ReportDto toDto(AnswerReportEntity report) {
        return new ReportDto(
                report.getId(),
                report.getQuestionId(),
                report.getUserId(),
                report.getClaimedCorrectIndex(),
                report.getJustification(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getResolvedBy(),
                report.getResolvedAt(),
                report.getResolutionNote());
    }
}
