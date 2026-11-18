package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.model.UserEntity;
import com.cyrilsx.russiancases.repository.UserRepository;
import com.cyrilsx.russiancases.security.JwtService;
import com.cyrilsx.russiancases.web.dto.AuthRequest;
import com.cyrilsx.russiancases.web.dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(jwtService.generate(user.getUsername()), user.getUsername()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }
        return new AuthResponse(jwtService.generate(user.getUsername()), user.getUsername());
    }

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        return Map.of("username", authentication.getName());
    }
}
