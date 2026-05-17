# Запуск

## Через Docker Compose

Собрать и поднять сервисы:

```bash
docker compose up --build
```

- Приложение: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Локально (без Docker для приложения)

1) Поднять PostgreSQL:

```bash
docker compose up -d postgres
```

2) Запустить приложение:

```bash
./mvnw spring-boot:run
```

Конфигурация подключения к PostgreSQL по умолчанию находится в `src/main/resources/application.properties`.

## Запуск тестов

```bash
./mvnw test
```

# Архитектура проекта

```mermaid
flowchart TB
  client["Client (Swagger UI / curl)"]

  subgraph app["Spring Boot app"]
    controllers["Controllers\nSubscriptionController\nPlanController"]
    advice["Exception handling\nExceptionController"]
    services["Service layer\nSubscriptionService (SubscriptionInterface)\nPlanService (PlanInterface)\nHistoryService (HistoryInterface)"]
    dao["DAO layer\nSubscriptionDAO\nPlanDAO\nHistoryDAO"]
    repos["Repository layer\nSubscriptionRepository\nPlanRepository\nHistoryRepository"]
    scheduler["Scheduler\nSubscriptionExpirationScheduler"]
    liquibase["Liquibase\nDB migrations / seed"]
  end

  db[(PostgreSQL)]

  client --> controllers
  controllers --> services
  controllers --> advice

  services --> dao
  dao --> repos
  repos --> db

  scheduler --> services
  liquibase --> db
```

# База данных

Схема управляется Liquibase (см. `src/main/resources/db/changelog/*`).

```mermaid
erDiagram
  SUBSCRIPTION_PLANS {
    UUID id PK
    VARCHAR service_name
    VARCHAR plan_name
    TEXT description
    NUMERIC default_price
    VARCHAR currency
    INT duration_days
    BOOLEAN is_active
    TIMESTAMP created_at
  }

  SUBSCRIPTIONS {
    UUID id PK
    UUID user_id
    UUID plan_id FK
    VARCHAR status
    DATE start_date
    DATE end_date
  }

  SUBSCRIPTION_STATUS_HISTORY {
    UUID id PK
    UUID subscription_id FK
    VARCHAR old_status
    VARCHAR new_status
    TIMESTAMP changed_at
    TEXT reason
  }

  SUBSCRIPTION_PLANS ||--o{ SUBSCRIPTIONS : "plan_id"
  SUBSCRIPTIONS ||--o{ SUBSCRIPTION_STATUS_HISTORY : "subscription_id"
```

# Варианты улучшения системы

- Добавить Bean Validation (`spring-boot-starter-validation`) и аннотации в DTO для валидации входных данных.
- Добавить авторизацию (например, JWT) и ограничение доступа к данным по пользователю, также добавить систему учета пользователей
- Добавить кэширование часто запрашиваемых данных
