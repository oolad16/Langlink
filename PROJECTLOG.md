# Project Log

Please regularly update this file to record your project progress. You should be updating the project log at least once a fortnight.

---

## Week 1 — Project Setup
**October 2025**

**What I worked on:**
- Defined the concept of LangLink as a language exchange platform
- Decided to focus on connecting native speakers with learners
- Set up Spring Boot project structure
- Configured initial development environment

**Progress made:**
- Project successfully initialised
- Core idea clearly defined
- Basic backend structure created

**Problems / issues:**
- Needed to decide scope to avoid overcomplicating the project

---

## October – December 2025

**What was completed:**
- Outlined the project's idea: a platform for language exchange that links learners and native speakers
- Found the main issue: real-world human conversation practice amongst local users is lacking in the language learning apps currently in use (Duolingo, Babbel)
- Finished and uploaded the Project Plan to Blackboard on October 16, 2025
- Configured the development environment: Gradle, Java 17, and IntelliJ IDEA
- Started the Spring Boot 3.2.5 project with the necessary dependencies: H2 database, Spring Web, Spring Data JPA, and Spring Security
- Created the first commit and established the GitLab repository
- Designed the User entity with fields: ID, name, email, password (hashed using BCrypt), native language, target language, location, bio, and proficiency
- Implemented JPA repositories and designed the Message entity with fields: id, fromUserId, toUserId, content, and createdAt
- Implemented MessageRepository and UserRepository
- Configured H2 file-based database for development
- Confirmed Hibernate table construction at startup

**Important decisions made:**
- Gradle was selected over Maven due to its faster builds and clearer syntax
- Spring Boot 3.2.5 was selected because of its LTS stability and Java 17 compatibility
- Vanilla HTML/CSS/JS frontend: selected to keep the stack straightforward and concentrate on backend complexity
- Session-based authentication over JWT: easier to safely implement and more suitable for server-rendered applications

---

## November – January

**What was carried out:**
- BCrypt password encoder was used to implement Spring Security settings
- AuthController was constructed with the following endpoints: GET /auth/me, POST /auth/register, POST /auth/login, and POST /auth/logout
- LangLinkUserDetailsService was implemented for the integration of Spring Security
- CSRF was disabled to enable JavaScript fetch() compatibility with REST APIs
- 30-minute session timeout was set
- Manually tested session persistence, login, and logout

**Important decisions:**
- Disabling CSRF was necessary since the JavaScript fetch() API used by the frontend does not automatically handle CSRF tokens in the same way as conventional form submissions
- The GET /auth/me endpoint was added so that the frontend could verify the session status when the page loaded and reroute to login if not authenticated
- Instead of sending a 401 JSON answer, Spring Security's default behaviour sent unauthenticated requests to a login form. Resolved by customising the security filter chain to return 401 for API endpoints

---

## February – March 2026

**What was carried out:**
- PUT /users/me (to update one's own profile) and GET /users/me (to see one's own profile) were built into the UserController
- The matching algorithm was created at GET /users/me/matches, which locates users whose nativeLanguage and targetLanguage match the current user's languages
- The matches endpoint now has an optional location filter (case-insensitive partial match)
- Constructed ChatController with GET /chat/conversation and POST /chat/send endpoints
- Additional access control: message senders are always specified server-side from the authenticated session (cannot be spoofed), and users can only modify their own profiles
- Location matching as a text filter: GPS radius matching was considered but postponed. Text-based city/area matching suffices for the dissertation
- Bidirectional conversation loading: the /chat/conversation endpoint retrieves messages in both directions (A→B and B→A), arranged by timestamp
- Created and constructed login.html with a gradient background, animated card, and session-aware redirect
- Created and constructed register.html with sectioned layout and language selection pills
- Created the first index.html dashboard with a chat panel, match cards, and profile editing
- Used a uniform colour scheme of teal, blue, and green on every page
- Used Google Fonts (Nunito + Lora) for typography
- All pages now have client-side session checks, which reroute users to login if not authorised

---

## March – April 2026

### Testing

**What was done:**
- Wrote LangLinkIntegrationTest.java with 10 integration tests covering all core functionality
- Configured separate test application.properties using H2 in-memory database
- Tests cover: registration, login, logout, session check, profile update, matching, messaging, conversation loading, access control, duplicate email rejection
- All 10 tests passing confirmed

**Test results:**

| Test | Result |
|---|---|
| User registration | PASS |
| User login | PASS |
| User logout | PASS |
| Get current user | PASS |
| Update profile | PASS |
| Get matches | PASS |
| Send message | PASS |
| Load conversation | PASS |
| Access control | PASS |
| Duplicate email rejection | PASS |

---

### Database Migration to Supabase

**What was done:**
- Created a Supabase project in the AWS London (eu-west-2) region
- Upgraded PostgreSQL JDBC driver from 42.6.2 to 42.7.3 in build.gradle
- Configured application.properties to connect to Supabase via the Session Pooler (aws-1-eu-west-2.pooler.supabase.com, port 5432)
- Hibernate automatically created the users and messages tables in Supabase on first connection
- Verified that user registration, login, and messaging all persist correctly in the cloud database

**Challenges:**
- Home broadband ISP blocks outbound connections on PostgreSQL ports (5432, 6543). Resolved by using a mobile phone hotspot for development
- Initial connection attempts failed due to incorrect pooler region (aws-0 vs aws-1) and wrong port. Resolved by checking the Supabase dashboard for the exact Session Pooler host and port

**Key decisions:**
- Supabase over local PostgreSQL: Supabase provides a cloud-hosted database accessible from any network, making the application demonstrable from any machine. H2 kept for tests to maintain test isolation

---

### UI Restructure

**What was done:**
- index.html completely reorganised from a single scrolling page to a four-section application with a sticky sidebar navigation: Dashboard, Find Partners, Messages, and My Profile
- Personalised welcome message added ("Welcome back, [First Name]!")
- User avatar with initials added to the header
- Stats cards added to the dashboard showing: Matches Found, Conversations Started, and Messages Sent
- Conversation inbox added to the Messages section listing all prior interactions with partners
- Unread message notifications added: orange border, bold inbox text, red badge on the sidebar, and a "New" pill on inbox rows
- Proficiency level filter added to the Find Partners page
- Chat auto-refresh implemented every ten seconds
- Report user placeholder button added to every match card
- Profile completion progress indicator added showing which fields are still missing
- Fully responsive design developed with a hamburger menu for mobile

**Key decisions:**
- Sidebar navigation chosen over tab bar navigation as it gives labels more room and scales better to future sections
- Proficiency filtering applied on the backend to keep the endpoint clean and consistent with the location filter
- Report button used as a placeholder: demonstrates awareness of user safety requirements without needing moderation infrastructure. Recorded as future work

---

### Documentation

**What was done:**
- Written README.md documenting the project, how to run it, features, limitations, and future work
- Written PROJECTLOG.md documenting all phases of development

---

## Summary of All Features

- User registration
- BCrypt password hashing
- Login / logout
- Session management
- Language matching algorithm
- Location text filter
- Proficiency filter
- View and edit own profile
- Send and receive messages
- Conversation inbox
- Chat auto-refresh (10s)
- Stats dashboard
- Welcome greeting
- Avatar with initials
- Profile completion indicator
- Report user placeholder
- Unread message notifications
- Responsive mobile design
- Cloud PostgreSQL (Supabase)
- Integration test suite (10 tests)

---

## Planned Future Work (Not Implemented)

| Feature | Reason Not Implemented |
|---|---|
| GPS radius matching | Requires Geolocation API and backend distance calculation — deferred to future work |
| WebSocket real-time chat | Significant additional complexity — polling sufficient for current scope |
| Email verification | Infrastructure requirement (SMTP server) outside project scope |
| Password reset | Requires email infrastructure |
| Mobile application | Separate project beyond dissertation scope |
| User blocking | Deferred — report placeholder documents intent |



