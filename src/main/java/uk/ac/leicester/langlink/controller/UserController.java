package uk.ac.leicester.langlink.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.leicester.langlink.model.User;
import uk.ac.leicester.langlink.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET http://localhost:8080/users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // POST http://localhost:8080/users
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // DELETE http://localhost:8080/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // PUT http://localhost:8080/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updated) {
        return userRepository.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setEmail(updated.getEmail());
                    existing.setNativeLanguage(updated.getNativeLanguage());
                    existing.setTargetLanguage(updated.getTargetLanguage());
                    User saved = userRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET http://localhost:8080/users/{id}/matches
    @GetMapping("/{id}/matches")
    public ResponseEntity<List<User>> findMatches(@PathVariable Long id) {
        User me = userRepository.findById(id).orElse(null);
        if (me == null) return ResponseEntity.notFound().build();

        List<User> all = userRepository.findAll();
        List<User> matches = new ArrayList<>();

        for (User u : all) {
            if (u.getId() == null || u.getId().equals(me.getId())) continue;

            boolean languageSwapMatch =
                    me.getNativeLanguage() != null && me.getTargetLanguage() != null &&
                            u.getNativeLanguage() != null && u.getTargetLanguage() != null &&
                            me.getNativeLanguage().equalsIgnoreCase(u.getTargetLanguage()) &&
                            me.getTargetLanguage().equalsIgnoreCase(u.getNativeLanguage());

            if (languageSwapMatch) {
                matches.add(u);
            }
        }

        return ResponseEntity.ok(matches);
    }
}

