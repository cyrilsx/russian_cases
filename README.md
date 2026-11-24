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
  (android-maven-plugin 4.6.0 et son `aapt`/`dx` historiques nécessitent JDK 8 ; `aapt` v1 ne peut pas
  lire les resources des plateformes 34+, donc on compile contre la plateforme 29 — voir note ci-dessous)

## Lancer le backend + la webapp / Run backend + webapp

```bash
mvn install -DskipTests                  # une fois : construit et installe tous les modules (dont le jar webapp)
mvn -pl backend spring-boot:run          # démarre le backend sur http://localhost:8080
# puis ouvrir http://localhost:8080
```

The webapp is served by the backend at [http://localhost:8080](http://localhost:8080).
Do not run `mvn spring-boot:run` on the root or with `-pl webapp,backend` without the
install step: the `webapp` module has no main class (the plugin is configured to skip it),
and the backend needs the installed webapp jar on its classpath.

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
| GET | `/api/levels/{id}/questions` | Toutes les questions d'un niveau (admin) |
| GET | `/api/questions/{id}` | Une question |
| POST | `/api/questions/{id}/answer` | Répondre (`{"optionIndex": 0}`) → résultat + indice + règle |
| POST | `/api/questions/{id}/report` | Signaler une réponse avec justification (`{"claimedCorrectIndex": 1, "justification": "..."}`) → 201, 409 si signalement déjà ouvert |
| GET | `/api/reports/me` | Mes signalements |
| POST | `/api/levels/{id}/sessions` | Démarrer un quiz : sélection aléatoire de `app.quiz.questions-per-session` (8 par défaut) questions → session |
| GET | `/api/sessions/{id}` | État d'une session de quiz |
| POST | `/api/sessions/{id}/answer` | Répondre dans une session (`{"questionId": 1, "optionIndex": 0}`) → résultat + indice + règle + `answered`/`total` (réessai autorisé après une mauvaise réponse, `answered` compte les questions distinctes ; 400 si hors session) |
| GET | `/api/cases` | Les 6 cas grammaticaux |
| GET | `/api/rules?case=GENITIVE` | Rappels de règles (filtre par cas optionnel) |
| GET | `/api/progress/me` | Progression détaillée de l'utilisateur connecté |
| GET | `/api/progress/me/summary` | Résumé de progression par niveau |
| GET | `/api/auth/me` | Utilisateur courant + `role` (`USER` / `ADMIN`) |

### Administration (`/api/admin/**`, rôle `ADMIN` requis)

| Méthode | Endpoint | Description |
| --- | --- | --- |
| GET/POST/PUT/DELETE | `/api/admin/levels[/{id}]` | CRUD des niveaux (champs fr + en) |
| GET/POST/PUT/DELETE | `/api/admin/questions[/{id}]` (`?levelId=` au GET) | CRUD des questions (options fr/en, `correctIndex`, indice, `ruleId`) |
| GET/POST/PUT/DELETE | `/api/admin/rules[/{id}]` | CRUD des règles (une par cas) |
| GET | `/api/admin/users` | Liste des utilisateurs |
| PUT | `/api/admin/users/{id}/role` | Changer un rôle (`{"role": "ADMIN"}`) — pas son propre rôle, pas le dernier admin |
| DELETE | `/api/admin/users/{id}` | Supprimer un utilisateur (+ sa progression) |
| GET | `/api/admin/progress` | Progression de tous les utilisateurs |
| DELETE | `/api/admin/progress/{userId}` | Réinitialiser la progression d'un utilisateur |
| GET | `/api/admin/reports?status=OPEN` | Lister les signalements de réponses |
| PUT | `/api/admin/reports/{id}/resolve` | Traiter un signalement (`{"status": "ACCEPTED", "correctIndex": 1, "note": "..."}` — `correctIndex` optionnel, corrige la question si fourni) |

Un compte admin est créé au démarrage si absent : `app.admin.username` / `app.admin.password`
(variables d'environnement `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD` — à changer en production).

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
# SDK autonome recommandé (pas le dossier /usr/share/android-studio, non modifiable sans root) :
export ANDROID_HOME=$HOME/Android/Sdk
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "platforms;android-29" "build-tools;30.0.3"
# le build crée lui-même $ANDROID_HOME/tools/source.properties (shim exigé par android-maven-plugin)
mvn -pl android package                          # produit android/target/*.apk (JDK 8)
```

Signature de l'APK : par défaut (CI et local) l'APK est signé avec la **clé debug**. Pour une release
signée, créez un keystore et renseignez les secrets du dépôt :

```bash
keytool -genkeypair -v -keystore release.keystore -storetype JKS -alias russian-cases \
  -keyalg RSA -keysize 2048 -validity 10000 -storepass *** -keypass *** -dname "CN=Russian Cases"
# secrets GitHub : ANDROID_KEYSTORE_B64 = $(base64 -w0 release.keystore),
# ANDROID_STORE_PASSWORD, ANDROID_KEY_PASSWORD, ANDROID_KEY_ALIAS
```

Un tag `v*` déclenche `release.yml` qui construit l'APK signé release (via le plugin Maven avec
`-Dandroid.sign.debug=false` + variables d'env, puis re-signé v2/v3 et vérifié avec `apksigner`)
et le publie dans la Release GitHub. En local :
`ANDROID_KEYSTORE=$PWD/release.keystore ANDROID_STORE_PASS=*** ANDROID_KEY_PASS=*** ANDROID_KEY_ALIAS=russian-cases mvn -pl android package -Dandroid.sign.debug=false`

Les sources du module Android sont en **Kotlin** (`android/src/main/kotlin`, compilées par
kotlin-maven-plugin 1.3.72, `jvmTarget 1.8`). `minSdkVersion` passe à 26 (Android 8) car `dx` n'accepte les `invokedynamic` de Kotlin qu'à partir de l'API 26. L'app **cible
Android 16** (`targetSdkVersion 36`, `minSdkVersion 26`) : elle s'installe et tourne sur Android 16.
La compilation se fait contre la plateforme 29 car le `aapt` v1 d'android-maven-plugin 4.6.0 ne sait
pas parser les resources des plateformes récentes ; passer `compileSdk` à 36 nécessiterait de migrer
le build vers AGP/Gradle.

L'app se connecte à l'API authentifiée (JWT) : écran de connexion/inscription, liste des
niveaux, quiz avec indice et rappel de règle, progression (`/api/progress/me/summary`).
Par défaut elle pointe sur `http://10.0.2.2:8080` (localhost de l'hôte vu depuis
l'émulateur Android) — modifiable dans l'écran de connexion. En production, utiliser
une URL **HTTPS** (le manifeste autorise le cleartext uniquement pour le dev local).

The app talks to the authenticated API (JWT): sign-in/register screen, level list, quiz
with hint and rule reminder, progress. It defaults to `http://10.0.2.2:8080` (the host's
localhost from the Android emulator), editable on the sign-in screen. In production use
an **HTTPS** URL (the manifest allows cleartext for local dev only).

Le module `android` n'est inclus dans le build que si `ANDROID_HOME` est défini (profil Maven `android`).

## CI

`.github/workflows/ci.yml` : trois jobs — backend+webapp (JDK 25, `mvn verify -pl webapp,backend`, publie
les JAR `backend/target/*.jar` et `webapp/target/*.jar` comme artefact d'actions
`nommé russian-cases-backend-webapp-jars`),
vérifications d'intégration de la webapp (contrat API `webapp/tests/contract_check.py` + test E2E
Node `webapp/tests/e2e_smoke.mjs`, exécutés en local avec `python3 webapp/tests/contract_check.py`
et `node webapp/tests/e2e_smoke.mjs`) et APK Android (JDK 8 + SDK 29, `mvn verify -pl android`, publie l'APK comme artefact
`russian-cases-android-apk`). Un tag `v*` déclenche `.github/workflows/release.yml` qui construit
les JAR + l'APK et les publie dans une **Release GitHub** (nécessite `contents: write` sur le
`GITHUB_TOKEN`, déjà configuré dans le workflow).

## Déploiement / Deployment

The backend is a single executable jar with the webapp bundled in (`mvn -pl webapp,backend package` → `backend/target/backend-1.0.0-SNAPSHOT.jar`). CI uploads it as the `russian-cases-backend-jar` artifact.

### Docker
```bash
docker build -t russian-cases .
docker run -p 8080:8080 russian-cases                                   # dev (H2 in-memory)
SPRING_PROFILES_ACTIVE=prod APP_JWT_SECRET=$(openssl rand -base64 64) \
  APP_ADMIN_PASSWORD=... docker run -p 8080:8080 russian-cases           # prod profile
```

### Exoscale
- **Compute instance** (recommended to start): create an Ubuntu 24.04 instance in your zone, open a security group (22 from your IP, 80/443 public), attach an Elastic IP + DNS A record. Install Temurin JDK 25, copy the jar, run it as a systemd service, put nginx + certbot in front (443 → 8080). See the env vars below.
- **SOKS (Kubernetes)**: build/push the image from the `Dockerfile`, deploy with a Deployment + Service + Ingress + cert-manager; secrets via a Kubernetes Secret.
- **Exoscale PAAS**: git-push deploys with a `Procfile` (`web: java -jar backend/target/backend-1.0.0-SNAPSHOT.jar`).

### Variables d'environnement (profil `prod`, `application-prod.properties`)
| Variable | Requis | Rôle |
|---|---|---|
| `APP_JWT_SECRET` | oui | secret de signature JWT (démarrage impossible sinon) |
| `APP_ADMIN_PASSWORD` | oui | mot de passe du compte admin initial |
| `APP_ADMIN_USERNAME` | non (admin) | nom du compte admin initial |
| `SERVER_PORT` | non (8080) | port d'écoute |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` / `_DRIVER` | recommandé | PostgreSQL (ex. Aiven Exoscale) — sinon H2 en mémoire (données perdues au redémarrage) |
| `APP_JWT_TTL_MINUTES` | non (10080) | durée de validité du token |
| `APP_QUIZ_QUESTIONS_PER_SESSION` | non (15) | questions tirées par quiz |

The app already sends strict security headers (CSP `default-src 'self'`, HSTS, frame-deny, …) and `server.forward-headers-strategy=framework` in prod for TLS-terminating proxies.
