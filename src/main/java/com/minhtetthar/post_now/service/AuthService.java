package com.minhtetthar.post_now.service;

import com.minhtetthar.post_now.config.JwtService;
import com.minhtetthar.post_now.dto.auth.AuthResponseDto;
import com.minhtetthar.post_now.dto.auth.LoginRequestDto;
import com.minhtetthar.post_now.dto.user.UserCreateDto;
import com.minhtetthar.post_now.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        User userDetails = (User) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);

        return AuthResponseDto.builder()
                .token(jwt)
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRole().name())
                .message("Login successful")
                .build();
    }

    @Transactional
    public AuthResponseDto register(UserCreateDto registerRequest) {
        // Create the user
        userService.createUser(registerRequest);

        // Log them in automatically
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername(registerRequest.getUsername());
        loginRequest.setPassword(registerRequest.getPassword());

        AuthResponseDto response = login(loginRequest);
        response.setMessage("Registration successful");

        return response;
    }

    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userService.loadUserByUsername(username);
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsernameFromToken(String token) {
        return jwtService.extractUsername(token);
    }
}