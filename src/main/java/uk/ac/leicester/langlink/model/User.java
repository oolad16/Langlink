package uk.ac.leicester.langlink.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    // Stored as BCrypt hash — never returned in API responses
    @JsonIgnore
    private String password;

    private String nativeLanguage;
    private String targetLanguage;

    // Phase 4 — Profile fields
    @Column(length = 500)
    private String bio;

    // Simplified location: city/area/borough (Phase 5)
    private String location;

    // e.g. "Beginner", "Intermediate", "Advanced"
    private String proficiency;

    public User() {}

    public User(String name, String email, String nativeLanguage, String targetLanguage) {
        this.name = name;
        this.email = email;
        this.nativeLanguage = nativeLanguage;
        this.targetLanguage = targetLanguage;
    }

    // Getters & setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNativeLanguage() { return nativeLanguage; }
    public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getProficiency() { return proficiency; }
    public void setProficiency(String proficiency) { this.proficiency = proficiency; }
}
