FROM harbor.nb.no/cache/eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8087 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
