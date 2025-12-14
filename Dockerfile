FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml ./
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=builder /app/target/*-exec.jar /app/app.jar

RUN addgroup --system triegroup \
    && adduser --system --ingroup triegroup --home /app trieuser \
    && mkdir -p /app/backups \
    && chown -R trieuser:triegroup /app/backups /app
USER trieuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]