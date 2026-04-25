package uk.ac.leicester.langlink.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.ac.leicester.langlink.model.User;
import uk.ac.leicester.langlink.repository.UserRepository;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── GET /users/me ─────────────────────────────────────────────
    // Returns full profile of the currently logged-in user
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication auth) {
        return resolveCurrentUser(auth)
                .map(u -> ResponseEntity.ok(toPublicProfile(u)))
                .orElse(ResponseEntity.status(401).build());
    }

    // ── PUT /users/me ─────────────────────────────────────────────
    // Edit own profile only
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody UpdateProfileRequest req,
                                             Authentication auth) {
        return resolveCurrentUser(auth).map(user -> {
            if (req.name != null && !req.name.trim().isEmpty()) user.setName(req.name.trim());
            if (req.nativeLanguage != null) user.setNativeLanguage(req.nativeLanguage.trim());
            if (req.targetLanguage != null) user.setTargetLanguage(req.targetLanguage.trim());
            if (req.bio != null) user.setBio(req.bio.trim());
            if (req.location != null) user.setLocation(req.location.trim());
            if (req.proficiency != null) user.setProficiency(req.proficiency.trim());
            userRepository.save(user);
            return ResponseEntity.ok(toPublicProfile(user));
        }).orElse(ResponseEntity.status(401).build());
    }

    // ── GET /users/{id}/profile ──────────────────────────────────
    // View another user's public profile (no email, no private fields)
    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getPublicProfile(@PathVariable Long id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toPublicProfile(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET /users/me/matches ─────────────────────────────────────
    // Find matches: language swap + optional location + optional proficiency filter
    @GetMapping("/me/matches")
    public ResponseEntity<?> findMatches(@RequestParam(required = false) String location,
                                         @RequestParam(required = false) String proficiency,
                                         Authentication auth) {
        Optional<User> meOpt = resolveCurrentUser(auth);
        if (meOpt.isEmpty()) return ResponseEntity.status(401).build();

        User me = meOpt.get();
        if (me.getNativeLanguage() == null || me.getTargetLanguage() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Set your native and target language first."));
        }

        List<User> all = userRepository.findAll();
        List<Map<String, Object>> matches = new ArrayList<>();

        for (User u : all) {
            if (u.getId().equals(me.getId())) continue;
            if (u.getNativeLanguage() == null || u.getTargetLanguage() == null) continue;

            boolean languageMatch =
                    me.getNativeLanguage().equalsIgnoreCase(u.getTargetLanguage()) &&
                            me.getTargetLanguage().equalsIgnoreCase(u.getNativeLanguage());

            if (!languageMatch) continue;

            // Optional location filter: loose contains match (city/borough/area)
            if (location != null && !location.isBlank()) {
                String filterArea = location.trim().toLowerCase();
                String userArea = u.getLocation() != null ? u.getLocation().toLowerCase() : "";
                if (!userArea.contains(filterArea) && !filterArea.contains(userArea)) {
                    continue;
                }
            }

            // Optional proficiency filter: exact match (case-insensitive)
            if (proficiency != null && !proficiency.isBlank()) {
                String filterProf = proficiency.trim().toLowerCase();
                String userProf = u.getProficiency() != null ? u.getProficiency().toLowerCase() : "";
                if (!userProf.equals(filterProf)) {
                    continue;
                }
            }

            matches.add(toPublicProfile(u));
        }

        return ResponseEntity.ok(matches);
    }

    // ── DELETE /users/me ──────────────────────────────────────────
    // Allow user to delete their own account
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication auth) {
        return resolveCurrentUser(auth).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.status(401).build());
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Optional<User> resolveCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        return userRepository.findByEmail(auth.getName());
    }

    // Returns only safe public data — no password, no raw email for other users
    private Map<String, Object> toPublicProfile(User u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId());
        map.put("name", u.getName() != null ? u.getName() : "");
        map.put("nativeLanguage", u.getNativeLanguage() != null ? u.getNativeLanguage() : "");
        map.put("targetLanguage", u.getTargetLanguage() != null ? u.getTargetLanguage() : "");
        map.put("bio", u.getBio() != null ? u.getBio() : "");
        map.put("location", u.getLocation() != null ? u.getLocation() : "");
        map.put("proficiency", u.getProficiency() != null ? u.getProficiency() : "");
        // Email only included as it's needed on the dashboard for "my own" profile;
        // the UI should not display it in match cards
        map.put("email", u.getEmail() != null ? u.getEmail() : "");
        return map;
    }

    // ── Request DTOs ──────────────────────────────────────────────
    public static class UpdateProfileRequest {
        public String name;
        public String nativeLanguage;
        public String targetLanguage;
        public String bio;
        public String location;
        public String proficiency;
    }
}