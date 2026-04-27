Langlink - Language exchange matching platform
CO3202 - Dissertation project: University of Leicester
Pupil: Omotola Oladejo
Year: 2025 - 2026

What is Langlink?
A web-based language exchange matching platform called LangLink links local native speakers with those who wish to practice speaking foreign languages. The fundamental idea is two-way: if User A is learning Spanish and speaks English as their first language, they are paired with User B, who is learning English and speaks Spanish as their first language. Real conversation practice with a real partner is beneficial to both users.
With a cloud-hosted PostgreSQL database and a responsive vanilla HTML/CSS/JavaScript frontend, the application is constructed as a full-stack Java web application using Spring Boot.

Technologies that were used
Layer
Technology
Backend Framework
Spring Boot 3.2.5 (Java 17)
Build Tool
Gradle
Database
Supabase PostgreSQL (cloud-hosted, London region)
ORM
Spring Data JPA / Hibernate
Security
Spring Security 6 — BCrypt, session-based auth
Frontend
Vanilla HTML, CSS, JavaScript
Fonts
Google Fonts — Nunito + Lora
Testing
JUnit 5 + Spring Boot Test
Version Control
GitLab

How to run
Requirements
Java 17 (JDK)
IntelliJ IDEA  or any IDE with Gradle support
Internet connection that allows outbound connections on port 5432 (mobile hotspot recommended as some home broadband ISPs block this port)

Steps
Clone the repository from GitLab
Open the project in IntelliJ IDEA
Connect to a network that allows port 5432 (mobile hotspot works reliably)
Run LangLinkApplication.java using the green play button in IntelliJ
Wait for the console to show: Started LangLinkApplication in X seconds
Open a browser and navigate to: http://localhost:8080

Running tests
./gradlew test

Tests use an H2 in-memory database and do not require an internet connection. All 10 integration tests should pass.
Features
Authentication
Entering the user's name, email address, password, native and target languages, location, bio, and level of proficiency
Spring Security session management for safe login and logout
BCrypt-hashed passwords

Dashboard
First-name-based personalized welcome greeting
Stats cards displaying messages sent, discussions initiated, and matches found
summary of the profile with a completion progress bar

Buttons for quick action navigation

Finding language partners
An algorithm for matching individuals with complementary language pairs (your target language is their native language and vice versa)
Location-based filtering by region or city
Filter for proficiency level

Each match card has a report user button that displays the person's name, languages, area, proficiency, and bio.

Messages
All current discussions are listed in the conversation inbox.
Real-time conversation that automatically refreshes every ten seconds
Timestamps for messages

Notifications of unread messages with badge counts

My profile

Examine and modify every profile field.
An indicator of profile completion that displays fields that are missing
Changes appear instantly on the dashboard.

UI

Desktop sidebar navigation and a fully responsive design
Mobile menu for hamburgers
Teal, blue, and green colors are used consistently throughout.

Initials of the user in the header of the avatar

PROJECT STRUCTURE

src/
main/
java/uk/ac/leicester/langlink/
config/          — Spring Security configuration
controller/      — REST API endpoints (Auth, User, Chat, Home)
model/           — JPA entities (User, Message)
repository/      — Spring Data repositories
resources/
static/          — Frontend HTML files (login, register, index)
application.properties — Database and server configuration
test/
java/              — Integration tests (10 tests)
resources/
application.properties — H2 in-memory config for tests
build.gradle
README.md
PROJECTLOG.md

Known limitations

Network requirement: A network that does not block outgoing port 5432 is necessary for the Supabase connection. This port is blocked by several home internet ISPs; using a mobile hotspot fixes the problem.
Currently, text-based matching (city/area name) is used for location matching. Future improvements include radius-based GPS distance matching.
No real-time chat: Instead of using WebSocket connections, messages are refreshed via polling every ten seconds.

No email verification: Users are able to sign up without having to confirm their email address.
No password reset: The ability to recover passwords has not yet been included.


Future work

GPS radius-based matching with the Haversine distance algorithm and the browser Geolocation API
WebSocket real-time chat for sending messages instantly
Verification by email during registration

Email-based password reset
Mobile app (Android/iOS)
Advanced filtering on a premium subscription tier
Integration of university partnerships for validated student accounts
Resources for in-app language learning and scheduling sessions


Database

The application uses Supabase PostgreSQL hosted in the AWS London (eu-west-2) region.  The tables are created automatically by Hibernate on first run (ddl-auto=update). Also the tests use an H2 in-memory database that is completely isolated from production data.


TESTING
The integration test suite covers:
User registration
User login
User logout
Get current user (session check)
Update user profile
Get language matches
Send a message
Load a conversation
Access control (cannot edit other users)
Duplicate email rejection
All tests pass and use H2 in-memory database for complete isolation from production.
