FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -f ./pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY profile_image ./profile_image
COPY --from=build /app/target/*.jar playlist.jar
ENTRYPOINT ["java", "-jar", "playlist.jar"]