package com.cyrilsx.russiancases;

import com.cyrilsx.russiancases.web.dto.AnswerResponse;
import com.cyrilsx.russiancases.web.dto.LevelDto;
import com.cyrilsx.russiancases.web.dto.ProgressSummaryDto;
import com.cyrilsx.russiancases.web.dto.QuestionDto;
import com.cyrilsx.russiancases.web.dto.RuleDto;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ApiIntegrationTests {

    @Autowired
    private TestRestTemplate rest;

    private String registerUser() {
        String username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        HttpHeaders headers = jsonHeaders();
        ResponseEntity<Map> response = rest.postForEntity("/api/auth/register",
                new HttpEntity<>(Map.of("username", username, "password", "secret123"), headers), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (String) response.getBody().get("token");
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders authHeaders(String token, Locale locale) {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(token);
        headers.setAcceptLanguageAsLocales(List.of(locale));
        return headers;
    }

    @Test
    void unauthenticatedRequestReturns401() {
        ResponseEntity<Map> response = rest.getForEntity("/api/levels", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void registerLoginAndMe() {
        String username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        HttpHeaders headers = jsonHeaders();

        ResponseEntity<Map> registered = rest.postForEntity("/api/auth/register",
                new HttpEntity<>(Map.of("username", username, "password", "secret123"), headers), Map.class);
        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String token = (String) registered.getBody().get("token");
        assertThat(token).isNotBlank();

        ResponseEntity<Map> duplicate = rest.postForEntity("/api/auth/register",
                new HttpEntity<>(Map.of("username", username, "password", "secret123"), headers), Map.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<Map> wrongPassword = rest.postForEntity("/api/auth/login",
                new HttpEntity<>(Map.of("username", username, "password", "wrong-pass"), headers), Map.class);
        assertThat(wrongPassword.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> login = rest.postForEntity("/api/auth/login",
                new HttpEntity<>(Map.of("username", username, "password", "secret123"), headers), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody().get("username")).isEqualTo(username);

        ResponseEntity<Map> me = rest.exchange("/api/auth/me", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token, Locale.ENGLISH)), Map.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody().get("username")).isEqualTo(username);
        assertThat(me.getBody().get("role")).isEqualTo("USER");
    }

    @Test
    void invalidTokenReturns401() {
        ResponseEntity<Map> response = rest.exchange("/api/levels", HttpMethod.GET,
                new HttpEntity<>(authHeaders("not-a-real-token", Locale.ENGLISH)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void levelsAreListedInOrder() {
        ResponseEntity<LevelDto[]> response = rest.exchange("/api/levels", HttpMethod.GET,
                new HttpEntity<>(authHeaders(registerUser(), Locale.ENGLISH)), LevelDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LevelDto[] levels = response.getBody();
        assertThat(levels).hasSize(3);
        assertThat(levels[0].levelNumber()).isEqualTo(1);
        assertThat(levels[0].questionCount()).isGreaterThan(0);
    }

    @Test
    void questionsAreLocalizedInFrench() {
        ResponseEntity<QuestionDto[]> response = rest.exchange("/api/levels/1/questions", HttpMethod.GET,
                new HttpEntity<>(authHeaders(registerUser(), Locale.FRENCH)), QuestionDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QuestionDto[] questions = response.getBody();
        assertThat(questions).isNotEmpty();
        assertThat(questions[0].prompt()).contains("Это");
        assertThat(questions[0].caseLabel()).isEqualTo("Nominatif");
    }

    @Test
    void questionsAreLocalizedInEnglish() {
        ResponseEntity<QuestionDto[]> response = rest.exchange("/api/levels/1/questions", HttpMethod.GET,
                new HttpEntity<>(authHeaders(registerUser(), Locale.ENGLISH)), QuestionDto[].class);
        QuestionDto[] questions = response.getBody();
        assertThat(questions).isNotEmpty();
        assertThat(questions[0].prompt()).contains("This is");
        assertThat(questions[0].caseLabel()).isEqualTo("Nominative");
    }

    @Test
    void questionPayloadDoesNotExposeCorrectIndex() {
        ResponseEntity<QuestionDto[]> questions = rest.exchange("/api/levels/1/questions", HttpMethod.GET,
                new HttpEntity<>(authHeaders(registerUser(), Locale.ENGLISH)), QuestionDto[].class);
        Long id = questions.getBody()[0].id();
        ResponseEntity<String> raw = rest.exchange("/api/questions/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders(registerUser(), Locale.ENGLISH)), String.class);
        assertThat(raw.getBody()).doesNotContain("correctIndex");
    }

    @Test
    void answeringAQuestionUpdatesProgress() {
        String token = registerUser();
        ResponseEntity<QuestionDto[]> questions = rest.exchange("/api/levels/1/questions", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token, Locale.ENGLISH)), QuestionDto[].class);
        QuestionDto first = questions.getBody()[0];

        ResponseEntity<AnswerResponse> answer = rest.postForEntity(
                "/api/questions/" + first.id() + "/answer",
                new HttpEntity<>(Map.of("optionIndex", 0), authHeaders(token, Locale.ENGLISH)),
                AnswerResponse.class);
        assertThat(answer.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(answer.getBody().correct()).isTrue();
        assertThat(answer.getBody().ruleContent()).isNotBlank();

        ResponseEntity<ProgressSummaryDto> summary = rest.exchange("/api/progress/me/summary", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token, Locale.ENGLISH)), ProgressSummaryDto.class);
        assertThat(summary.getBody().totalAttempts()).isEqualTo(1);
        assertThat(summary.getBody().totalCorrect()).isEqualTo(1);
        assertThat(summary.getBody().percent()).isEqualTo(100);
        assertThat(summary.getBody().levels()).isNotEmpty();
    }

    @Test
    void wrongAnswerIsRecorded() {
        String token = registerUser();
        ResponseEntity<QuestionDto[]> questions = rest.exchange("/api/levels/1/questions", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token, Locale.ENGLISH)), QuestionDto[].class);
        QuestionDto first = questions.getBody()[0];
        int wrongIndex = first.options().size() - 1;

        ResponseEntity<AnswerResponse> answer = rest.postForEntity(
                "/api/questions/" + first.id() + "/answer",
                new HttpEntity<>(Map.of("optionIndex", wrongIndex), authHeaders(token, Locale.ENGLISH)),
                AnswerResponse.class);
        assertThat(answer.getBody().correct()).isFalse();
        assertThat(answer.getBody().correctIndex()).isNotEqualTo(wrongIndex);
    }

    @Test
    void rulesAreAvailablePerCaseAndLocalized() {
        ResponseEntity<RuleDto[]> response = rest.exchange("/api/rules?case=GENITIVE", HttpMethod.GET,
                new HttpEntity<>(authHeaders(registerUser(), Locale.ENGLISH)), RuleDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].caseLabel()).isEqualTo("Genitive");
        assertThat(response.getBody()[0].content()).contains("possession");
    }

    @Test
    void unknownQuestionReturnsLocalized404() {
        ResponseEntity<Map> response = rest.exchange("/api/questions/999999", HttpMethod.GET,
                new HttpEntity<>(authHeaders(registerUser(), Locale.FRENCH)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message").toString()).isEqualTo("Ressource introuvable");
    }
}
