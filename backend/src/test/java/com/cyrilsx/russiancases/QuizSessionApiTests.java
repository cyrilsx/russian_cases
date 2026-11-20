package com.cyrilsx.russiancases;

import com.cyrilsx.russiancases.web.dto.QuestionDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class QuizSessionApiTests {

    @Autowired
    private TestRestTemplate rest;

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

    @Test
    void sessionReturnsRandomDistinctQuestionsWithoutCorrectIndex() {
        String token = registerUser();

        ResponseEntity<QuestionDto[]> all = rest.exchange("/api/levels/1/questions", HttpMethod.GET,
                new HttpEntity<>(auth(token)), QuestionDto[].class);
        Set<Long> levelQuestionIds = new HashSet<>();
        for (QuestionDto q : all.getBody()) {
            levelQuestionIds.add(q.id());
        }

        ResponseEntity<String> raw = rest.exchange("/api/levels/1/sessions", HttpMethod.POST,
                new HttpEntity<>(auth(token)), String.class);
        assertThat(raw.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(raw.getBody()).doesNotContain("correctIndex");

        ResponseEntity<Map> session = rest.exchange("/api/levels/1/sessions", HttpMethod.POST,
                new HttpEntity<>(auth(token)), Map.class);
        List<?> questions = (List<?>) session.getBody().get("questions");
        int total = ((Number) session.getBody().get("total")).intValue();
        assertThat(total).isEqualTo(15);
        assertThat(questions).hasSize(8);
        Set<Long> ids = new HashSet<>();
        for (Object q : questions) {
            ids.add(((Number) ((Map<?, ?>) q).get("id")).longValue());
        }
        assertThat(ids).hasSize(8);
        assertThat(levelQuestionIds).containsAll(ids);
        assertThat(((Number) session.getBody().get("answered")).intValue()).isZero();
    }

    @Test
    void answeringWithinSessionAllowsRetriesAndCountsDistinctQuestions() {
        String token = registerUser();

        ResponseEntity<Map> created = rest.exchange("/api/levels/2/sessions", HttpMethod.POST,
                new HttpEntity<>(auth(token)), Map.class);
        Long sessionId = ((Number) created.getBody().get("id")).longValue();
        List<Map<String, Object>> questions = (List<Map<String, Object>>) created.getBody().get("questions");
        Long questionId = ((Number) questions.get(0).get("id")).longValue();

        ResponseEntity<Map> first = rest.exchange("/api/sessions/" + sessionId + "/answer", HttpMethod.POST,
                new HttpEntity<>(Map.of("questionId", questionId, "optionIndex", 0), auth(token)), Map.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) first.getBody().get("answered")).intValue()).isEqualTo(1);
        assertThat(((Number) first.getBody().get("total")).intValue()).isEqualTo(15);

        ResponseEntity<Map> retry = rest.exchange("/api/sessions/" + sessionId + "/answer", HttpMethod.POST,
                new HttpEntity<>(Map.of("questionId", questionId, "optionIndex", 0), auth(token)), Map.class);
        assertThat(retry.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) retry.getBody().get("answered")).intValue()).isEqualTo(1);

        ResponseEntity<Map> notInSession = rest.exchange("/api/sessions/" + sessionId + "/answer", HttpMethod.POST,
                new HttpEntity<>(Map.of("questionId", 999999L, "optionIndex", 0), auth(token)), Map.class);
        assertThat(notInSession.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<Map> progress = rest.exchange("/api/progress/me/summary", HttpMethod.GET,
                new HttpEntity<>(auth(token)), Map.class);
        assertThat(((Number) progress.getBody().get("totalAttempts")).longValue()).isEqualTo(2);

        ResponseEntity<Map> fetched = rest.exchange("/api/sessions/" + sessionId, HttpMethod.GET,
                new HttpEntity<>(auth(token)), Map.class);
        assertThat(((Number) fetched.getBody().get("answered")).intValue()).isEqualTo(1);
    }

    @Test
    void sessionIsPrivateAndUnknownSession404() {
        String token = registerUser();
        String otherToken = registerUser();

        ResponseEntity<Map> created = rest.exchange("/api/levels/1/sessions", HttpMethod.POST,
                new HttpEntity<>(auth(token)), Map.class);
        Long sessionId = ((Number) created.getBody().get("id")).longValue();

        ResponseEntity<Map> foreign = rest.exchange("/api/sessions/" + sessionId, HttpMethod.GET,
                new HttpEntity<>(auth(otherToken)), Map.class);
        assertThat(foreign.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Map> unknown = rest.exchange("/api/sessions/424242", HttpMethod.GET,
                new HttpEntity<>(auth(token)), Map.class);
        assertThat(unknown.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Map> anon = rest.getForEntity("/api/sessions/" + sessionId, Map.class);
        assertThat(anon.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
