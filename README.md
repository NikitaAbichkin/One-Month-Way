# One Month Way

Месяц изучения микросервисной архитектуры на Java / Spring: каждый день — тема, заметка с теорией и практика в коде.  
В конце — итоговый проект [`marketplace`](marketplace), где всё собрано вместе и я попытался использовать все мои знания в одном проекте . Этот месяц получился довольно продуктивный  и я  решил структурировать свои заметки и код который  писал руками :)

В некоторые заметки я прикрепил свои рисунки которые делал по ходу изучения. В заметках я выписывал только самое основное (как обычно коротко не получается ). 
  
**Стек:** Java 25, Spring Boot, PostgreSQL, Kafka, RabbitMQ, gRPC, Spring Cloud Gateway, Resilience4j, Docker / Docker Compose, Kubernetes (kind), Prometheus, Tempo, Grafana.

## По дням

| День | Тема | Код | Заметка |
|---|---|---|---|
| 1 | Spring: `@Transactional`, propagation, откат при исключении | [`day1`](day1) | — |
| 2 | Минусы микросервисов, coupling и cohesion | — | [notes](day2/notes.md) |
| 3 | Распределённый монолит | — | [notes](day3/notes.md) |
| 4 | HTTP-статусы, идемпотентность | [`day4`](day4) | [notes](day4/notes.md) |
| 5 | Сокеты, потоки, thread dump | [`day5`](day5) | [notes](day5/notes.md) |
| 6 | gRPC: контракт `.proto`, deadline, статус-коды | [`day6`](day6) | [notes](day6/notes.md) |
| 7 | gRPC: interceptor, трейлеры, эволюция контракта | [`day6/.../LoggingInterceptor.java`](day6/inventory-service/src/main/java/LoggingInterceptor.java) | [notes](day7/notes.md) |
| 8 | Брокер сообщений: RabbitMQ | [`day8`](day8) | [notes](day8/notes.md) |
| 9 | DLQ, dead lettering | [`day8/notification`](day8/notification) | [notes](day9/notes.md) |
| 10 | Kafka: партиции, ключи, consumer group | [`day10`](day10) | [notes](day10/notes.md) |
| 11 | Kafka: репликация, acks, коммиты (потеря и дубль через `halt(1)`) | [`day11`](day11) | [notes](day11/notes.md) |
| 12 | Kafka consumer: offset, битые записи, DLT | [`day12`](day12) | [notes](day12/notes.md) |
| 13 | Database per service, потеря join'ов | [`day13`](day13) | [notes](day13/notes.md) |
| 14 | Dual write, Transactional Outbox | [`day14`](day14) | [notes](day14/notes.md) |
| 15 | Saga: хореография и оркестрация, компенсации | [`day15`](day15) | [notes](day15/notes.md) |
| 16 | API Gateway, BFF, CORS | [`day16`](day16) | [notes](day16/notes.md) |
| 17 | Service Discovery (Eureka) | [`day17`](day17) | [notes](day17/notes.md) |
| 18 | Circuit Breaker | [`day18`](day18) | [notes](day18/notes.md) |
| 19 | Retry storm, таймауты | [`day19`](day19) | [notes](day19/notes.md) |
| 20 | Docker: контейнеры, namespaces, сеть | [`day20`](day20) | [notes](day20/notes.md) |
| 21 | Kubernetes: кластер, Deployment, Service | [`day21`](day21) | [notes](day21/notes.md) |
| 22 | Kubernetes: ConfigMap, Secret, probes, resources | [`day22`](day22) | [notes](day22/notes.md) |
| 23 | Наблюдаемость: логи, метрики, трейсы | [`day23`](day23) | [notes](day23/notes.md) |

Отдельно: [`proxy-testing`](proxy-testing) — JDK dynamic proxy (как Spring оборачивает бины).

## Итоговый проект: marketplace

Маркетплейс из 7 сервисов: api-gateway, user, catalog, order, inventory, payment, notification.

- оформление заказа — сага-оркестрация (order) через Kafka с компенсациями;
- Transactional Outbox, идемпотентность заказов, у каждого сервиса своя БД;
- один traceId через все сервисы (Micrometer Tracing → Tempo → Grafana), метрики в Prometheus;
- запуск в Docker Compose или в Kubernetes (kind).

Запуск и сценарий: [`marketplace/COMMANDS.md`](marketplace/COMMANDS.md), запросы для демо: [`marketplace/DEMO.md`](marketplace/DEMO.md), манифесты: [`marketplace/k8s`](marketplace/k8s).

### Диаграммы

Use Case, C4 (Level 1 и 2), диаграмма компонентов, Activity (сага оформления заказа) и диаграмма классов — [открыть в полном размере](marketplace/docs/diagrams.png).

[![Диаграммы marketplace](marketplace/docs/diagrams.png)](marketplace/docs/diagrams.png)
