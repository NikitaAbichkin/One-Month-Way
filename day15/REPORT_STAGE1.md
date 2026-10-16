# День 15. Сага с оркестратором и transactional outbox. Этап 1: успешный путь

## 1. Архитектура

Maven-модуль `day15` содержит 4 Spring Boot сервиса (Boot 4.1.1, Java 25) и **один общий Kafka-брокер**. У каждого сервиса своя Postgres-база.

| Сервис | Порт | База | Топик (свой) |
|---|---|---|---|
| order (оркестратор) | 8081 | order_db :5451 | `OrderTopic` |
| inventory | 8082 | inventory_db :5452 | `InventoryTopic` |
| payment | 8083 | payment_db :5453 | `PaymentTopic` |
| notification | 8084 | notification_db :5454 | `NotificationTopic` (только каркас) |

Доступ к БД идёт через `JdbcClient`, без JPA. Kafka-топики создаются `NewTopic`-бинами, по 3 партиции. Сообщения — JSON-строки, на приёме их превращает в DTO бин `StringJacksonJsonMessageConverter`.

## 2. Схема таблиц (главное)

- **order_db**:
  - `orders(id, user_id, product_id, price, amount, status default 'Created', idempotence_key UNIQUE)`. Цена фиксируется снимком из `products`.
  - `saga_orders(order_id, step, deadline)`.
  - `outbox(id, order_id, message_key, topic, message, send_at)`.
- **inventory_db**:
  - `products(product_id, name, price, amount)`. Начальные данные: Phone 100×10, Laptop 500×5, Tablet 300×0.
  - `inventory_order(id, order_id, product_id, amount, idempotence_key UNIQUE, status)`.
  - `outbox`.
- **payment_db**:
  - `payments(id, user_id, order_id, total_price, idempotence_key UNIQUE, status)`.
  - `balance(user_id, balance)`. Начальные данные: user1=1000, user2=50.
  - `outbox`.

## 3. Поток успешного заказа

```
POST /orders ─► [Order tx] orders + saga_orders(step=INVENTORY) + outbox(INVENTORY_RESERVE)
                     │ (outbox-poller)
                     ▼ InventoryTopic
        [Inventory tx] inventory_order + списание products + outbox(INVENTORY_RESERVED)
                     │ (outbox-poller)
                     ▼ OrderTopic
        [Order tx] saga step INVENTORY→PAYMENT + outbox(PAYMENT_CREATE)
                     │
                     ▼ PaymentTopic
        [Payment tx] payments + списание balance + outbox(PAYMENT_SUCCEEDED)
                     │
                     ▼ OrderTopic
        [Order tx] saga step PAYMENT→DONE, orders.status='Done'
```

## 4. Ключевые решения

**Transactional outbox.** Ни один сервис не отправляет в Kafka напрямую из обработчика. Он пишет сообщение в `outbox` в той же транзакции, что и бизнес-изменение. Отправляет отдельный `@Scheduled(fixedDelay=1000) @Transactional` поллер. Он делает `select ... where send_at is null order by id FOR UPDATE SKIP LOCKED`, отправляет через `KafkaTemplate`, ждёт `CompletableFuture.allOf(...).join()` и ставит `send_at = now()`. Так нет двойной записи БД + Kafka, а `SKIP LOCKED` позволяет запускать несколько экземпляров. Outbox есть в Order, Inventory и Payment.

**Идемпотентность.**
- Внешняя: клиент сам присылает `idempotenceKey`. Заказ создаётся через `insert ... on conflict (idempotence_key) do nothing returning id`. Если id не вернулся, это повтор, и мы выходим.
- Внутренняя: у каждого шага свой ключ (`id+"Creating"`, `id+"paying"`, `id+"compensating"`). Потребители первым делом делают `insert ... on conflict do nothing`. Если вставлено 0 строк, сообщение дублирующее, и мы выходим. Только потом идёт бизнес-изменение.

**Защита шага саги.** `update saga_orders set step=:step where order_id=:orderId and step=:from`. Если обновлено 0 строк, сообщение дубль или запоздалое, и мы выходим. Так порядок шагов гарантируется условием в SQL. Для шага `PAYMENT` ещё ставится `deadline = now() + 30 seconds`.

**Ключ сообщения.** Ключ равен `orderId`, поэтому все сообщения одного заказа идут в одну партицию, и порядок сохраняется. Один топик на сервис, команды различаются полем `statusMessage` (enum) в JSON.

**Условное списание.** Склад: `update products set amount = amount - :amount where product_id=:id and amount >= :amount`. Оплата: `update balance set balance = balance - :sum where user_id=:id and balance >= :sum`. Если обновлено 0 строк, значит отказ (`INVENTORY_REJECTED`, `PAYMENT_FAILED`).

**Цена.** Order при создании заказа запрашивает цену синхронно: `GET http://localhost:8082/products/{id}/price` (`RestClient`). Цена фиксируется в `orders.price`. Сумма платежа = `price × amount` (`BigDecimal`).

## 5. Код основных переходов

**Order: создание заказа** (`OrderService.OrderCreate`, `@Transactional`)
```java
BigDecimal price = inventoryClient.get().uri("/products/{productId}/price", productId).retrieve().body(BigDecimal.class);
Optional<Long> created = jdbcClient.sql("""
  insert into orders (user_id, product_id, amount, price, idempotence_key)
  values (:userId,:productId,:amount,:price,:idempotenceKey)
  on conflict (idempotence_key) do nothing returning id""")....query(Long.class).optional();
if (created.isEmpty()) return;
// insert saga_orders (step=INVENTORY, deadline=now()+30s)
// insert outbox: topic=InventoryTopic, INVENTORY_RESERVE, key=orderId
```

**Order: оркестратор** (`Listener.topicListner`, `@KafkaListener(OrderTopic) @Transactional`)
- `INVENTORY_RESERVED`: `saga step INVENTORY→PAYMENT` (+deadline). Если 0 строк, выходим. Затем outbox `PAYMENT_CREATE` с `totalPrice = price×amount` в `PaymentTopic`.
- `PAYMENT_SUCCEEDED`: `saga step PAYMENT→DONE`, затем `orders.status = 'Done'`.
- `INVENTORY_REJECTED`: `INVENTORY→CANCELED`, `status = 'Canceled'` (этап 2).
- `PAYMENT_FAILED`: `PAYMENT→CANCELED`, `status = 'Canceled'`, outbox `INVENTORY_COMPENSATE` (этап 2).

**Inventory** (`INVENTORY_RESERVE`)
1. `insert inventory_order ... on conflict do nothing`. Если 0 строк, это дубль, выходим.
2. `update products set amount = amount - :amount where ... and amount >= :amount`.
3. Если 0 строк: outbox `INVENTORY_REJECTED`, статус `inventory_order = INVENTORY_REJECTED`, выходим.
4. Иначе outbox `INVENTORY_RESERVED` в `OrderTopic`.

**Payment** (`PAYMENT_CREATE`)
1. `insert payments (status DEDUCTED) ... on conflict do nothing`. Если 0 строк, выходим.
2. `update balance set balance = balance - :sum where user_id=:id and balance >= :sum`.
3. Если 0 строк: outbox `PAYMENT_FAILED`, `payments.status = 'FAILED'`, выходим.
4. Иначе outbox `PAYMENT_SUCCEEDED` в `OrderTopic`.

## 6. Статусы и отличия от задания

| В задании | В реализации |
|---|---|
| `RESERVING` | `saga_orders.step = INVENTORY` |
| `PAYING` | `saga_orders.step = PAYMENT` |
| `DONE` | `saga_orders.step = DONE` |
| заказ `CONFIRMED` | `orders.status = 'Done'` |
| (прочее) | `Created` при создании, `Canceled` при отказе, `step = CANCELED` |

Статусы разных уровней:
- `orders.status`: `Created / Done / Canceled`.
- `saga_orders.step`: `INVENTORY / PAYMENT / DONE / CANCELED`. Уведомление не является шагом саги: заказ не ждёт письма.
- `inventory_order.status`: `INVENTORY_RESERVED / INVENTORY_REJECTED / INVENTORY_COMPENSATED`.
- `payments.status`: `DEDUCTED / FAILED / REFUNDED`.

Отступления от задания:
- Названия статусов другие (см. таблицу), логика переходов та же.
- Клиент присылает `idempotenceKey` сам, а не получает его от сервера.
- Один Kafka-топик на сервис. Команды и ответы различаются полем `statusMessage`, а не отдельными топиками.
- Таймаут саги (job по `deadline < now()`) и отправка уведомлений (в плане: запись в outbox в той же транзакции, что переход в `DONE`/`CANCELED`, без отдельного шага саги) пока не реализованы. Колонка `deadline` заполняется, но ничто её не читает.
- Notification-сервис пока только каркас (приложение, топик, схема).
- Ответ `POST /orders` — `202 Accepted` без тела. Эндпоинта чтения статуса нет, статус смотрю в БД.

## 7. Результат прогона (заказ №2: `userId=1, productId=1, amount=2`)

Запрос:
```
POST http://localhost:8081/orders
{"userId":1,"productId":1,"amount":2,"idempotenceKey":"test-2"}  → 202
```

Итоговое состояние таблиц:

| Таблица | Данные |
|---|---|
| `orders` | id=2, статус **Done**, ключ `test-2` |
| `saga_orders` | order_id=2, step **DONE** |
| `inventory_order` | order_id=2, amount=2, ключ `2Creating`, статус **INVENTORY_RESERVED** |
| `products` | Phone: **6**, Laptop 5, Tablet 0 |
| `payments` | order_id=2, total_price **200.00**, ключ `2paying`, статус **DEDUCTED** |
| `balance` | user1 **800.00**, user2 50.00 |

Цепочка статусов: `INVENTORY` (RESERVING) → `PAYMENT` (PAYING) → `DONE`, заказ `Done`. Сообщения пошли через outbox: Order → Inventory → Order → Payment → Order.

В базах также есть заказ №1, который завис на шаге `INVENTORY`. Это результат первой попытки: в Order-слушателе `select` не включал колонку `idempotence_key`, `findOrder` падал, и сообщение пропускалось после 10 повторов. После правки запроса поток работает. Остаток Phone = 6, потому что заказ №1 зарезервировал 2 штуки, а заказ №2 ещё 2.

Если нужны чистые данные без заказа №1, пересоздайте базы: `docker compose down -v`, затем `up -d`, перезапуск сервисов и один новый заказ.
