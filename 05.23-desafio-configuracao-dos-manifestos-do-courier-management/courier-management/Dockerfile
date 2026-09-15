FROM eclipse-temurin:21-jre-alpine-3.23

ENV JAR_NAME=courier-management.jar \
    SERVER_PORT=8081 \
    TZ=America/Sao_Paulo

RUN addgroup -S spring && adduser -S spring -G spring && \
    apk update && apk upgrade --no-cache && \
    apk add --no-cache wget tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY build/libs/*.jar $JAR_NAME

COPY --chmod=777 docker/docker-entrypoint.sh .

USER spring

HEALTHCHECK --interval=15s --timeout=5s --start-period=15s --retries=3 \
    CMD wget -q -O - http://localhost:$SERVER_PORT/actuator/health | grep -i UP || exit 1

EXPOSE 8081

ENTRYPOINT ["./docker-entrypoint.sh"]
