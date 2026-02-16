package uk.ac.leicester.langlink.repository;

import uk.ac.leicester.langlink.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        WHERE (m.fromUserId = :a AND m.toUserId = :b)
           OR (m.fromUserId = :b AND m.toUserId = :a)
        ORDER BY m.createdAt ASC
    """)
    List<Message> findConversation(@Param("a") Long a, @Param("b") Long b);
}
