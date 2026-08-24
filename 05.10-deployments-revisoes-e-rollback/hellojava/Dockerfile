FROM eclipse-temurin:21-jre
ARG APP_VERSION=v1
ENV APP_VERSION=${APP_VERSION}
COPY build/libs/hellojava.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
