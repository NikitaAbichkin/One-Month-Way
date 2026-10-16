# День 8 — RabbitMQ

- contract-first: openapi.yaml → генерация DTO и интерфейса плагином → контроллер реализует сгенерированный интерфейс;
- producer: order публикует OrderCreated в topic exchange с routing key, JSON-конвертер, messageId как UUID;
- consumer: notification-queue с x-dead-letter-exchange, fanout dlx, notification-dlq, два binding'а;
- ручной ack: basicAck после успеха, basicNack при ошибке;
- разделение ошибок: IOException → requeue=true (повтор), IllegalArgumentException → requeue=false (в DLQ).
