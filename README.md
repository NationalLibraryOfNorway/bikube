# BIKUBE - Katalog API for tekstmateriale på nasjonalbiblioteket

REST-API for kommunikasjon mot kataloger som inneholder tekstlig materiale på Nasjonalbiblioteket.

Laget med Kotlin og Spring Boot.

## Lokal utvikling

### Forutsetninger
Maven og JDK 17 (anbefaler Temurin) må være installert

### Kjøring
For lokal utvikling må url til Axiell Collections-APIet legges inn, og man må sette verdier for Kerberos
dersom APIet er satt opp med autentisering.
Anbefaler å lage en egen `application-local.yml` under `src/main/resources` (vil bli ignorert av gitignore).

| Påkrevde variabler       | Forklaring                       |
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


Kjør `java -jar target/bikube.jar` eller sett opp i din IDE. APIet kjører default på port 8080.

### Testing
Kjør `mvn clean verify` for å kjøre alle tester og bygge prosjektet.

## Utrulling
Pr. nå er applikasjonen ikke rullet ut. 
I nær framtid blir image bygget i GitHub og lagt på dockerhub, mens utrulling på platform må gjøres selv.

For NB-utviklere: deployment-filer vil ligge på det interne versjonskontroll-systemet.

## Vedlikehold
Tekst-teamet på Nasjonalbibliotekets IT-avdeling vedlikeholder Bikube.

Alle kan lage issues, men vi kan ikke garantere at alle blir tatt tak i. 
Interne behov går foran eksterne forespørsler.
