FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw -DskipTests clean package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
