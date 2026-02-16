package uk.ac.leicester.langlink.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fromUserId;
    private Long toUserId;

    @Column(length = 2000)
    private String content;

    private Instant createdAt;

    public Message() { }

    public Message(Long fromUserId, Long toUserId, String content, Instant createdAt) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
