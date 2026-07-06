# BIKUBE - Katalog API for tekstmateriale på nasjonalbiblioteket

REST-API for kommunikasjon mot kataloger som inneholder tekstlig materiale på Nasjonalbiblioteket.
Inkluderer et React-basert grensesnitt (Hugin) som serveres direkte fra Spring-applikasjonen i produksjon.

Laget med Kotlin, Spring Boot og React (Vite + TypeScript).

## Lokal utvikling

### Forutsetninger

- Maven og JDK 21 (anbefaler Temurin)
- [Bun](https://bun.sh/) (pakkehåndterer og skriptmiljø for frontenden)

### Konfigurasjon

For lokal utvikling må nødvendige variabler legges inn. Anbefaler å lage en egen
`application-local.yml` under `src/main/resources` (vil bli ignorert av gitignore).

| Påkrevd variabel         | Forklaring                       |
|--------------------------|----------------------------------|
| COLLECTIONS_URL          | Base URL til Collections API-et  |
| COLLECTIONS_USERNAME     | AD-brukernavn til Collections    |
| COLLECTIONS_PASSWORD     | AD-passord til Collections       |
| COLLECTIONS_DIRECTLINK   | Direkte URL til Collections      |
| KEYCLOAK_ISSUER_URI      | Keycloak issuer uri              |
| KEYCLOAK_CLIENT_ID       | Keycloak id                      |
| KEYCLOAK_CLIENT_SECRET   | Keycloak secret                  |
| POSTGRES_URL             | URL til postgres-databasen       |
| POSTGRES_USERNAME        | Brukernavn til postgres          |
| POSTGRES_PASSWORD        | Passord til postgres             |
| alma.alma-sru-url        | Base URL til Alma SRU API-et     |
| alma.alma-ws-url         | Base URL til AlmaWS API-et       |
| alma.api-key             | API-nøkkel til AlmaWS API-et     |

### Kjøring – utvikling (`dev:all`)

```sh
bun run dev:all
```

Starter tre prosesser parallelt via `concurrently`:

1. **Spring** – `./mvnw spring-boot:run` med `local`-profil (backend på port 9000)
2. **Orval** – watch-modus som regenererer API-klienten ved endringer i `target/openapi.json`
3. **Vite** – dev-server med HMR for frontenden (port 5173)

Frontenden er i denne modusen servert av Vite med proxy til Spring for API-kall.
API-klienten i `src/main/frontend/src/api/` genereres av [Orval](https://orval.dev/) fra
OpenAPI-spesifikasjonen som Spring eksponerer via springdoc.

### Kjøring – produksjonslikt lokalt (`prod:local`)

```sh
bun run prod:local
```

Bygger og kjører applikasjonen slik den vil kjøre i produksjon – frontenden er bygget med
Vite og pakket inn i JAR-filen, og serveres av Spring på `/bikube/hugin/`.

Skriptet gjør følgende i riktig rekkefølge:

1. Kompilerer backend og starter Spring midlertidig for å generere `target/openapi.json`
2. Kjører Orval for å generere API-klienten fra spesifikasjonen
3. Bygger frontenden med `bun run build`
4. Pakker alt inn i én JAR med `./mvnw package`
5. Starter JAR-en på port 8087

Bruk `prod:local` for å verifisere at bygg-pipeline og statisk filservering fungerer som
forventet før deploy.

### Testing

```sh
nix-shell -p maven jdk21 --run "mvn clean verify"
```

## Arkitektur

- **Backend:** Kotlin + Spring WebFlux
- **Frontend:** React 19, TypeScript, Vite, Tailwind CSS
- **API-klient:** Generert av Orval fra OpenAPI-spec (springdoc-openapi)
- **Autentisering:** OAuth2 / OIDC via Keycloak (både browser-login og JWT resource server)
- **Frontend i produksjon:** Serveres fra JAR under `classpath:static/hugin/` med SPA-fallback

## Utrulling

Image bygges i GitHub og legges på et internt container-register.
For NB-utviklere: deployment-filer ligger på det interne versjonskontrollsystemet.

## Vedlikehold

Tekst-teamet på Nasjonalbibliotekets IT-avdeling vedlikeholder Bikube.

Alle kan lage issues, men vi kan ikke garantere at alle blir tatt tak i.
Interne behov går foran eksterne forespørsler.
