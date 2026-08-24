FROM eclipse-temurin:21-jre
ARG APP_VERSION=v1
ENV APP_VERSION=${APP_VERSION}
COPY build/libs/hellojava-0.0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
