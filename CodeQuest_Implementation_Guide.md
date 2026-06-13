

⚔  CODE QUEST
Gamified Learning Platform for Tech Students

PRODUCT REQUIREMENTS & IMPLEMENTATION GUIDE
Version 1.0  |  June 2026




CONFIDENTIAL — FOR INTERNAL USE ONLY

1. Executive Summary
Code Quest is an immersive, gamified educational platform designed specifically for Computer Science and Engineering students. It bridges the gap between passive learning and active skill-building by transforming coding practice — similar to LeetCode — into a rich role-playing game (RPG) experience.
Students create a developer avatar that evolves in power, appearance, and abilities as they solve real programming challenges. Each programming language (Python, JavaScript, Java, C++, etc.) is treated as a unique skill tree. Performance across all languages is published on a live global leaderboard, fostering healthy competition and community.

1.1  Core Value Proposition
Pillar
Description
Progression
Characters evolve visually and gain new powers as they complete coding tasks
Competition
Live leaderboards per language, overall rank, and institution-level comparisons
Curriculum
Structured challenges aligned to industry skill-levels from Apprentice to Legend
Community
Guild system, team challenges, code reviews, and mentor matching

2. Game Design & Character System
2.1  Character Architecture
Every user controls a Developer Avatar with three interconnected layers:
Appearance Layer — Visual assets (sprite, badge, gear) that update at milestone levels.
Stats Layer — Numeric attributes that affect XP multipliers and challenge unlocks.
Skill Tree Layer — One tree per programming language; each node unlocks a new challenge category.

2.2  Character Evolution Tiers
Characters evolve through six tiers, each with a distinct visual theme and new platform capabilities:
Tier
Name
XP Range
Visual Theme
Unlocks
1
Apprentice
0 – 999
Grey robe, wooden staff
Basic challenges, public profile
2
Coder
1,000 – 4,999
Blue hoodie, laptop prop
Medium challenges, streak bonuses
3
Engineer
5,000 – 14,999
Green armor, circuit patterns
Hard challenges, guild creation
4
Architect
15,000 – 39,999
Gold armor, floating data orbs
System design challenges, mentoring
5
Wizard
40,000 – 99,999
Purple robes, spell effects
Expert challenges, leaderboard badges
6
Legend
100,000+
Dark matter aura, unique FX
All content, Legend-only tournaments

2.3  Character Stats
Each character has five core stats that grow with activity:
Stat
Grows By
Effect
Intelligence
Solving algorithm tasks
Increases XP per correct solution
Speed
Fast submissions
Time-bonus multiplier on timed challenges
Endurance
Daily streaks
Streak shield (miss 1 day without penalty)
Creativity
Alt-solution submissions
Bonus XP for novel approaches
Collaboration
Guild tasks & code reviews
Guild XP multiplier

3. XP & Leveling System
3.1  XP Calculation Formula
Base XP is awarded per solved challenge using the following formula:
XP = Base_Difficulty × Speed_Multiplier × Streak_Bonus × Language_Weight

3.2  Difficulty Base XP
Difficulty
Base XP
Time Limit
Retry Penalty
Easy
100 XP
30 min
−5 XP / attempt
Medium
300 XP
60 min
−10 XP / attempt
Hard
700 XP
90 min
−15 XP / attempt
Expert
1,500 XP
120 min
−25 XP / attempt
Legend
3,000 XP
Unlimited
−50 XP / attempt

3.3  Bonus Multipliers
Speed Bonus: ×1.5 if solved in first half of time limit, ×1.2 if in first 75%
Streak Bonus: +5% XP per consecutive day, capped at +50%
First Blood: ×2.0 for being the first student globally to solve a new challenge
Language Depth: Each language has a Level (1–100). Harder challenges in that language award +1% per language level above 50
Guild Bonus: +10% XP on all challenges while in an active Guild

4. Programming Language Skill System
4.1  Language Levels — Live Leaderboard
Each programming language has an independent Level (1–100) for every student. These levels are displayed on a real-time public leaderboard that updates within seconds of a submission being judged.

4.2  Supported Languages at Launch
Language
Track
Challenge Count
Max Level
Status at Launch
Python
General / ML
400+
100
✅ Live
JavaScript
Web / Full-stack
350+
100
✅ Live
Java
Backend / OOP
320+
100
✅ Live
C++
Systems / CP
300+
100
✅ Live
TypeScript
Web / Full-stack
200+
100
✅ Live
Go
Cloud / Backend
150+
100
🔄 Beta
Rust
Systems / Safety
120+
100
🔄 Beta
SQL
Data / Backend
180+
100
✅ Live

4.3  Language Level XP Thresholds
Language levels follow an exponential curve: each level requires 10% more XP than the previous. Starting XP for Level 1 is 500; Level 100 requires approximately 56,000 cumulative XP in that language alone.
Levels 1–25: Apprentice & Coder tier challenges only
Levels 26–50: Engineer tier challenges unlocked
Levels 51–75: Architect tier + system design questions
Levels 76–99: Expert competitions, open-source project tasks
Level 100: Legend — invite-only tournaments, contributor badge

5. Challenge Design & Task Types
5.1  Challenge Categories
Category
Difficulty Range
Description
Algorithm Quest
Easy – Expert
Sorting, searching, graph traversal, DP — LeetCode-style problems
Bug Hunt
Easy – Hard
Find and fix intentional bugs in provided code snippets
Build Sprint
Medium – Expert
Mini features to implement within a scaffolded codebase
Code Review
Medium – Hard
Review peers' code and provide structured feedback (earns Collaboration XP)
System Design
Hard – Legend
Diagram and justify architecture for given system requirements
Daily Challenge
Mixed
One rotating challenge per day; first 100 completions earn bonus XP
Guild Raid
Hard – Legend
Collaborative multi-part problem requiring guild coordination

5.2  Challenge Lifecycle
Author submits challenge via Content Management System (CMS) with test cases.
Automated validator runs test suite; checks edge cases and time/space complexity.
AI review assistant flags ambiguity or missing constraints.
Human moderator approves and tags by language, category, difficulty, and concepts.
Challenge published to the platform; XP values computed automatically.
Post-launch: community upvote/downvote adjusts difficulty classification over time.

6. Live Leaderboard Specification
6.1  Leaderboard Views
The platform exposes four distinct leaderboard contexts, each with real-time updates via WebSocket:
View
Update Frequency
Columns Displayed
Global Overall
Real-time (<2 s)
Rank, Avatar, Username, Tier, Total XP, Languages Mastered
Per Language
Real-time (<2 s)
Rank, Avatar, Username, Language Level, XP in Language, Challenges Solved
Institution
Every 5 min
Rank, Institution, Top 3 Students, Average XP
Friends / Guild
Real-time (<2 s)
Rank within circle, Delta vs. last week, Head-to-head record

6.2  Leaderboard Technical Requirements
Redis Sorted Set per language and global; O(log N) rank updates after each submission.
WebSocket push on score change — clients subscribe to channels by leaderboard type.
Historical snapshots saved daily to PostgreSQL for trend analytics.
Anti-cheat: velocity limits (max XP gain per hour), anomaly detection on solution timing.
GDPR opt-out: users may hide their profile from public leaderboards while retaining XP.

7. Technical Architecture
7.1  System Components
Layer
Technology
Responsibilities
Frontend
Next.js 14 + React
Game UI, avatar rendering, code editor (Monaco), leaderboard panels
API Gateway
Kong / AWS API GW
Auth, rate limiting, request routing, SSL termination
Game Service
Node.js / NestJS
XP calculation, level-up events, avatar state, quest management
Code Judge
Isolate / nsjail
Sandboxed execution, test case running, time/memory measurement
Leaderboard
Redis 7 + Sorted Sets
Real-time rank storage, WebSocket broadcast, TTL-based windows
Persistence
PostgreSQL 15
User profiles, challenge metadata, submissions, guild data
Events
Apache Kafka
Submission events, XP events, notification triggers, analytics feed
Notifications
Firebase FCM + WS
Level-up, badge earned, guild invite, daily challenge alerts
CDN / Media
Cloudflare + S3
Avatar sprites, badge images, static assets, challenge attachments

7.2  Code Execution Sandbox
Every submission runs in a hermetically isolated container (nsjail) with strict resource constraints:
CPU time limit: configurable per difficulty (Easy: 2 s, Hard: 10 s, Legend: 30 s)
Memory limit: 256 MB default; 512 MB for system design simulation tasks
Network access: disabled; file system: read-only tmpfs
Language runtimes pre-warmed in container pool to reduce cold-start latency
Execution results published to Kafka topic; Game Service consumes and awards XP

7.3  Real-Time WebSocket Protocol
Clients maintain a persistent WebSocket connection for live updates:
On submission judged: { type: 'XP_AWARD', delta: 350, newTotal: 14230, rankChange: +12 }
On level-up: { type: 'LEVEL_UP', newTier: 'Engineer', unlockedChallenges: [...] }
On leaderboard change: { type: 'LB_UPDATE', board: 'python', rank: 42, score: 14230 }
On guild event: { type: 'GUILD_RAID_START', raidId: 'r99', timeLimit: 3600 }

8. Guild System
8.1  Guild Features
Guilds are persistent teams of 2–20 students. They provide a social layer and collaborative incentives:
Guild XP Pool: all member XP contributions accumulate; guilds have their own level (1–50).
Guild Raids: time-boxed cooperative challenges requiring 3+ members; massive XP rewards.
Guild Leaderboard: guilds ranked by total XP, average member level, and raids completed.
Guild Chat: integrated text channel with code-snippet sharing and reaction support.
Recruitment System: guilds post an open slot; students apply with portfolio stats.

8.2  Guild Roles
Role
Permissions
Grand Master
All permissions; can disband guild, set XP goals, initiate raids, manage all roles
Captain
Invite/kick members (not Grand Master), start practice sessions, pin messages
Member
Participate in raids, view guild analytics, contribute to guild XP pool

9. Achievement & Badge System
9.1  Achievement Categories
Category
Example Badge
Trigger Condition
Streak
🔥 7-Day Flame
Complete at least one challenge every day for N consecutive days
Mastery
🐍 Python Sage
Reach Level 50 or 100 in a specific language
Speed
⚡ Lightning Coder
Solve a Hard challenge in under 5 minutes with full score
Collector
🗝 Polyglot
Reach Level 25+ in 5 different languages
Social
🤝 Mentor
Provide 50 accepted code reviews rated 4+ stars
Seasonal
🏆 Season Champion
Finish in the top 1% of global leaderboard at season end

9.2  Badge Display
Top 5 badges displayed on public profile card and leaderboard entry.
All earned badges visible in profile trophy case with date and unlock story.
Rare badges (earned by < 1% of users) highlighted with animated border effect.
Badges exportable as verifiable credentials (Open Badges 3.0 standard).

10. Implementation Roadmap
10.1  Phase Overview
Phase
Duration
Team Size
Deliverables
0 — Discovery
2 weeks
3 people
Finalized PRD, Tech stack decision, Design system tokens
1 — Foundation
8 weeks
6 engineers
Auth, code judge sandbox, basic challenge CRUD, profile pages
2 — Game Layer
10 weeks
8 engineers
XP engine, avatar system, 6 tiers, achievement engine, streaks
3 — Social
8 weeks
7 engineers
Guilds, real-time leaderboards, code review system, notifications
4 — Content
6 weeks
5 engineers + content team
400+ challenges across 5 languages, CMS, moderation workflow
5 — Beta
4 weeks
Full team
Closed beta with 500 students, performance tuning, bug fixes
6 — Launch
2 weeks
Full team
Public launch, press kit, institution partnerships, Season 1 start

10.2  Sprint Structure
Each phase follows 2-week sprints with consistent ceremonies:
Sprint Planning (Monday): story point estimation, capacity planning, sprint goal set.
Daily Standups (15 min): blockers, progress, alignment.
Mid-Sprint Demo (Wednesday of Week 2): stakeholder preview of in-progress work.
Sprint Review + Retrospective (Friday): ship, reflect, backlog refinement.

11. Database Schema (Key Entities)
11.1  Core Tables
Table
Primary Key
Key Columns
users
user_id (UUID)
username, email, created_at, tier_id, total_xp, streak_days, guild_id
character_stats
user_id FK
intelligence, speed, endurance, creativity, collaboration (all INT)
language_progress
(user_id, lang_id)
xp, level, challenges_solved, first_solved_at, last_activity
challenges
challenge_id (UUID)
title, description, difficulty, category, lang_id, test_cases (JSONB), base_xp
submissions
submission_id (UUID)
user_id, challenge_id, code, language, status, runtime_ms, xp_awarded, submitted_at
achievements
achievement_id
name, description, icon_url, rarity, trigger_type, trigger_value
user_achievements
(user_id, achievement_id)
earned_at, display_order
guilds
guild_id (UUID)
name, description, total_xp, level, member_count, created_at, grand_master_id

12. REST API Specification (Core Endpoints)
12.1  Authentication
Method
Endpoint
Description
Auth
POST
/api/v1/auth/register
Create new student account
None
POST
/api/v1/auth/login
Obtain JWT access + refresh tokens
None
GET
/api/v1/users/{id}/profile
Full profile, tier, stats, top badges
Optional

12.2  Challenges & Submissions
Method
Endpoint
Description
Auth
GET
/api/v1/challenges
List challenges (filter: lang, difficulty, category)
JWT
GET
/api/v1/challenges/{id}
Challenge detail with examples and constraints
JWT
POST
/api/v1/submissions
Submit solution; returns submission_id for polling
JWT
GET
/api/v1/submissions/{id}
Poll submission result and XP awarded
JWT

12.3  Leaderboards
Method
Endpoint
Description
GET
/api/v1/leaderboards/global
Top N students by total XP (paginated, 50/page)
GET
/api/v1/leaderboards/language/{lang}
Top N students by XP in specified language
GET
/api/v1/leaderboards/me/rank
Authenticated user's rank on all leaderboards
WS
wss://api/v1/leaderboards/live
WebSocket stream; subscribe to one or more board channels

13. Security & Compliance
13.1  Security Requirements
All API traffic served over TLS 1.3; HSTS enforced with 1-year max-age.
JWT access tokens expire in 15 minutes; refresh tokens expire in 30 days with rotation.
Code execution sandbox: seccomp BPF profile blocks all syscalls except a safe allowlist.
Anti-cheat velocity limits: maximum 500 XP/minute; submissions flagged above threshold.
OWASP Top-10 scan integrated into CI pipeline; blocking on High/Critical findings.
Secrets managed via AWS Secrets Manager; no secrets in source code or environment files.

13.2  Privacy & Data Compliance
GDPR compliant: data export endpoint, right-to-erasure workflow, cookie consent banner.
Leaderboard opt-out: users may set profile to private; XP still counted in system.
Student data (under-18): parental consent flow for institutions onboarding minors.
Data residency: EU and US data zones selectable at institution onboarding.

14. Appendix
14.1  Glossary
Term
Definition
XP
Experience Points — the primary progression currency of Code Quest
Tier
One of six character evolution stages: Apprentice, Coder, Engineer, Architect, Wizard, Legend
Language Level
A 1–100 score representing proficiency in a specific programming language
Guild
A named team of 2–20 students with shared XP pool and cooperative challenges
Raid
A time-limited multi-part challenge requiring a guild to collaborate
First Blood
Bonus for being the first globally to solve a newly published challenge
Sandbox
Isolated execution environment for running student code safely
Season
A recurring competitive cycle (typically 3 months) with exclusive prizes and badges

14.2  Revision History
Version
Date
Author
Changes
1.0
June 2026
Product Team
Initial release — full PRD & implementation guide