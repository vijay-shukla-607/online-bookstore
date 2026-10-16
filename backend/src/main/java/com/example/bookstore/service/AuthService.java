package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.AppUser;
import com.example.bookstore.domain.Role;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.UserRepository;
import com.example.bookstore.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    @Transactional
    public ApiDtos.AuthResponse register(ApiDtos.RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw ApiException.conflict("An account with this email already exists");
        AppUser user = users.save(new AppUser(email, encoder.encode(req.password()), Role.CUSTOMER));
        return token(user);
    }

    public ApiDtos.AuthResponse login(ApiDtos.LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        } catch (AuthenticationException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        AppUser user = users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return token(user);
    }

    private ApiDtos.AuthResponse token(AppUser user) {
        return new ApiDtos.AuthResponse(jwt.generate(user.getEmail(), user.getRole().name()), "Bearer", jwt.expirationSeconds(), user.getEmail(), user.getRole().name());
    }
}
