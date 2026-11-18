package com.cyrilsx.russiancases;

import com.cyrilsx.russiancases.web.dto.AdminLevelDto;
import com.cyrilsx.russiancases.web.dto.AdminQuestionDto;
import com.cyrilsx.russiancases.web.dto.AdminRuleDto;
import com.cyrilsx.russiancases.web.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminApiTests {

    @Autowired
    private TestRestTemplate rest;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    private HttpHeaders json() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = json();
        headers.setBearerAuth(token);
        headers.setAcceptLanguageAsLocales(List.of(Locale.ENGLISH));
        return headers;
    }

    private String registerUser() {
        return registerUserAndGetToken()[0];
    }

    private String[] registerUserAndGetToken() {
        String username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        ResponseEntity<Map> response = rest.postForEntity("/api/auth/register",
                new HttpEntity<>(Map.of("username", username, "password", "secret123"), json()), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return new String[]{(String) response.getBody().get("token"), username};
    }

    private String login(String username, String password) {
        ResponseEntity<Map> response = rest.postForEntity("/api/auth/login",
                new HttpEntity<>(Map.of("username", username, "password", password), json()), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) response.getBody().get("token");
    }

    private String adminToken() {
        return login(adminUsername, adminPassword);
    }

    @Test
    void adminEndpointsRejectAnonymousAndNormalUsers() {
        assertThat(rest.getForEntity("/api/admin/levels", Map.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        String userToken = registerUser();
        ResponseEntity<Map> forbidden = rest.exchange("/api/admin/levels", HttpMethod.GET,
                new HttpEntity<>(auth(userToken)), Map.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(forbidden.getBody().get("message").toString()).contains("admin");

        ResponseEntity<Map> forbiddenPost = rest.exchange("/api/admin/levels", HttpMethod.POST,
                new HttpEntity<>(Map.of("levelNumber", 99, "code", "X99", "titleFr", "t", "titleEn", "t",
                        "descriptionFr", "d", "descriptionEn", "d"), auth(userToken)), Map.class);
        assertThat(forbiddenPost.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void bootstrapAdminExistsAndHasAdminRole() {
        String token = adminToken();
        ResponseEntity<Map> me = rest.exchange("/api/auth/me", HttpMethod.GET,
                new HttpEntity<>(auth(token)), Map.class);
        assertThat(me.getBody().get("username")).isEqualTo(adminUsername);
        assertThat(me.getBody().get("role")).isEqualTo("ADMIN");
    }

    @Test
    void adminLevelCrudAndDeleteGuard() {
        String token = adminToken();

        ResponseEntity<AdminLevelDto> created = rest.exchange("/api/admin/levels", HttpMethod.POST,
                new HttpEntity<>(Map.of("levelNumber", 42, "code", "T42", "titleFr", "Niveau test",
                        "titleEn", "Test level", "descriptionFr", "desc", "descriptionEn", "desc"), auth(token)),
                AdminLevelDto.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = created.getBody().id();
        assertThat(created.getBody().questionCount()).isZero();

        ResponseEntity<AdminLevelDto[]> list = rest.exchange("/api/admin/levels", HttpMethod.GET,
                new HttpEntity<>(auth(token)), AdminLevelDto[].class);
        assertThat(list.getBody()).anyMatch(l -> l.id().equals(id));

        ResponseEntity<AdminLevelDto> updated = rest.exchange("/api/admin/levels/" + id, HttpMethod.PUT,
                new HttpEntity<>(Map.of("levelNumber", 42, "code", "T42", "titleFr", "Niveau modifié",
                        "titleEn", "Updated level", "descriptionFr", "desc", "descriptionEn", "desc"), auth(token)),
                AdminLevelDto.class);
        assertThat(updated.getBody().titleEn()).isEqualTo("Updated level");

        ResponseEntity<Void> deleted = rest.exchange("/api/admin/levels/" + id, HttpMethod.DELETE,
                new HttpEntity<>(auth(token)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> gone = rest.exchange("/api/admin/levels/" + id, HttpMethod.DELETE,
                new HttpEntity<>(auth(token)), Map.class);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Map> seededLevelHasQuestions = rest.exchange("/api/admin/levels/1", HttpMethod.DELETE,
                new HttpEntity<>(auth(token)), Map.class);
        assertThat(seededLevelHasQuestions.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void adminQuestionCrudAndValidation() {
        String token = adminToken();

        ResponseEntity<AdminRuleDto[]> rules = rest.exchange("/api/admin/rules", HttpMethod.GET,
                new HttpEntity<>(auth(token)), AdminRuleDto[].class);
        long dativeRuleId = List.of(rules.getBody()).stream()
                .filter(r -> r.caseName().equals("DATIVE")).findFirst().orElseThrow().id();

        Map<String, Object> valid = Map.of(
                "levelId", 2,
                "caseName", "DATIVE",
                "promptFr", "Je donne ___ (livre).",
                "promptEn", "I give a ___ (book).",
                "optionsFr", List.of("livre", "livres", "livreau", "livrette"),
                "optionsEn", List.of("knigu", "knigi", "knige", "knigoy"),
                "correctIndex", 0,
                "hintFr", "Datif.",
                "hintEn", "Dative.",
                "ruleId", dativeRuleId);
        ResponseEntity<AdminQuestionDto> created = rest.exchange("/api/admin/questions", HttpMethod.POST,
                new HttpEntity<>(valid, auth(token)), AdminQuestionDto.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = created.getBody().id();
        assertThat(created.getBody().caseName()).isEqualTo("DATIVE");
        assertThat(created.getBody().ruleId()).isEqualTo(dativeRuleId);

        ResponseEntity<AdminQuestionDto[]> list = rest.exchange("/api/admin/questions?levelId=2", HttpMethod.GET,
                new HttpEntity<>(auth(token)), AdminQuestionDto[].class);
        assertThat(list.getBody()).anyMatch(q -> q.id().equals(id));

        Map<String, Object> invalid = new java.util.HashMap<>(valid);
        invalid.put("correctIndex", 99);
        ResponseEntity<Map> bad = rest.exchange("/api/admin/questions", HttpMethod.POST,
                new HttpEntity<>(invalid, auth(token)), Map.class);
        assertThat(bad.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> unknownCase = new java.util.HashMap<>(valid);
        unknownCase.put("caseName", "VOCATIVE");
        ResponseEntity<Map> badCase = rest.exchange("/api/admin/questions", HttpMethod.POST,
                new HttpEntity<>(unknownCase, auth(token)), Map.class);
        assertThat(badCase.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<Void> deleted = rest.exchange("/api/admin/questions/" + id, HttpMethod.DELETE,
                new HttpEntity<>(auth(token)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void adminRuleCrudAndOneRulePerCase() {
        String token = adminToken();

        ResponseEntity<AdminRuleDto[]> rules = rest.exchange("/api/admin/rules", HttpMethod.GET,
                new HttpEntity<>(auth(token)), AdminRuleDto[].class);
        assertThat(rules.getBody()).hasSize(6);
        AdminRuleDto genitive = List.of(rules.getBody()).stream()
                .filter(r -> r.caseName().equals("GENITIVE")).findFirst().orElseThrow();

        ResponseEntity<AdminRuleDto> updated = rest.exchange("/api/admin/rules/" + genitive.id(), HttpMethod.PUT,
                new HttpEntity<>(Map.of("caseName", "GENITIVE", "titleFr", "Génitif (édité)", "titleEn", "Genitive (edited)",
                        "contentFr", "contenu", "contentEn", "content"), auth(token)), AdminRuleDto.class);
        assertThat(updated.getBody().titleEn()).isEqualTo("Genitive (edited)");

        ResponseEntity<Map> duplicate = rest.exchange("/api/admin/rules", HttpMethod.POST,
                new HttpEntity<>(Map.of("caseName", "GENITIVE", "titleFr", "x", "titleEn", "x",
                        "contentFr", "x", "contentEn", "x"), auth(token)), Map.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void adminUserManagementAndRoleFreshness() {
        String adminToken = adminToken();
        String[] credentials = registerUserAndGetToken();
        String userToken = credentials[0];
        String username = credentials[1];

        ResponseEntity<Map> me = rest.exchange("/api/auth/me", HttpMethod.GET,
                new HttpEntity<>(auth(userToken)), Map.class);
        assertThat(me.getBody().get("role")).isEqualTo("USER");

        ResponseEntity<UserDto[]> users = rest.exchange("/api/admin/users", HttpMethod.GET,
                new HttpEntity<>(auth(adminToken)), UserDto[].class);
        UserDto target = List.of(users.getBody()).stream()
                .filter(u -> u.username().equals(username)).findFirst().orElseThrow();
        assertThat(target.role()).isEqualTo("USER");

        ResponseEntity<UserDto> promoted = rest.exchange("/api/admin/users/" + target.id() + "/role", HttpMethod.PUT,
                new HttpEntity<>(Map.of("role", "ADMIN"), auth(adminToken)), UserDto.class);
        assertThat(promoted.getBody().role()).isEqualTo("ADMIN");

        ResponseEntity<String> adminAccessWithOldToken = rest.exchange("/api/admin/levels", HttpMethod.GET,
                new HttpEntity<>(auth(userToken)), String.class);
        assertThat(adminAccessWithOldToken.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<UserDto> demoted = rest.exchange("/api/admin/users/" + target.id() + "/role", HttpMethod.PUT,
                new HttpEntity<>(Map.of("role", "USER"), auth(adminToken)), UserDto.class);
        assertThat(demoted.getBody().role()).isEqualTo("USER");

        ResponseEntity<Map> userAccessAgain = rest.exchange("/api/admin/levels", HttpMethod.GET,
                new HttpEntity<>(auth(userToken)), Map.class);
        assertThat(userAccessAgain.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Map> selfRoleChange = rest.exchange("/api/admin/users/" + List.of(users.getBody()).stream()
                        .filter(u -> u.username().equals(adminUsername)).findFirst().orElseThrow().id() + "/role",
                HttpMethod.PUT, new HttpEntity<>(Map.of("role", "USER"), auth(adminToken)), Map.class);
        assertThat(selfRoleChange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<Map> selfDelete = rest.exchange("/api/admin/users/" + List.of(users.getBody()).stream()
                        .filter(u -> u.username().equals(adminUsername)).findFirst().orElseThrow().id(),
                HttpMethod.DELETE, new HttpEntity<>(auth(adminToken)), Map.class);
        assertThat(selfDelete.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<Void> deleted = rest.exchange("/api/admin/users/" + target.id(), HttpMethod.DELETE,
                new HttpEntity<>(auth(adminToken)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void adminProgressListAndReset() {
        String adminToken = adminToken();
        String userToken = registerUser();

        ResponseEntity<Map> answered = rest.postForEntity("/api/questions/1/answer",
                new HttpEntity<>(Map.of("optionIndex", 0), auth(userToken)), Map.class);
        assertThat(answered.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map[]> progress = rest.exchange("/api/admin/progress", HttpMethod.GET,
                new HttpEntity<>(auth(adminToken)), Map[].class);
        assertThat(progress.getBody()).isNotEmpty();
        String userId = (String) progress.getBody()[0].get("userId");

        ResponseEntity<Void> reset = rest.exchange("/api/admin/progress/" + userId, HttpMethod.DELETE,
                new HttpEntity<>(auth(adminToken)), Void.class);
        assertThat(reset.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> summary = rest.exchange("/api/progress/me/summary", HttpMethod.GET,
                new HttpEntity<>(auth(userToken)), Map.class);
        assertThat(((Number) summary.getBody().get("totalAttempts")).longValue()).isZero();
    }
}
