package com.minhtetthar.post_now.controller;

import com.minhtetthar.post_now.dto.auth.AuthResponseDto;
import com.minhtetthar.post_now.dto.auth.LoginRequestDto;
import com.minhtetthar.post_now.dto.user.UserCreateDto;
import com.minhtetthar.post_now.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            AuthResponseDto response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponseDto.builder()
                            .message("Invalid credentials")
                            .build());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody UserCreateDto registerRequest) {
        try {
            AuthResponseDto response = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponseDto.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                boolean isValid = authService.validateToken(token);

                if (isValid) {
                    String username = authService.extractUsernameFromToken(token);
                    return ResponseEntity.ok(Map.of(
                            "valid", true,
                            "username", username));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Invalid token format"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Invalid or expired token"));
        }
    }
}