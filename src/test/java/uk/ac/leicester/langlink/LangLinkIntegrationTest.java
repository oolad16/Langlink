package uk.ac.leicester.langlink;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LangLinkIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    // ── Helper: register a user and return their session ─────────
    private MockHttpSession registerAndLogin(String name, String email,
                                             String password,
                                             String native_, String target,
                                             String location) throws Exception {
        // Register
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", name, "email", email, "password", password,
                                "nativeLanguage", native_, "targetLanguage", target,
                                "location", location
                        ))))
                .andExpect(status().isOk());

        // Login and capture session
        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession();
    }

    // ── Test 1: Register and login ────────────────────────────────
    @Test
    void testRegisterAndLogin() throws Exception {
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name","Alice","email","alice@test.com","password","secret123",
                                "nativeLanguage","English","targetLanguage","Spanish","location","Leicester"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email","alice@test.com","password","secret123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist()); // password must not be returned
    }

    // ── Test 2: Wrong password is rejected ───────────────────────
    @Test
    void testWrongPasswordRejected() throws Exception {
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name","Bob","email","bob@test.com","password","correct",
                                "nativeLanguage","French","targetLanguage","English","location","London"
                        ))))
                .andExpect(status().isOk());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email","bob@test.com","password","wrongpassword"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    // ── Test 3: /auth/me requires login ──────────────────────────
    @Test
    void testMeRequiresAuth() throws Exception {
        mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── Test 4: Profile edit ──────────────────────────────────────
    @Test
    void testProfileEdit() throws Exception {
        MockHttpSession session = registerAndLogin(
                "Carol","carol@test.com","pass123","German","Italian","Birmingham");

        mvc.perform(put("/users/me").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "bio","I love languages","location","Manchester"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("I love languages"))
                .andExpect(jsonPath("$.location").value("Manchester"));
    }

    // ── Test 5: Language matching ─────────────────────────────────
    @Test
    void testLanguageMatching() throws Exception {
        MockHttpSession aliceSession = registerAndLogin(
                "Alice2","alice2@test.com","pass123","English","Spanish","Leicester");
        MockHttpSession bobSession = registerAndLogin(
                "Bob2","bob2@test.com","pass123","Spanish","English","Leicester");

        // Alice should find Bob as a match
        mvc.perform(get("/users/me/matches").session(aliceSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bob2"));

        // Bob should find Alice
        mvc.perform(get("/users/me/matches").session(bobSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice2"));
    }

    // ── Test 6: Location filter on matching ───────────────────────
    @Test
    void testLocationFilter() throws Exception {
        MockHttpSession aliceSession = registerAndLogin(
                "Alice3","alice3@test.com","pass123","English","French","London");
        registerAndLogin("Bob3","bob3@test.com","pass123","French","English","Manchester");
        registerAndLogin("Eve3","eve3@test.com","pass123","French","English","London");

        // Filter to London — should return Eve, not Bob
        mvc.perform(get("/users/me/matches?location=London").session(aliceSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Eve3"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── Test 7: Chat send and retrieve ───────────────────────────
    @Test
    void testChatSendAndRetrieve() throws Exception {
        MockHttpSession aliceSession = registerAndLogin(
                "Alice4","alice4@test.com","pass123","English","Japanese","Leeds");
        MockHttpSession bobSession = registerAndLogin(
                "Bob4","bob4@test.com","pass123","Japanese","English","Leeds");

        // Get Bob's ID
        MvcResult mr = mvc.perform(get("/auth/me").session(bobSession))
                .andExpect(status().isOk()).andReturn();
        Map bob = mapper.readValue(mr.getResponse().getContentAsString(), Map.class);
        Integer bobId = (Integer) bob.get("id");

        // Alice sends message to Bob
        mvc.perform(post("/chat/send").session(aliceSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "toUserId", bobId, "content", "Hello Bob!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello Bob!"));

        // Alice retrieves conversation
        mvc.perform(get("/chat/conversation?withUserId=" + bobId).session(aliceSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello Bob!"));
    }

    // ── Test 8: Duplicate email registration rejected ─────────────
    @Test
    void testDuplicateEmailRejected() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "name","X","email","dup@test.com","password","abc123",
                "nativeLanguage","English","targetLanguage","French","location",""
        ));
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    // ── Test 9: User cannot spoof sender in chat ──────────────────
    @Test
    void testChatSenderCannotBeSpoofed() throws Exception {
        MockHttpSession aliceSession = registerAndLogin(
                "Alice5","alice5@test.com","pass123","English","Korean","Bristol");
        MockHttpSession bobSession = registerAndLogin(
                "Bob5","bob5@test.com","pass123","Korean","English","Bristol");

        MvcResult mr = mvc.perform(get("/auth/me").session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map alice = mapper.readValue(mr.getResponse().getContentAsString(), Map.class);
        Integer aliceId = (Integer) alice.get("id");

        MvcResult mr2 = mvc.perform(get("/auth/me").session(bobSession))
                .andExpect(status().isOk()).andReturn();
        Map bob = mapper.readValue(mr2.getResponse().getContentAsString(), Map.class);
        Integer bobId = (Integer) bob.get("id");

        // Bob sends — fromUserId in DB should be Bob's ID, not Alice's
        MvcResult sendResult = mvc.perform(post("/chat/send").session(bobSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "toUserId", aliceId, "content", "Hi Alice"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        Map saved = mapper.readValue(sendResult.getResponse().getContentAsString(), Map.class);
        // fromUserId must be Bob's real ID
        assert saved.get("fromUserId").equals(bobId) : "Sender ID was spoofed!";
    }

    // ── Test 10: Logout invalidates session ───────────────────────
    @Test
    void testLogoutInvalidatesSession() throws Exception {
        MockHttpSession session = registerAndLogin(
                "LogoutTest","logout@test.com","pass123","English","Arabic","Leeds");

        // Verify logged in
        mvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk());

        // Logout
        mvc.perform(post("/auth/logout").session(session))
                .andExpect(status().isOk());

        // Session should now be invalid
        mvc.perform(get("/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }
}
