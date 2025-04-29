FROM eclipse-temurin:21-jdk as build

WORKDIR /app

COPY . .

RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.4-bin.zip && \
    unzip gradle-8.4-bin.zip && \
    rm gradle-8.4-bin.zip

ENV PATH="/app/gradle-8.4/bin:${PATH}"

RUN gradle build --no-daemon -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
