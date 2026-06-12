# ExploreWithMe — Этап 1: Spring Cloud инфраструктура

Платформа для публикации городских мероприятий и сбора заявок на участие. Дипломный проект курса «Java-разработчик. Расширенный» (https://practicum.yandex.ru/java-developer-plus/)

---

## Архитектура

На этом этапе монолитное приложение подключено к Spring Cloud инфраструктуре. Все внешние запросы проходят через единую точку входа — Gateway, который маршрутизирует их в `main-service`.

```
Клиент
  │
  ▼
gateway-server  :8080   — маршрутизация всех запросов
  │
  └── main-service        — все бизнес-эндпоинты (события, пользователи,
                            заявки, подборки, категории, локации)

discovery-server  :8761  — Eureka: регистрация и обнаружение сервисов
config-server     :8888  — Spring Cloud Config: централизованная конфигурация
stats-server      :случайный — запись и чтение статистики просмотров
```

**Базы данных:**
- `ewm_main_db` — БД для `main-service` (пользователи, события, заявки, подборки)
- `ewm_stats_db` — БД `stats-server`

---

## Сервисы

| Сервис | Назначение | Порт |
|---|---|---|
| `gateway-server` | Единая точка входа, маршрутизация | 8080 |
| `discovery-server` | Реестр сервисов (Eureka) | 8761 |
| `config-server` | Централизованная конфигурация | 8888 |
| `main-service` | Все бизнес-операции | случайный |
| `stats-server` | Запись и чтение статистики просмотров | случайный |

Бизнес-сервисы используют случайный порт (`server.port: 0`) — адрес обнаруживается через Eureka.

---

## Взаимодействие между сервисами

`main-service` обращается к `stats-server` напрямую через `StatsClient` (Spring WebClient + Eureka discovery):

| Вызывает | Вызывает кого | Эндпоинт | Поведение при недоступности |
|---|---|---|---|
| `main-service` | `stats-server` | `POST /hit` | fire-and-forget, ошибка логируется |
| `main-service` | `stats-server` | `GET /stats` | возвращает `views = 0` |

Отправка хита реализована асинхронно (`subscribe()`), чтобы не блокировать Tomcat-поток. Чтение статистики — синхронное с таймаутом 5 секунд.

---

## Настройки

Все настройки хранятся в `infra/config-server/src/main/resources/config/`:

| Файл | Назначение |
|---|---|
| `gateway-server.yml` | Маршруты Gateway (все пути → `main-service`) |
| `main-service.yml` | Datasource, JPA, идентификатор stats-server |
| `stats-server.yml` / `*-docker.yaml` | Datasource stats-server |
| `user-service.yml` | Datasource user-service (задел для следующего этапа) |

Все чувствительные параметры (пароль БД, порт) задаются через переменные окружения с fallback-значением: `${POSTGRES_PASSWORD:54321}`.

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

- Как работает Service Discovery: сервисы регистрируются по имени, клиенты находят их через Eureka без захардкоженных адресов
- Зачем нужен `bootstrap.yml` и чем он отличается от `application.yml` — он загружается до основного контекста и используется для подключения к Config Server
- Как `${VAR:default}` в Spring позволяет одновременно поддерживать локальный запуск и CI/CD без изменения кода
- Почему синхронный `WebClient.block()` без таймаута опасен под нагрузкой — исчерпывает пул потоков; решение — либо `subscribe()` для fire-and-forget, либо `block(Duration)`
- Что отступы в YAML критичны: `spring.cloud.loadbalancer` и `spring.cloud.gateway.loadbalancer` — это разные свойства с разным поведением
