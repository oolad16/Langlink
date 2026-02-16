package uk.ac.leicester.langlink.repository;

import uk.ac.leicester.langlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNativeLanguageAndTargetLanguage(String nativeLanguage, String targetLanguage);
}


