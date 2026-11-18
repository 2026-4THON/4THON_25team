
# 1. Spring Boot 빌드
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# backend 폴더 전체 복사
COPY . .

# Gradle 실행 및 JAR 생성
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon clean bootJar

# 실행 가능한 JAR 파일 복사
RUN JAR_FILE="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit)" \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /workspace/app.jar

# 2. Spring Boot 실행
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/app.jar /app/app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
