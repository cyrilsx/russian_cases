package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.service.AdminService;
import com.cyrilsx.russiancases.web.dto.AdminLevelDto;
import com.cyrilsx.russiancases.web.dto.AdminProgressDto;
import com.cyrilsx.russiancases.web.dto.AdminQuestionDto;
import com.cyrilsx.russiancases.web.dto.AdminRuleDto;
import com.cyrilsx.russiancases.web.dto.LevelUpsertRequest;
import com.cyrilsx.russiancases.web.dto.QuestionUpsertRequest;
import com.cyrilsx.russiancases.web.dto.RoleUpdateRequest;
import com.cyrilsx.russiancases.web.dto.RuleUpsertRequest;
import com.cyrilsx.russiancases.web.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/levels")
    public List<AdminLevelDto> listLevels() {
        return adminService.listLevels();
    }

    @PostMapping("/levels")
    public ResponseEntity<AdminLevelDto> createLevel(@Valid @RequestBody LevelUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createLevel(request));
    }

    @PutMapping("/levels/{id}")
    public AdminLevelDto updateLevel(@PathVariable Long id, @Valid @RequestBody LevelUpsertRequest request) {
        return adminService.updateLevel(id, request);
    }

    @DeleteMapping("/levels/{id}")
    public ResponseEntity<Void> deleteLevel(@PathVariable Long id) {
        adminService.deleteLevel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/questions")
    public List<AdminQuestionDto> listQuestions(@RequestParam(required = false) Long levelId) {
        return adminService.listQuestions(levelId);
    }

    @PostMapping("/questions")
    public ResponseEntity<AdminQuestionDto> createQuestion(@Valid @RequestBody QuestionUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createQuestion(request));
    }

    @PutMapping("/questions/{id}")
    public AdminQuestionDto updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionUpsertRequest request) {
        return adminService.updateQuestion(id, request);
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rules")
    public List<AdminRuleDto> listRules() {
        return adminService.listRules();
    }

    @PostMapping("/rules")
    public ResponseEntity<AdminRuleDto> createRule(@Valid @RequestBody RuleUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createRule(request));
    }

    @PutMapping("/rules/{id}")
    public AdminRuleDto updateRule(@PathVariable Long id, @Valid @RequestBody RuleUpsertRequest request) {
        return adminService.updateRule(id, request);
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        adminService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public List<UserDto> listUsers() {
        return adminService.listUsers();
    }

    @PutMapping("/users/{id}/role")
    public UserDto setRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request,
                           Authentication authentication) {
        return adminService.setRole(id, request.role(), authentication.getName());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        adminService.deleteUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/progress")
    public List<AdminProgressDto> listProgress() {
        return adminService.listProgress();
    }

    @DeleteMapping("/progress/{userId}")
    public ResponseEntity<Void> resetProgress(@PathVariable String userId) {
        adminService.resetProgress(userId);
        return ResponseEntity.noContent().build();
    }
}
