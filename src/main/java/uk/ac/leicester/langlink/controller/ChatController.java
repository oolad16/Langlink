package uk.ac.leicester.langlink.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.ac.leicester.langlink.model.Message;
import uk.ac.leicester.langlink.model.User;
import uk.ac.leicester.langlink.repository.MessageRepository;
import uk.ac.leicester.langlink.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // ── GET /chat/conversations ───────────────────────────────────
    // Returns all users the logged-in user has exchanged messages with
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(Authentication auth) {
        Optional<User> meOpt = resolveCurrentUser(auth);
        if (meOpt.isEmpty()) return ResponseEntity.status(401).build();

        Long myId = meOpt.get().getId();

        List<Long> partnerIds = messageRepository.findConversationPartnerIds(myId);
        List<User> partners = userRepository.findAllById(partnerIds);
        return ResponseEntity.ok(partners);
    }

    // ── GET /chat/conversation?withUserId=5 ───────────────────────
    // Only accessible by the two participants — logged-in user is always one of them
    @GetMapping("/conversation")
    public ResponseEntity<?> getConversation(@RequestParam Long withUserId,
                                             Authentication auth) {
        Optional<User> meOpt = resolveCurrentUser(auth);
        if (meOpt.isEmpty()) return ResponseEntity.status(401).build();

        Long myId = meOpt.get().getId();

        // Verify the other user exists
        if (!userRepository.existsById(withUserId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found."));
        }

        List<Message> messages = messageRepository.findConversation(myId, withUserId);
        return ResponseEntity.ok(messages);
    }

    // ── POST /chat/send ───────────────────────────────────────────
    // fromUserId is always the logged-in user — cannot spoof sender
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequest req,
                                         Authentication auth) {
        Optional<User> meOpt = resolveCurrentUser(auth);
        if (meOpt.isEmpty()) return ResponseEntity.status(401).build();

        Long myId = meOpt.get().getId();

        if (req.toUserId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "toUserId is required."));
        }
        if (req.toUserId.equals(myId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "You cannot message yourself."));
        }
        if (!userRepository.existsById(req.toUserId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Recipient not found."));
        }

        String text = req.content != null ? req.content.trim() : "";
        if (text.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty."));
        }
        if (text.length() > 2000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message too long (max 2000 characters)."));
        }

        Message message = new Message(myId, req.toUserId, text, Instant.now());
        return ResponseEntity.ok(messageRepository.save(message));
    }

    // ── Helpers ───────────────────────────────────────────────────
    private Optional<User> resolveCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        return userRepository.findByEmail(auth.getName());
    }

    public static class SendMessageRequest {
        public Long toUserId;
        public String content;
    }
}
