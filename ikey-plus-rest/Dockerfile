FROM openjdk:25-ea-1-slim AS runner

WORKDIR /app

COPY target/ikey-plus-rest-2.0-SNAPSHOT-jar-with-dependencies.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]