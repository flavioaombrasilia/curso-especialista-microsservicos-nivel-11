#!/bin/sh

set -e

if [ -z "$JAVA_OPTS" ]; then
  JAVA_OPTS="-XX:MinRAMPercentage=10.0 -XX:MaxRAMPercentage=75.0"
fi

echo "Starting Courier-Management with JAVA_OPTS: $JAVA_OPTS"
echo "Port configured: $SERVER_PORT"

exec java $JAVA_OPTS -jar ${JAR_NAME:-app.jar}
