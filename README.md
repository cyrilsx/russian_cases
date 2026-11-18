# Russian Cases / Les cas russes

Application bilingue (français / anglais) pour apprendre et s'exercer aux cas grammaticaux russes :
webapp + application Android + backend REST.

Bilingual (French / English) app to learn and practise Russian grammatical cases:
webapp + Android app + REST backend.

Fonctionnalités / Features:

- Quiz par niveau (nominatif, génitif, datif, accusatif, instrumental, prépositionnel)
- Quizzes by level (nominative, genitive, dative, accusative, instrumental, prepositional)
- Indices par question et rappels de règles / hints per question and rule reminders
- Suivi de progression par utilisateur / per-user progress tracking
- Interface en français et en anglais (`Accept-Language: fr` / `en`)

## Structure

```
russian-cases/
├── pom.xml                  # parent POM (Maven multi-module)
├── backend/                 # Spring Boot 4 REST API (Java 25, H2 in-memory)
├── webapp/                  # webapp statique (HTML/CSS/JS vanilla, fr/en), servie par le backend
├── android/                 # application Android (packaging APK via android-maven-plugin)
└── .github/workflows/ci.yml # CI GitHub Actions
```

## Prérequis / Prerequisites

- JDK 25 + Maven 3.9+ pour le backend et la webapp
- JDK 8 + Android SDK (platform 29, build-tools 30.0.3, `ANDROID_HOME` renseigné) pour le module Android
  (android-maven-plugin 4.6.0 et son `dx` historique nécessitent JDK 8)

## Lancer le backend + la webapp / Run backend + webapp

```bash
mvn -pl webapp,backend spring-boot:run
# puis ouvrir http://localhost:8080
```

The webapp is served by the backend at [http://localhost:8080](http://localhost:8080).

## API

Toutes les routes `/api/**` (sauf `register`/`login`) nécessitent un en-tête
`Authorization: Bearer <token>` obtenu via `/api/auth/register` ou `/api/auth/login`.
All `/api/**` routes (except `register`/`login`) require an `Authorization: Bearer <token>`
header obtained from `/api/auth/register` or `/api/auth/login`.

| Méthode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/auth/register` | Créer un compte (`{"username": "...", "password": "..."}`) → `201 {"token", "username"}` |
| POST | `/api/auth/login` | Se connecter → `200 {"token", "username"}` (401 si invalide) |
| GET | `/api/auth/me` | Utilisateur courant |
| GET | `/api/levels` | Liste des niveaux (avec nombre de questions) |
| GET | `/api/levels/{id}/questions` | Questions d'un niveau |
| GET | `/api/questions/{id}` | Une question |
| POST | `/api/questions/{id}/answer` | Répondre (`{"optionIndex": 0}`) → résultat + indice + règle |
| GET | `/api/cases` | Les 6 cas grammaticaux |
| GET | `/api/rules?case=GENITIVE` | Rappels de règles (filtre par cas optionnel) |
| GET | `/api/progress/me` | Progression détaillée de l'utilisateur connecté |
| GET | `/api/progress/me/summary` | Résumé de progression par niveau |

La langue est choisie via l'en-tête `Accept-Language: fr` ou `en`.
Language is selected with the `Accept-Language: fr` or `en` header.

## Sécurité / Security

- Authentification par **JWT (HS256, jjwt)** stateless via Spring Security ; les mots de passe
  sont hachés avec **BCrypt**. L'identité utilisée pour la progression provient du token, plus
  du `userId` envoyé par le client.
- Stateless **JWT (HS256, jjwt)** authentication via Spring Security; passwords are hashed
  with **BCrypt**. Progress is tied to the identity from the token, not to a client-supplied id.
- En dev, la clé JWT est dans `backend/src/main/resources/application.properties`
  (`app.jwt.secret`). **En production**, la définir via la variable d'environnement
  `APP_JWT_SECRET` (≥ 64 caractères) et servir l'API uniquement en **HTTPS**.
- In dev the JWT key lives in `application.properties` (`app.jwt.secret`). **In production**
  set it via the `APP_JWT_SECRET` environment variable (≥ 64 chars) and serve the API over
  **HTTPS** only.

## Android

```bash
export ANDROID_HOME=/chemin/vers/android-sdk   # platform android-29 + build-tools 30.0.3
mvn -pl android package                          # produit android/target/*.apk (JDK 8)
```

Le module `android` n'est inclus dans le build que si `ANDROID_HOME` est défini (profil Maven `android`).

## CI

`.github/workflows/ci.yml` : deux jobs — backend+webapp (JDK 25, `mvn verify -pl webapp,backend`)
et APK Android (JDK 8 + SDK 29, `mvn verify -pl android`).
