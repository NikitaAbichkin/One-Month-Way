# Демо: заказ и пополнение баланса

Команды выполнять из папки `marketplace`. Запросы идут через api-gateway (`localhost:8080`).

## Зарегистрировать пользователя

```zsh
curl -s -X POST localhost:8080/users/register -H 'Content-Type: application/json' -d "{\"name\":\"Nikita\",\"email\":\"nikita$(date +%s)@gmail.com\",\"role\":\"BUYER\"}"
```

user-service сохраняет пользователя, payment создаёт ему кошелёк с балансом **1000**, notification запоминает email для писем.
`role`: `BUYER` (покупатель) или `SELLER` (продавец).
Email должен быть уникальным: `$(date +%s)` подставляет текущее время, поэтому каждый запуск даёт новый email. Если отправить уже занятый email — ответ **`409 Conflict`** (пользователь с таким email уже есть).
**Ответ:** `201` и JSON пользователя — из поля `id` берём `userId` для заказа и пополнения.

### Посмотреть пользователя

```zsh
curl -s localhost:8080/users/1
```

## Создать товар и положить на склад

```zsh
curl -s localhost:8080/catalog/products --json '{"name":"Laptop","category":"computers","price":500}'
```

catalog создаёт товар и шлёт событие в `CatalogTopic` — order запоминает цену. Без этого заказ вернёт **`422`** (цена товара неизвестна).

```zsh
curl -s -X PUT localhost:8080/inventory/products/1/stock --json '{"amount":5}'
```

inventory выставляет остаток товара `1` = 5 штук.

## Создать заказ

```zsh
curl -si localhost:8080/orders --json '{"userId":1,"productId":1,"amount":1,"idempotenceKey":"order-1"}'
```

Пользователь `userId` покупает `amount` штук товара `productId`. order создаёт заказ и запускает сагу: inventory резервирует товар → payment списывает деньги → notification шлёт письмо.
**Ответ:** `202 Accepted` + заголовок `Location: /orders/<id>`.
Для каждого нового заказа меняй `idempotenceKey` (`order-2`, `order-3`…), иначе дубль не создастся.

### Статус заказа

```zsh
curl -s localhost:8080/orders/1
```

`Done` — оплачен, `Canceled` — не хватило денег или товара.

## Пополнить баланс

```zsh
curl -s localhost:8080/payments/wallets/1/top-up --json '{"amount":2000}'
```

payment увеличивает баланс кошелька пользователя `1` на `amount`.
**Ответ:** `{"userId":1,"balance":...}`. Сумма ≤ 0 → `400`, нет кошелька → `404`.

### Посмотреть баланс

```zsh
curl -s localhost:8080/payments/wallets/1
```
