# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Answer reports** — any signed-in user can dispute a question's correct answer with a
  justification (`POST /api/questions/{id}/report`, `GET /api/reports/me`); admins review
  reports in a new "Signalements / Reports" tab and can accept (optionally fixing the
  correct option) or reject them (`GET /api/admin/reports`, `PUT /api/admin/reports/{id}/resolve`).
  See PR #16.
- Quiz sessions draw 15 questions instead of 8 (`app.quiz.questions-per-session=15`). See PR #15.

## [1.0.0] - 2026-11-20

Initial release.

### Added
- **Backend**: Spring Boot 4.1.2 on Java 25, in-memory H2, JWT authentication
  (`/api/auth/register|login|me`, BCrypt), roles `USER`/`ADMIN` with a bootstrap admin
  (`APP_ADMIN_USERNAME`/`APP_ADMIN_PASSWORD`), fr/en responses via `Accept-Language`.
- **Quiz content**: 5 levels, 75 bilingual (fr/en) questions across the six Russian cases,
  including a dedicated exceptions level (в году, глаз/раз, -мя nouns, дети/люди) and a
  final вечер (evening) review level; per-question hints and linked case rules.
- **Random quiz sessions**: starting a level draws a random subset of questions
  (`POST /api/levels/{id}/sessions`, default 8 per quiz), answers validated server-side
  per session (`POST /api/sessions/{id}/answer`), per-user progress
  (`/api/progress/me[/summary]`).
- **Admin API + admin UI**: full CRUD for levels/questions/rules, user role management
  (self/last-admin guards), progress listing/reset (`/api/admin/**`, ADMIN role only),
  and a fr/en "Administration" section in the webapp shown only to admins.
- **Webapp**: fr/en single-page app served at `http://localhost:8080` — auth, level list,
  quiz with hints/rule reminders, progress summary; responsive with a11y support.
- **Android app**: fr/en app (Kotlin, minSdk 21 / targetSdk 29) with sign-in, level list,
  quiz and progress; built as a zipaligned APK (CI, JDK 8 + SDK 29), downloadable as the
  `russian-cases-android-apk` workflow artifact.
- **CI**: GitHub Actions — backend+webapp build/tests on JDK 25, Android APK build on JDK 8,
  APK artifact upload.

### Changed
- Migrated the project to Spring Boot 4.1.2 / Java 25 (Actions `setup-java` v6, Temurin 25).
