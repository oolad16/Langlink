package uk.ac.leicester.langlink.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import uk.ac.leicester.langlink.model.User;
import uk.ac.leicester.langlink.repository.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    // ── POST /auth/register ──────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        // Basic validation
        if (req.name == null || req.name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required."));
        }
        if (req.email == null || !req.email.contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid email is required."));
        }
        if (req.password == null || req.password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters."));
        }
        if (req.nativeLanguage == null || req.nativeLanguage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Native language is required."));
        }
        if (req.targetLanguage == null || req.targetLanguage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Target language is required."));
        }
        if (userRepository.existsByEmail(req.email.trim().toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error", "An account with this email already exists."));
        }

        User user = new User();
        user.setName(req.name.trim());
        user.setEmail(req.email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.password));
        user.setNativeLanguage(req.nativeLanguage.trim());
        user.setTargetLanguage(req.targetLanguage.trim());
        user.setLocation(req.location != null ? req.location.trim() : null);
        user.setBio(req.bio != null ? req.bio.trim() : null);
        user.setProficiency(req.proficiency != null ? req.proficiency.trim() : null);

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Account created. Please log in."));
    }

    // ── POST /auth/login ─────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req,
                                   HttpServletRequest request) {
        if (req.email == null || req.password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required."));
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.email.trim().toLowerCase(), req.password)
            );

            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);

            // Store in HTTP session so subsequent requests are authenticated
            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

            // Return the user's profile data
            User user = userRepository.findByEmail(req.email.trim().toLowerCase()).orElseThrow();
            return ResponseEntity.ok(safeUser(user));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Incorrect email or password."));
        }
    }

    // ── POST /auth/logout ────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out."));
    }

    // ── GET /auth/me ─────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in."));
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(safeUser(u)))
                .orElse(ResponseEntity.status(404).build());
    }

    // Return user data without exposing password
    private Map<String, Object> safeUser(User u) {
        return Map.of(
                "id", u.getId(),
                "name", u.getName() != null ? u.getName() : "",
                "email", u.getEmail() != null ? u.getEmail() : "",
                "nativeLanguage", u.getNativeLanguage() != null ? u.getNativeLanguage() : "",
                "targetLanguage", u.getTargetLanguage() != null ? u.getTargetLanguage() : "",
                "bio", u.getBio() != null ? u.getBio() : "",
                "location", u.getLocation() != null ? u.getLocation() : "",
                "proficiency", u.getProficiency() != null ? u.getProficiency() : ""
        );
    }

    // ── Request DTOs ─────────────────────────────────────────────
    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
        public String nativeLanguage;
        public String targetLanguage;
        public String location;
        public String bio;
        public String proficiency;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }
}