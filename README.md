# ExploreWithMe — Этап 2: Разбивка на микросервисы

Дипломный проект курса «Java-разработчик. Расширенный» (https://practicum.yandex.ru/java-developer-plus/)

---

## Архитектура

Приложение разбито на независимые микросервисы, каждый со своей схемой БД. Все внешние запросы проходят через единую точку входа — Gateway.

```
Клиент
  │
  ▼
gateway-server  :8080   — маршрутизация по пути
  │
  ├── user-service        — управление пользователями (/admin/users/**)
  ├── event-service       — события, категории, локации
  │                         (/events/**, /categories/**, /locations/**,
  │                          /admin/events/**, /admin/categories/**,
  │                          /admin/locations/**, /users/*/events/**)
  ├── request-service     — заявки на участие
  │                         (/users/*/requests/**, /users/*/events/*/requests/**)
  ├── compilation-service — подборки (/compilations/**, /admin/compilations/**)
  └── stats-server        — статистика просмотров (/hit, /stats/**)

discovery-server  :8761  — Eureka: регистрация и обнаружение сервисов
config-server     :8888  — Spring Cloud Config: централизованная конфигурация
```

**Базы данных:**
- `ewm_main_db` — общая БД для `user-service`, `event-service`, `request-service`, `compilation-service` (каждый работает только со своими таблицами)
- `ewm_stats_db` — БД `stats-server`

---

## Сервисы

| Сервис | Назначение | Порт |
|---|---|---|
| `gateway-server` | Единая точка входа, маршрутизация | 8080 |
| `discovery-server` | Реестр сервисов (Eureka) | 8761 |
| `config-server` | Централизованная конфигурация | 8888 |
| `user-service` | CRUD пользователей | случайный |
| `event-service` | События, категории, локации, статистика | случайный |
| `request-service` | Заявки на участие в событиях | случайный |
| `compilation-service` | Подборки событий | случайный |
| `stats-server` | Запись и чтение статистики просмотров | случайный |

Бизнес-сервисы используют случайный порт (`server.port: 0`) — адрес обнаруживается через Eureka.

---

## Взаимодействие между сервисами

Вызовы между микросервисами происходят через Feign-клиенты с circuit breaker (Resilience4j). При недоступности зависимого сервиса применяется fallback.

| Вызывает | Вызывает кого | Эндпоинт | Fallback |
|---|---|---|---|
| `event-service` | `user-service` | `GET /internal/users/{id}` | `UserShortDto(id, "N/A")` |
| `event-service` | `user-service` | `GET /internal/users?ids=` | пустой список |
| `event-service` | `request-service` | `GET /internal/requests/count?eventIds=` | пустая Map (confirmedRequests = 0) |
| `request-service` | `event-service` | `GET /internal/events/{id}` | исключение (критично) |
| `request-service` | `user-service` | `GET /internal/users/{id}` | `null` |
| `compilation-service` | `event-service` | `GET /internal/events?ids=` | пустой список событий |

---

## Внутренний API (не проксируется через Gateway)

Для межсервисного взаимодействия используются внутренние эндпоинты:

| Сервис | Путь | Описание |
|---|---|---|
| `user-service` | `GET /internal/users/{userId}` | Получить пользователя по ID |
| `user-service` | `GET /internal/users?ids=` | Получить список пользователей по ID |
| `event-service` | `GET /internal/events/{eventId}` | Получить событие по ID (для заявок) |
| `event-service` | `GET /internal/events?ids=` | Получить список событий по ID (для подборок) |
| `request-service` | `GET /internal/requests/count?eventIds=` | Получить количество подтверждённых заявок |

---

## Настройки

Все настройки хранятся в `infra/config-server/src/main/resources/config/`:

| Файл | Назначение |
|---|---|
| `gateway-server.yml` | Маршруты Gateway |
| `user-service.yml` / `*-docker.yaml` | Настройки user-service |
| `event-service.yml` / `*-docker.yaml` | Настройки event-service |
| `request-service.yml` / `*-docker.yaml` | Настройки request-service |
| `compilation-service.yml` / `*-docker.yaml` | Настройки compilation-service |
| `stats-server.yml` / `*-docker.yaml` | Настройки stats-server |

Профиль `docker` активируется через `SPRING_PROFILES_ACTIVE=docker` в `docker-compose.yml` и переопределяет URL базы данных с `localhost` на имя контейнера.

---

## Запуск

```bash
# Сборка всех модулей
mvn package -DskipTests

# Запуск всех сервисов
docker compose up --build
```

После запуска:
- Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761

---

## Спецификации API

- [Основной сервис (ewm-main-service)](ewm-main-service-spec.json)
- [Сервис статистики (ewm-stats-service)](ewm-stats-service-spec.json)

---

## Что изучил в процессе

- Как определять границы микросервисов: разбил монолит на домены — события, заявки, подборки, пользователи
- Как работает Feign + Resilience4j: вместо прямых вызовов — HTTP-клиенты с fallback при отказе зависимого сервиса
- Почему важно избегать N+1 запросов: вместо вызова `getUser(id)` в цикле — один батч-запрос `getUsers(ids)`
- Как проектировать внутренний API: `/internal/**` — эндпоинты только для межсервисного взаимодействия, не проксируются через Gateway
- Что микросервисы, работающие с общей БД, изолируются на уровне таблиц, а не схем — каждый сервис знает только о своих таблицах
