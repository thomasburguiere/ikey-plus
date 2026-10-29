FROM maven:eclipse-temurin AS builder

WORKDIR /build

COPY pom.xml .
COPY ikey-plus-api ikey-plus-api
COPY ikey-plus-rest ikey-plus-rest

RUN mvn -B -pl ikey-plus-rest -am package -DskipTests

FROM eclipse-temurin:25-jre-alpine AS runner

RUN addgroup -S ikey && adduser -S -G ikey ikey

WORKDIR /app
COPY --from=builder /build/ikey-plus-rest/target/*-jar-with-dependencies.jar app.jar

USER ikey

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]