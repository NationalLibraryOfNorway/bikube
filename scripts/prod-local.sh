#!/usr/bin/env bash
set -e

# Compile, start Spring, generate openapi.json from the live API, stop Spring
./mvnw compile spring-boot:start org.springdoc:springdoc-openapi-maven-plugin:generate spring-boot:stop \
    -Dspring-boot.run.profiles=local \
    -Dspring-boot.run.arguments="--search-index.enabled=false" \
    -Dopenapi.generate.skip=false \
    -Dspringdoc.api.docs.url=http://localhost:9000/bikube/v3/api-docs

# prepare-package: installs bun, bun install, orval (reads fresh openapi.json), vitest, vite build → target/classes/static/hugin
# package: assembles the JAR (target/classes/static/hugin is picked up automatically)
./mvnw package -DskipTests

# Run the actual JAR — static files are bundled inside, no classpath ambiguity
java -jar target/bikube.jar --spring.profiles.active=local --server.port=8087
