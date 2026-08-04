FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon || true

COPY src src

RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd -r -s /bin/false spring

USER spring

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]