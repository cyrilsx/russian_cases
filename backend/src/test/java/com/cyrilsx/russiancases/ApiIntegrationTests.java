package com.cyrilsx.russiancases;

import com.cyrilsx.russiancases.web.dto.AnswerResponse;
import com.cyrilsx.russiancases.web.dto.LevelDto;
import com.cyrilsx.russiancases.web.dto.ProgressSummaryDto;
import com.cyrilsx.russiancases.web.dto.QuestionDto;
import com.cyrilsx.russiancases.web.dto.RuleDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ApiIntegrationTests {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void levelsAreListedInOrder() {
        ResponseEntity<LevelDto[]> response = rest.getForEntity("/api/levels", LevelDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LevelDto[] levels = response.getBody();
        assertThat(levels).hasSize(3);
        assertThat(levels[0].levelNumber()).isEqualTo(1);
        assertThat(levels[0].questionCount()).isGreaterThan(0);
    }

    @Test
    void questionsAreLocalizedInFrench() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAcceptLanguageAsLocales(List.of(java.util.Locale.FRENCH));
        ResponseEntity<QuestionDto[]> response = rest.exchange(
                "/api/levels/1/questions", org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers), QuestionDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QuestionDto[] questions = response.getBody();
        assertThat(questions).isNotEmpty();
        assertThat(questions[0].prompt()).contains("Это");
        assertThat(questions[0].caseLabel()).isEqualTo("Nominatif");
    }

    @Test
    void questionsAreLocalizedInEnglish() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAcceptLanguageAsLocales(List.of(java.util.Locale.ENGLISH));
        ResponseEntity<QuestionDto[]> response = rest.exchange(
                "/api/levels/1/questions", org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers), QuestionDto[].class);
        QuestionDto[] questions = response.getBody();
        assertThat(questions).isNotEmpty();
        assertThat(questions[0].prompt()).contains("This is");
        assertThat(questions[0].caseLabel()).isEqualTo("Nominative");
    }

    @Test
    void answeringAQuestionUpdatesProgress() {
        ResponseEntity<QuestionDto[]> questions = rest.getForEntity("/api/levels/1/questions", QuestionDto[].class);
        QuestionDto first = questions.getBody()[0];

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of("userId", "test-user", "optionIndex", 0);
        ResponseEntity<AnswerResponse> answer = rest.postForEntity(
                "/api/questions/" + first.id() + "/answer", new HttpEntity<>(body, headers), AnswerResponse.class);
        assertThat(answer.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(answer.getBody().correct()).isTrue();
        assertThat(answer.getBody().ruleContent()).isNotBlank();

        ResponseEntity<ProgressSummaryDto> summary = rest.getForEntity("/api/progress/test-user/summary", ProgressSummaryDto.class);
        assertThat(summary.getBody().totalAttempts()).isEqualTo(1);
        assertThat(summary.getBody().totalCorrect()).isEqualTo(1);
        assertThat(summary.getBody().percent()).isEqualTo(100);
        assertThat(summary.getBody().levels()).isNotEmpty();
    }

    @Test
    void wrongAnswerIsRecorded() {
        ResponseEntity<QuestionDto[]> questions = rest.getForEntity("/api/levels/1/questions", QuestionDto[].class);
        QuestionDto first = questions.getBody()[0];
        int wrongIndex = first.options().size() - 1;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of("userId", "test-user-wrong", "optionIndex", wrongIndex);
        ResponseEntity<AnswerResponse> answer = rest.postForEntity(
                "/api/questions/" + first.id() + "/answer", new HttpEntity<>(body, headers), AnswerResponse.class);
        assertThat(answer.getBody().correct()).isFalse();
        assertThat(answer.getBody().correctIndex()).isNotEqualTo(wrongIndex);
    }

    @Test
    void rulesAreAvailablePerCaseAndLocalized() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAcceptLanguageAsLocales(List.of(java.util.Locale.ENGLISH));
        ResponseEntity<RuleDto[]> response = rest.exchange(
                "/api/rules?case=GENITIVE", org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers), RuleDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].caseLabel()).isEqualTo("Genitive");
        assertThat(response.getBody()[0].content()).contains("possession");
    }

    @Test
    void unknownQuestionReturnsLocalized404() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAcceptLanguageAsLocales(List.of(java.util.Locale.FRENCH));
        ResponseEntity<Map> response = rest.exchange(
                "/api/questions/999999", org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message").toString()).isEqualTo("Ressource introuvable");
    }
}
