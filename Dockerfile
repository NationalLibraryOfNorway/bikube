FROM eclipse-temurin:17-jdk-alpine
RUN echo $(pwd && ls -lR)
COPY bikube.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java","-jar","/app.jar"]
