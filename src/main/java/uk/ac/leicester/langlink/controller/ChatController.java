package uk.ac.leicester.langlink.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.leicester.langlink.model.Message;
import uk.ac.leicester.langlink.repository.MessageRepository;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/chat")
@CrossOrigin
public class ChatController {

    private final MessageRepository messageRepository;

    public ChatController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // GET /chat/conversation?user1=1&user2=2
    @GetMapping("/conversation")
    public List<Message> conversation(@RequestParam Long user1, @RequestParam Long user2) {
        return messageRepository.findConversation(user1, user2);
    }

    // POST /chat/send
    // body: { "fromUserId": 1, "toUserId": 2, "content": "Hello" }
    @PostMapping("/send")
    public Message send(@RequestBody SendMessageRequest req) {
        if (req.fromUserId == null || req.toUserId == null) {
            throw new IllegalArgumentException("fromUserId and toUserId are required.");
        }
        String text = (req.content == null) ? "" : req.content.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        Message m = new Message(req.fromUserId, req.toUserId, text, Instant.now());
        return messageRepository.save(m);
    }

    public static class SendMessageRequest {
        public Long fromUserId;
        public Long toUserId;
        public String content;
    }
}

