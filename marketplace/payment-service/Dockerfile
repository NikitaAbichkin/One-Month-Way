# ---- Этап 1: сборка. Контекст сборки — папка marketplace ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Родительский pom.xml и pom.xml всех модулей (родитель перечисляет их все)
COPY pom.xml .
COPY order-service/pom.xml order-service/
COPY inventory-service/pom.xml inventory-service/
COPY payment-service/pom.xml payment-service/
COPY catalog-service/pom.xml catalog-service/
COPY user-service/pom.xml user-service/
COPY notification-service/pom.xml notification-service/
COPY api-gateway/pom.xml api-gateway/

# Загружаем зависимости (слой кэшируется, пока pom-ы не менялись)
RUN mvn -B -q -pl payment-service -am dependency:go-offline

# Копируем код и собираем jar
COPY payment-service/src payment-service/src
RUN mvn -B -q -pl payment-service -am -DskipTests package

# Раскладываем jar по слоям
RUN java -Djarmode=tools -jar payment-service/target/payment-service.jar extract --layers --destination /extracted

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

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "payment-service.jar"]
