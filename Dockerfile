FROM eclipse-temurin:25-jre-alpine@sha256:28db6fdf60e38945e43d840c0333aeaec66c15943070104f7586fd3c9d1665b0

ARG JAR_FILE=build/release/cart-decision-tree.jar
ARG VERSION=0.1.0-SNAPSHOT
ARG REVISION=unknown

LABEL org.opencontainers.image.title="cart-decision-tree" \
      org.opencontainers.image.description="Bounded CART fruit-classification service with MVC and MCP interfaces" \
      org.opencontainers.image.source="https://github.com/JonHHH09/cart-decision-tree" \
      org.opencontainers.image.licenses="MIT" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${REVISION}"

RUN apk add --no-cache --upgrade \
      "libexpat=2.8.2-r0" \
      "p11-kit=0.26.2-r0" \
      "p11-kit-trust=0.26.2-r0" \
    && addgroup -S app \
    && adduser -S -G app -h /app app

WORKDIR /app
COPY --chown=app:app ${JAR_FILE} app.jar

USER app
EXPOSE 8081

ENV APP_VERSION=${VERSION} \
    SERVER_PORT=8081 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=12 \
  CMD wget -q -O /dev/null http://127.0.0.1:8081/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
