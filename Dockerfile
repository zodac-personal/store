# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:26.0.2_10-jdk AS build

WORKDIR /work

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle

# Warms the dependency cache in its own layer, invalidated only by the files above.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies > /dev/null

COPY src src

# Tests need a live database (see .claude/CLAUDE.md); CI runs them separately, so the
# image build only needs a correct, formatted compile.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon spotlessCheck bootJar -x test \
    && find build/libs -maxdepth 1 -name '*.jar' -not -name '*-plain.jar' -exec cp {} /app.jar \;

FROM eclipse-temurin:26.0.2_10-jre AS runtime

RUN groupadd --system store \
    && useradd --system --gid store --no-create-home store

COPY --from=build --chown=store:store /app.jar /app/app.jar

USER store
WORKDIR /app
EXPOSE 8080

# No actuator dependency is present, so the health check is a plain TCP probe against the
# HTTP port rather than a /actuator/health call.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
