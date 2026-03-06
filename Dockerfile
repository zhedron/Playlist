FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY profile_image ./profile_image
COPY --from=build /app/target/*.jar playlist.jar
ENTRYPOINT ["java", "-jar", "playlist.jar"]