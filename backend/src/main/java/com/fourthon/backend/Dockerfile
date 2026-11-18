
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY . .

RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon bootJar \
    && find build/libs -maxdepth 1 -type f \
       -name '*.jar' ! -name '*-plain.jar' \
       -exec cp '{}' /workspace/app.jar \;

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/app.jar /app/app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
