FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/bikube.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java","-jar","/app.jar"]
