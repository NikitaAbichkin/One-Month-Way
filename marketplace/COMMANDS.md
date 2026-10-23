# Команды для macOS (zsh)

Все команды выполняются из папки `marketplace`:

```zsh
cd ~/путь/к/One-Month-Way/marketplace
```

Нужно: Docker Desktop (запущен), `kind`, `kubectl` (`brew install kind kubectl`).

---

## 1. Запуск в Docker Compose

```zsh
# собрать образы и поднять всё, дождаться healthy (первая сборка 5-10 минут)
docker compose up -d --build --wait

docker compose ps                       # статус контейнеров
docker compose logs -f order            # логи одного сервиса
docker compose logs notification --since 5m | grep "@gmail.com"   # письма
docker compose down                     # остановить (данные БД сохраняются)
docker compose down -v                  # остановить и удалить данные БД
```

Адреса:

| Что | Адрес |
|---|---|
| API (api-gateway) | http://localhost:8080 |
| **Kafka UI** (топики, сообщения) | http://localhost:8090 |
| Grafana | http://localhost:3000 (источники: `http://prometheus:9090`, `http://tempo:3200`) |
| Prometheus | http://localhost:9090 |

---

## 2. Запуск в Kubernetes (kind)

### 2.1 Собрать образы

Образы собирает compose, они называются `marketplace-<сервис>:latest`:

```zsh
docker compose build
docker images | grep marketplace
```

### 2.2 Создать кластер

```zsh
kind create cluster --name marketplace --config kind-config.yaml
kubectl cluster-info --context kind-marketplace
```

### 2.3 Подгрузить образы в кластер

kind не видит образы из Docker Desktop, их нужно загрузить в ноды:

```zsh
kind load docker-image \
  marketplace-api-gateway:latest \
  marketplace-user:latest \
  marketplace-catalog:latest \
  marketplace-order:latest \
  marketplace-inventory:latest \
  marketplace-payment:latest \
  marketplace-notification:latest \
  --name marketplace
```

Проверить, что образы на ноде:

```zsh
docker exec marketplace-worker crictl images | grep marketplace
```

После пересборки образа загрузи его заново и перезапусти деплоймент:

```zsh
docker compose build order
kind load docker-image marketplace-order:latest --name marketplace
kubectl rollout restart deployment/order
```

### 2.4 Применить манифесты и посмотреть поды

```zsh
kubectl apply -f k8s

kubectl get pods                 # список подов (ждем Running и READY 1/1)
kubectl get pods -w              # то же, с обновлением в реальном времени (выход: Ctrl+C)
kubectl get svc                  # сервисы
kubectl get pods -o wide         # на какой ноде каждый под
```

Если под не стартует:

```zsh
kubectl describe pod <имя-пода>          # события: образ не найден, проба не прошла
kubectl logs <имя-пода>                  # логи
kubectl logs deploy/order -f             # логи деплоймента
kubectl logs <имя-пода> -c wait-for-deps # init-контейнер: чего ждет
```

Письма в кластере:

```zsh
kubectl logs deploy/notification | grep "@gmail.com"
```

### 2.5 Kafka UI в кластере

```zsh
kubectl port-forward svc/kafka-ui 8090:8080
# открыть http://localhost:8090 (терминал держать открытым)
```

### 2.6 Удалить всё

```zsh
kubectl delete -f k8s        # убрать приложения
kind delete cluster --name marketplace   # удалить кластер целиком
```

API в кластере доступен на http://localhost:8080 (NodePort 30080 проброшен в `kind-config.yaml`).
В кластере нет Tempo/Prometheus/Grafana: они остаются в compose.

---

## 3. Сценарий для защиты (curl, zsh)

Все запросы идут через api-gateway.

```zsh
GW=http://localhost:8080
SUF=$(date +%H%M%S)
H='Content-Type: application/json'

# 1) регистрация продавца и покупателя (payment создает кошелек, notification запоминает email)
curl -s -X POST $GW/users/register -H $H -d "{\"name\":\"Seller\",\"email\":\"seller$SUF@gmail.com\",\"role\":\"SELLER\"}"
curl -s -X POST $GW/users/register -H $H -d "{\"name\":\"Nikita\",\"email\":\"nikita$SUF@gmail.com\",\"role\":\"BUYER\"}"
# в ответах поле id: у первого пользователя id=1, у второго id=2 (если база чистая)

# 2) продавец создает товар (цена уходит в order через CatalogTopic) и выставляет остаток
curl -s -X POST $GW/catalog/products -H $H -d '{"name":"Laptop","category":"computers","price":500}'
curl -s -X PUT $GW/inventory/products/1/stock -H $H -d '{"amount":5}'
sleep 4

# 3) успешный заказ (кошелек 1000, цена 500); id заказа в заголовке Location
curl -si -X POST $GW/orders -H $H -d "{\"userId\":2,\"productId\":1,\"amount\":1,\"idempotenceKey\":\"ok-$SUF\"}" | grep -i "^HTTP\|^location"
sleep 8
curl -s $GW/orders/1

# 4) webhook доставки
curl -s -X POST $GW/webhooks/delivery -H $H -d '{"orderId":1,"status":"DELIVERED"}'
sleep 4
curl -s $GW/orders/1

# 5) заказ с отказом по деньгам (3 x 500 > 1000): сага откатывается
curl -si -X POST $GW/orders -H $H -d "{\"userId\":2,\"productId\":1,\"amount\":3,\"idempotenceKey\":\"fail-$SUF\"}" | grep -i "^HTTP\|^location"
sleep 8
curl -s $GW/orders/2

# 6) письма
docker compose logs notification --since 3m | grep "@gmail.com"
```

Что смотреть в Kafka UI (http://localhost:8090) → Topics: `CatalogTopic`, `UserTopic`,
`InventoryTopic`, `PaymentTopic`, `OrderTopic`, `NotificationTopic` (вкладка Messages).
Топики `*-dlt` должны остаться пустыми: туда попадают сообщения, которые не удалось обработать.

---

## 4. Полезное

```zsh
# сбросить БД одного сервиса в compose
docker compose rm -sf order-db && docker volume rm marketplace_order-data && docker compose up -d order-db

# заглянуть в БД
docker compose exec order-db psql -U app -d order_db -c "select id, status from orders order by id desc limit 5"
docker compose exec notification-db psql -U app -d notification_db -c "select order_id, type, text from notifications"
```
