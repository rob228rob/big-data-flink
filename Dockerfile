# Этап сборки
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /opt/app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Собираем оба варианта JAR-файлов
RUN mvn clean package -DskipTests=true

# Финальный образ для Flink Job
FROM flink:1.20.0-java17

WORKDIR /opt/flink/usrlib

# Копируем fat-jar с зависимостями
COPY --from=builder /opt/app/target/big-data-flink-svc-*-jar-with-dependencies.jar /opt/flink/usrlib/app.jar

# Запускаем через Flink CLI
CMD ["bash", "-c", "${FLINK_HOME}/bin/flink run -d /opt/flink/usrlib/app.jar"]