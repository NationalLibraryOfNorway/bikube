#!/usr/bin/env bash
set -e

./mvnw package

java -jar target/bikube.jar --spring.profiles.active=local
