# SnapTask Bot

Max-бот для  с интеллектуальным парсингом естественного языка.

## Описание проекта

SnapTask - это бот напоминаний, который автоматически парсит сообщения пользователей и создает задачи с напоминаниями. Бот поддерживает:

- **Умный парсинг** естественного языка для извлечения дат, времени и повторяющихся событий
- **Поддержка часовых поясов** для пользователей из разных регионов
- **Архитектура на портах** (Port-Adapter) для легкого расширения функционала

## Технологический стек

- **Java 21** - основной язык разработки
- **Spring Boot 3.5.7** - фреймворк для создания приложения
- **Spring Data JPA** - работа с базой данных
- **H2 Database** - встраиваемая база данных
- **Maven** - система сборки
- **Docker** - контейнеризация
- *Полный список зависимостей доступен в `pom.xml`.*


## Архитектура

Проект построен по принципам чистой архитектуры (Clean Architecture) и порт-адаптер:

- **Domain Layer** - содержит бизнес-логику и модели.
- **Application Layer** - обработчики обновлений и сервисы приложения.
- **Infrastructure Layer** - взаимодействие с внешними системами.

---

## Быстрый старт

### Требования

- Docker и Docker Compose
- Java 21 (для локального запуска)
- Maven 3.9+


### Запуск через Docker Compose (рекомендуется)


1. **Отредактируйте файл `.env` с переменными окружения:**
```bash
MAX_BOT_TOKEN=your_actual_bot_token_here
```

2. **Запуск приложения:**
```bash
# Сборка и запуск 
docker compose up -d --build

# Проверка статуса контейнера (должен быть "running (healthy)")
docker compose ps

# Просмотр логов
docker compose logs -f snaptask-bot

# Остановка
docker compose down
```

### Файл конфигурации

Основные настройки находятся в `src/main/resources/application.properties`:


### Получить справку в боте
```
"/start" 
```

---

## Локальный запуск

### Настройка переменных окружения

Для локального запуска добавьте в `src/main/resources/application.properties` три ключа:

```properties
MAX_API_URL=https://platform-api.max.ru/
MAX_API_SEND_MESSAGE_URL=https://platform-api.max.ru/messages
MAX_BOT_TOKEN=your_actual_bot_token_here
```

### Сборка и запуск

```bash
# Сборка проекта
mvn clean package

# Запуск через JAR
java -jar target/snaptask-bot-0.0.1-SNAPSHOT.jar

# Или через Maven
mvn spring-boot:run
```

### Запуск через Docker

```bash
# Сборка образа
docker build -t snaptask-bot .

# Запуск контейнера
docker run -p 8081:8081 \
  -e MAX_BOT_TOKEN=your_actual_bot_token_here \
  -e MAX_API_URL=https://platform-api.max.ru/ \
  -e MAX_API_SEND_MESSAGE_URL=https://platform-api.max.ru/messages \
  snaptask-bot
```

