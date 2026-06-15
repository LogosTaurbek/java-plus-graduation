# ExploreWithMe — Этап 3-1: Рекомендательная система

Дипломный проект курса «Java-разработчик. Расширенный» (https://practicum.yandex.ru/java-developer-plus/)

---

## Архитектура

```
Клиент
  │
  ▼
gateway-server  :8080   — маршрутизация по пути
  │
  ├── user-service        — управление пользователями
  ├── event-service       — события, категории, локации, рекомендации
  ├── request-service     — заявки на участие
  └── compilation-service — подборки событий

discovery-server  :8761  — Eureka: регистрация и обнаружение сервисов
config-server     :8888  — Spring Cloud Config: централизованная конфигурация

Поток действий пользователей (асинхронно через Kafka):

event-service / request-service
  │  gRPC (UserActionProto)
  ▼
collector  ──► Kafka: stats.user-actions.v1
                         │
                    ┌────┴────┐
                    ▼         ▼
               aggregator   analyzer
               (cosine sim)  (PostgreSQL)
                    │         │
                    └────┬────┘
         Kafka: stats.events-similarity.v1
                         │
                    analyzer ◄── gRPC (рекомендации)
                                    ▲
                              event-service
```

---

## Сервисы

| Сервис | Назначение | Порт |
|---|---|---|
| `gateway-server` | Единая точка входа, маршрутизация | 8080 |
| `discovery-server` | Реестр сервисов (Eureka) | 8761 |
| `config-server` | Централизованная конфигурация | 8888 |
| `user-service` | CRUD пользователей | случайный |
| `event-service` | События, рекомендации, лайки | случайный |
| `request-service` | Заявки на участие | случайный |
| `compilation-service` | Подборки событий | случайный |
| `collector` | Приём действий пользователей (gRPC → Kafka) | случайный |
| `aggregator` | Вычисление косинусного сходства событий | — |
| `analyzer` | Хранение статистики + gRPC рекомендации | случайный |

---

## Рекомендательная система

Используется item-based collaborative filtering с косинусным сходством.

**Веса действий:**
| Действие | Вес |
|---|---|
| VIEW (просмотр) | 0.4 |
| REGISTER (регистрация) | 0.8 |
| LIKE (лайк) | 1.0 |

Берётся максимальный вес пользователя по событию (не суммируется).

**Формула сходства событий A и B:**

```
score(A,B) = Σ min(w(u,A), w(u,B))  /  √(Σ w(u,A)²) × √(Σ w(u,B)²)
```

**Новые эндпоинты event-service:**
| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/events` | Публичный список событий (сортировка по рейтингу вместо просмотров) |
| `GET` | `/events/{id}` | Событие по ID + фиксация просмотра (ACTION_VIEW) |
| `GET` | `/events/recommendations` | Персональные рекомендации для пользователя |
| `GET` | `/events/{id}/similar` | Похожие события |
| `PUT` | `/events/{id}/like` | Поставить лайк событию (ACTION_LIKE) |

Заголовок `X-EWM-USER-ID` передаётся от клиента через Gateway для идентификации пользователя.

---

## Взаимодействие между сервисами

**HTTP (Feign + Resilience4j):**
| Вызывает | Кого | Эндпоинт |
|---|---|---|
| `event-service` | `user-service` | `GET /internal/users/{id}`, `GET /internal/users?ids=` |
| `event-service` | `request-service` | `GET /internal/requests/count?eventIds=`, `GET /internal/requests/user/{userId}` |
| `request-service` | `event-service` | `GET /internal/events/{id}` |
| `request-service` | `user-service` | `GET /internal/users/{id}` |
| `compilation-service` | `event-service` | `GET /internal/events?ids=` |

**gRPC:**
| Вызывает | Кого | Метод |
|---|---|---|
| `event-service` | `collector` | `CollectUserAction` (VIEW, LIKE) |
| `request-service` | `collector` | `CollectUserAction` (REGISTER) |
| `event-service` | `analyzer` | `GetRecommendationsForUser`, `GetSimilarEvents`, `GetInteractionsCount` |

---

## Kafka топики

| Топик | Producer | Consumer |
|---|---|---|
| `stats.user-actions.v1` | `collector` | `aggregator`, `analyzer` |
| `stats.events-similarity.v1` | `aggregator` | `analyzer` |

Сообщения сериализованы в Avro (бинарный формат без schema registry).

---

## Запуск

```bash
mvn package -DskipTests
docker compose up --build
```

После запуска:
- Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761

---

## Что изучил в процессе

- Как работает item-based collaborative filtering и косинусное сходство событий
- Инкрементальное обновление similarity scores без перепересчёта всей матрицы
- Apache Kafka с Avro-сериализацией без schema registry — ручные сериализаторы
- gRPC + Protocol Buffers для низколатентного межсервисного взаимодействия
- Серверный стриминг gRPC (server-side streaming) для пагинации рекомендаций
- Upsert через нативный SQL с `ON CONFLICT DO UPDATE` и `GREATEST()` для идемпотентных записей
