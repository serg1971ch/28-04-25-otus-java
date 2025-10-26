FROM gradle:7.5.1-jdk17 AS build
WORKDIR /app

RUN gradle dependencies --no-daemon
COPY src /app/src

RUN gradle build --no-daemon -x test

# Use a lightweight OpenJDK image to run the application
FROM openjdk:17-jdk-slim
COPY --from=build /app/build/libs/*.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]