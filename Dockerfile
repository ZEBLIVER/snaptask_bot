# --- Этап 1: Сборка (Build Stage) ---
# Используем образ с Maven и Java 21 для компиляции
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем pom.xml, чтобы Docker мог закэшировать скачивание зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходный код и собираем проект
COPY src /app/src
# Собираем JAR-файл, пропуская тесты для скорости
RUN mvn package -DskipTests

# --- Этап 2: Запуск (Run Stage) ---
# Используем минимальный образ с Java 21 JRE для уменьшения размера
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный JAR-файл из этапа 'build' в финальный образ
# Имя JAR-файла: <artifactId>-<version>.jar
COPY --from=build /app/target/snaptask-bot-0.0.1-SNAPSHOT.jar app.jar

# Порт, который слушает Spring Boot
EXPOSE 8081

# Команда для запуска приложения при старте контейнера
ENTRYPOINT ["java", "-jar", "app.jar"]