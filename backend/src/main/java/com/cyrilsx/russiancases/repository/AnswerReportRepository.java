package com.cyrilsx.russiancases.repository;

import com.cyrilsx.russiancases.model.AnswerReportEntity;
import com.cyrilsx.russiancases.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerReportRepository extends JpaRepository<AnswerReportEntity, Long> {

    List<AnswerReportEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<AnswerReportEntity> findByStatusOrderByCreatedAtAsc(ReportStatus status);

    List<AnswerReportEntity> findAllByOrderByCreatedAtDesc();

    boolean existsByUserIdAndQuestionIdAndStatus(String userId, Long questionId, ReportStatus status);

    void deleteByQuestionId(Long questionId);
}
