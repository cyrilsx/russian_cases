package com.cyrilsx.russiancases;

import com.cyrilsx.russiancases.web.dto.AdminQuestionDto;
import com.cyrilsx.russiancases.web.dto.QuestionDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReportApiTests {

    @Autowired
    private TestRestTemplate rest;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.setAcceptLanguageAsLocales(List.of(Locale.ENGLISH));
        return headers;
    }

    private String registerUser() {
        String username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        ResponseEntity<Map> response = rest.postForEntity("/api/auth/register",
                new HttpEntity<>(Map.of("username", username, "password", "secret123"), auth(null)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (String) response.getBody().get("token");
    }

    private String adminToken() {
        ResponseEntity<Map> response = rest.postForEntity("/api/auth/login",
                new HttpEntity<>(Map.of("username", adminUsername, "password", adminPassword), auth(null)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) response.getBody().get("token");
    }

    private long firstQuestionId(String token) {
        ResponseEntity<QuestionDto[]> questions = rest.exchange("/api/levels/1/questions", HttpMethod.GET,
                new HttpEntity<>(auth(token)), QuestionDto[].class);
        return questions.getBody()[0].id();
    }

    @Test
    void userCanReportAnswerWithJustificationAndSeeOwnReports() {
        String token = registerUser();
        long questionId = firstQuestionId(token);

        assertThat(rest.postForEntity("/api/questions/" + questionId + "/report",
                new HttpEntity<>(null, new HttpHeaders()), Map.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> empty = rest.exchange("/api/questions/" + questionId + "/report", HttpMethod.POST,
                new HttpEntity<>(Map.of("justification", "  "), auth(token)), Map.class);
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<Map> unknown = rest.exchange("/api/questions/424242/report", HttpMethod.POST,
                new HttpEntity<>(Map.of("justification", "no such question"), auth(token)), Map.class);
        assertThat(unknown.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Map> created = rest.exchange("/api/questions/" + questionId + "/report", HttpMethod.POST,
                new HttpEntity<>(Map.of("claimedCorrectIndex", 1, "justification", "The system answer is wrong, it should be option 2."),
                        auth(token)), Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().get("status")).isEqualTo("OPEN");
        assertThat(created.getBody().get("claimedCorrectIndex")).isEqualTo(1);
        Long reportId = ((Number) created.getBody().get("id")).longValue();

        ResponseEntity<Map> duplicate = rest.exchange("/api/questions/" + questionId + "/report", HttpMethod.POST,
                new HttpEntity<>(Map.of("justification", "again"), auth(token)), Map.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<Map[]> mine = rest.exchange("/api/reports/me", HttpMethod.GET,
                new HttpEntity<>(auth(token)), Map[].class);
        assertThat(mine.getBody()).anyMatch(r -> ((Number) r.get("id")).longValue() == reportId);
    }

    @Test
    void adminCanListAndResolveReportsAndFixCorrectIndex() {
        String userToken = registerUser();
        String admin = adminToken();
        long questionId = firstQuestionId(userToken);

        rest.exchange("/api/questions/" + questionId + "/report", HttpMethod.POST,
                new HttpEntity<>(Map.of("claimedCorrectIndex", 1, "justification", "should be option 2"), auth(userToken)),
                Map.class);

        ResponseEntity<Map> forbidden = rest.exchange("/api/admin/reports", HttpMethod.GET,
                new HttpEntity<>(auth(userToken)), Map.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Map[]> open = rest.exchange("/api/admin/reports?status=OPEN", HttpMethod.GET,
                new HttpEntity<>(auth(admin)), Map[].class);
        assertThat(open.getBody()).isNotEmpty();
        Map<String, Object> report = null;
        for (Map<String, Object> r : open.getBody()) {
            if ("should be option 2".equals(r.get("justification"))) {
                report = r;
            }
        }
        assertThat(report).isNotNull();
        long reportId = ((Number) report.get("id")).longValue();

        ResponseEntity<AdminQuestionDto[]> before = rest.exchange("/api/admin/questions?levelId=1", HttpMethod.GET,
                new HttpEntity<>(auth(admin)), AdminQuestionDto[].class);
        int oldCorrect = -1;
        for (AdminQuestionDto q : before.getBody()) {
            if (q.id() == questionId) oldCorrect = q.correctIndex();
        }
        assertThat(oldCorrect).isNotEqualTo(1);

        ResponseEntity<Map> resolved = rest.exchange("/api/admin/reports/" + reportId + "/resolve", HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", "ACCEPTED", "correctIndex", 1, "note", "fixed"), auth(admin)), Map.class);
        assertThat(resolved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resolved.getBody().get("status")).isEqualTo("ACCEPTED");
        assertThat(resolved.getBody().get("resolvedBy")).isEqualTo(adminUsername);

        ResponseEntity<AdminQuestionDto[]> after = rest.exchange("/api/admin/questions?levelId=1", HttpMethod.GET,
                new HttpEntity<>(auth(admin)), AdminQuestionDto[].class);
        for (AdminQuestionDto q : after.getBody()) {
            if (q.id() == questionId) assertThat(q.correctIndex()).isEqualTo(1);
        }

        ResponseEntity<Map> again = rest.exchange("/api/admin/reports/" + reportId + "/resolve", HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", "REJECTED"), auth(admin)), Map.class);
        assertThat(again.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<Map[]> rejectOpen = rest.exchange("/api/admin/reports?status=OPEN", HttpMethod.GET,
                new HttpEntity<>(auth(admin)), Map[].class);
        boolean stillOpen = false;
        for (Map<String, Object> r : rejectOpen.getBody()) {
            if (((Number) r.get("id")).longValue() == reportId) {
                stillOpen = true;
            }
        }
        assertThat(stillOpen).isFalse();
    }
}
