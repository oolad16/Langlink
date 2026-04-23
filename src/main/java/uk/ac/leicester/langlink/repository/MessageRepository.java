package uk.ac.leicester.langlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.ac.leicester.langlink.model.Message;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
            "(m.fromUserId = :user1 AND m.toUserId = :user2) OR " +
            "(m.fromUserId = :user2 AND m.toUserId = :user1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("user1") Long user1, @Param("user2") Long user2);
}
