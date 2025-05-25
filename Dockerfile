# Dockerfile.flink-job

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /opt/app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests=true

FROM flink:1.20.0-java17

WORKDIR /opt/flink/usrlib

COPY --from=builder /opt/app/target/flink-job-SHADE.jar /opt/flink/usrlib/app.jar