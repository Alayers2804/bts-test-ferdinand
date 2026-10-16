
package com.bts.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bts.dto.LoginRequest;
import com.bts.dto.LoginResponse;
import com.bts.dto.RegisterRequest;
import com.bts.entity.User;
import com.bts.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        String name = request.getName();

        if (name == null || name.isBlank()) {
            name = "Jane Doe";
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(name);

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Invalid username or password"
                )
            );

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException(
                "Invalid username or password"
            );
        }

        String accessToken =
            tokenService.generateAccessToken(user.getUsername());

        String refreshToken =
            tokenService.generateRefreshToken(user.getUsername());

        return new LoginResponse(accessToken, refreshToken);
    }
}
