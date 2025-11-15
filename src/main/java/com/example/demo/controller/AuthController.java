package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.JwtService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

record AuthRegisterRequest(String username, String email, String password) {}
record AuthLoginRequest(String username, String password) {}
record AuthResponse(Long userId, String username, String email, String role, String token) {}

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRegisterRequest req) {
        try {
            if (req.username() == null || req.username().isBlank() ||
                req.email() == null || req.email().isBlank() ||
                req.password() == null || req.password().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu username, email hoặc password!"));
            }

            User user = userService.register(req.username(), req.email(), req.password());

            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();

            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(
                    user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(), token
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthLoginRequest req) {
        if (req.username() == null || req.username().isBlank() || req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu username hoặc password!"));
        }

        User user = userService.findByUsername(req.username());
        if (!encoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Sai mật khẩu hoặc tài khoản!"));
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(), token
        ));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody AuthRegisterRequest req) {
        try {
            if (req.username() == null || req.email() == null || req.password() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu thông tin đăng ký!"));
            }

            User user = userService.registerAdmin(req.username(), req.email(), req.password());

            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();

            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(
                    user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(), token
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
