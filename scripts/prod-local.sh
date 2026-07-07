#!/usr/bin/env bash
set -e

# Compile, start Spring, generate target/openapi.json, stop Spring
./mvnw compile spring-boot:start org.springdoc:springdoc-openapi-maven-plugin:generate spring-boot:stop \
    -Dspring-boot.run.profiles=local \
    -Dspring-boot.run.arguments="--search-index.enabled=false" \
    -Dfrontend.build.skip=true \
    -Dopenapi.generate.skip=false \
    -Dspringdoc.api.docs.url=http://localhost:9000/bikube/v3/api-docs

# Generate API client from target/openapi.json, then build frontend into src/main/resources/static/hugin/
bun run generate
bun run build

# Package JAR (process-resources now copies the freshly built frontend; skip tests and Vite re-run)
./mvnw package -DskipTests -Dfrontend.build.skip=true

# Run the actual JAR — static files are bundled inside, no classpath ambiguity
java -jar target/bikube.jar --spring.profiles.active=local --server.port=8087
