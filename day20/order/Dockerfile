# ---- Этап 1: сборка ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Сначала только pom-ы: слой с зависимостями кэшируется, пока pom не менялся

# копируем пом и родителя ( чтобы были зависимости)
COPY pom.xml .
COPY order/pom.xml order/
# остальные модули нужны, потому что родительский pom.xml перечисляет их все
COPY order-service/pom.xml order-service/
COPY inventory-service/pom.xml inventory-service/
COPY payment-service/pom.xml payment-service/
COPY notification-service/pom.xml notification-service/
RUN mvn -B -q -pl order -am dependency:go-offline

# Делаем код + делаем jar
COPY order/src order/src
RUN mvn -B -q -pl order -am package -DskipTests

# Раскладываем jar по слоям
RUN java -Djarmode=tools -jar order/target/order.jar extract --layers --destination /extracted

#( в образе только джава, без мавена и все скачанных зависимостей, только нужные нам)
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
# От редко меняющегося к часто меняющемуся
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "order.jar"]
