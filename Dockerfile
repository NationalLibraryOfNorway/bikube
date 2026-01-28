FROM harbor.nb.no/cache/eclipse-temurin:21-jdk-alpine
COPY target/bikube.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java","-jar","/app.jar"]
