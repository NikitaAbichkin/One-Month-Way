# ---- Этап 1: сборка ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Сначала только pom-ы: слой с зависимостями кэшируется, пока pom не менялся
COPY pom.xml .
COPY order/pom.xml order/
COPY inventory/pom.xml inventory/
RUN mvn -B -q -pl inventory -am dependency:go-offline

# Потом код
COPY inventory/src inventory/src
RUN mvn -B -q -pl inventory -am package -DskipTests

# Раскладываем jar по слоям
RUN java -Djarmode=tools -jar inventory/target/inventory22-1.0-SNAPSHOT.jar extract --layers --destination /extracted

# ---- Этап 2: запуск ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# От редко меняющегося к часто меняющемуся
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "inventory22-1.0-SNAPSHOT.jar"]
