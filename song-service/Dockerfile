ARG TARGETPLATFORM=linux/amd64
FROM --platform=${TARGETPLATFORM} maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -Dmaven.test.skip=true

FROM --platform=${TARGETPLATFORM} eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
CMD ["java", "-jar", "/app/app.jar"]
