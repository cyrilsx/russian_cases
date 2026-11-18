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
├── backend/                 # Spring Boot 4 REST API (Java 21, H2 in-memory)
├── webapp/                  # webapp statique (HTML/CSS/JS vanilla, fr/en), servie par le backend
├── android/                 # application Android (packaging APK via android-maven-plugin)
└── .github/workflows/ci.yml # CI GitHub Actions
```

## Prérequis / Prerequisites

- JDK 21 + Maven 3.9+ pour le backend et la webapp
- JDK 8 + Android SDK (platform 29, build-tools 30.0.3, `ANDROID_HOME` renseigné) pour le module Android

## Lancer le backend + la webapp / Run backend + webapp

```bash
mvn -pl webapp,backend spring-boot:run
# puis ouvrir http://localhost:8080
```

The webapp is served by the backend at [http://localhost:8080](http://localhost:8080).

## API

| Méthode | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/levels` | Liste des niveaux (avec nombre de questions) |
| GET | `/api/levels/{id}/questions` | Questions d'un niveau |
| GET | `/api/questions/{id}` | Une question |
| POST | `/api/questions/{id}/answer` | Répondre (`{"userId": "...", "optionIndex": 0}`) → résultat + indice + règle |
| GET | `/api/cases` | Les 6 cas grammaticaux |
| GET | `/api/rules?case=GENITIVE` | Rappels de règles (filtre par cas optionnel) |
| GET | `/api/progress/{userId}` | Progression détaillée |
| GET | `/api/progress/{userId}/summary` | Résumé de progression par niveau |

La langue est choisie via l'en-tête `Accept-Language: fr` ou `en`.
Language is selected with the `Accept-Language: fr` or `en` header.

## Android

```bash
export ANDROID_HOME=/chemin/vers/android-sdk   # platform android-29 + build-tools 30.0.3
mvn -pl android package                          # produit android/target/*.apk (JDK 8)
```

Le module `android` n'est inclus dans le build que si `ANDROID_HOME` est défini (profil Maven `android`).

## CI

`.github/workflows/ci.yml` : deux jobs — backend+webapp (JDK 21, `mvn verify -pl webapp,backend`)
et APK Android (JDK 8 + SDK 29, `mvn verify -pl android`).
