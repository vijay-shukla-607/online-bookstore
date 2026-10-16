package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.AppUser;
import com.example.bookstore.domain.Role;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.UserRepository;
import com.example.bookstore.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;


    @Transactional
    public ApiDtos.AuthResponse register(ApiDtos.RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        log.info("User registration requested");
        if (users.existsByEmail(email)) {
            log.warn("User registration rejected reason=account_already_exists");
            throw ApiException.conflict("An account with this email already exists");
        }
        AppUser user = users.save(new AppUser(email, encoder.encode(req.password()), Role.CUSTOMER));
        log.info("User registered userId={} role={}", user.getId(), user.getRole());
        return token(user);
    }

    public ApiDtos.AuthResponse login(ApiDtos.LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        log.info("User login attempt received");
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        } catch (AuthenticationException ex) {
            log.warn("Login failed reason=invalid_credentials");
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        AppUser user = users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        log.info("User login successful userId={}", user.getId());
        return token(user);
    }

    private ApiDtos.AuthResponse token(AppUser user) {
        return new ApiDtos.AuthResponse(jwt.generate(user.getEmail(), user.getRole().name()), "Bearer", jwt.expirationSeconds(), user.getEmail(), user.getRole().name());
    }
}
