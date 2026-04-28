package com.reviewsystem.controller;

import com.reviewsystem.model.User;
import com.reviewsystem.repository.UserRepository;
import com.reviewsystem.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    // POST /api/auth/login → returns JWT token
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Optional<User> userOpt = userRepo.findByEmail(req.getEmail());
        if (userOpt.isEmpty())
            return ResponseEntity.status(401).body(Map.of("error", "No account found with this email"));

        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            return ResponseEntity.status(401).body(Map.of("error", "Incorrect password"));

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
            "id",      user.getId(),
            "name",    user.getName(),
            "email",   user.getEmail(),
            "role",    user.getRole().name(),
            "token",   token,
            "message", "Login successful"
        ));
    }

    // POST /api/auth/register → creates account + returns JWT token
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));

        User user = userRepo.save(User.builder()
            .name(req.getName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(req.getRole() != null ? req.getRole() : User.Role.STUDENT)
            .build());

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
            "id",      user.getId(),
            "name",    user.getName(),
            "email",   user.getEmail(),
            "role",    user.getRole().name(),
            "token",   token,
            "message", "Registration successful"
        ));
    }

    @Data public static class LoginRequest {
        @NotBlank @Email private String email;
        @NotBlank private String password;
    }

    @Data public static class RegisterRequest {
        @NotBlank private String name;
        @NotBlank @Email private String email;
        @NotBlank @Size(min = 6) private String password;
        private User.Role role;
    }
}
