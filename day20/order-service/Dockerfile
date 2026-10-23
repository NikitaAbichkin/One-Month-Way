# ---- Этап 1: сборка. Контекст сборки — папка day20 ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Родительский pom.xml и pom.xml всех модулей (родитель перечисляет их все)
COPY pom.xml .
COPY order/pom.xml order/
COPY order-service/pom.xml order-service/
COPY inventory-service/pom.xml inventory-service/
COPY payment-service/pom.xml payment-service/
COPY notification-service/pom.xml notification-service/

# Загружаем зависимости (слой кэшируется, пока pom-ы не менялись)
RUN mvn -B -q -pl order-service -am dependency:go-offline

# Копируем код и собираем jar
COPY order-service/src order-service/src
RUN mvn -B -q -pl order-service -am -DskipTests package

# Раскладываем jar по слоям
RUN java -Djarmode=tools -jar order-service/target/order-service.jar extract --layers --destination /extracted

# ---- Этап 2: запуск (только JRE и нужные слои) ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# curl нужен для healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# От редко меняющегося к часто меняющемуся
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "order-service.jar"]
