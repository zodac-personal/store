# syntax=docker/dockerfile:1.7

# Stage 1: JAR build
FROM eclipse-temurin:26.0.2_10-jdk AS build

WORKDIR /work

COPY gradlew settings.gradle build.gradle VERSION ./
COPY gradle gradle

# Warms the dependency cache in its own layer, invalidated only by the files above.
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon dependencies > /dev/null

COPY src src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon spotlessCheck bootJar -x test \
    && find build/libs -maxdepth 1 -name '*.jar' -not -name '*-plain.jar' -exec cp {} /app.jar \;

# Extract app JAR
RUN java -Djarmode=tools -jar /app.jar extract --destination /extracted

# Stage 2: Extract jdeps modules from the app JAR
FROM build AS jdeps
RUN jdeps --ignore-missing-deps --multi-release 26 --recursive --print-module-deps \
        --class-path '/extracted/lib/*' \
        /extracted/app.jar \
    > /modules.txt

# Stage 3: Build minimal JRE using jlink
FROM eclipse-temurin:26.0.2_10-jdk AS jre
COPY --from=jdeps /modules.txt /modules.txt

# BEGIN UBUNTU PACKAGES
RUN apt-get update && apt-get install -yqq --no-install-recommends \
        binutils="2.46-3ubuntu2" \
    && \
    apt-get autoremove && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
# END UBUNTU PACKAGES

RUN jlink --compress=zip-9 \
        --no-header-files \
        --no-man-pages \
        --strip-debug \
        --add-modules "$(cat /modules.txt),jdk.crypto.ec,jdk.crypto.cryptoki" \
        --output /opt/jdk \
    && strip -p --strip-unneeded /opt/jdk/lib/server/libjvm.so

# Stage 4: Include busybox for binaries
FROM busybox:1.37.0-musl AS shell

# Stage 5: Runtime image
FROM gcr.io/distroless/base-nossl-debian13:nonroot AS runtime

COPY --from=jre /opt/jdk /opt/jdk
ENV JAVA_HOME="/opt/jdk"
ENV PATH="/opt/jdk/bin:${PATH}"

EXPOSE 8080

COPY --from=shell /bin/busybox /bin/wget
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD ["/bin/wget", "--quiet", "--tries=1", "--spider", "http://127.0.0.1:8080/status"]

WORKDIR /app
COPY --from=build /extracted/lib/ ./lib/
COPY --from=build /extracted/app.jar ./

ENTRYPOINT ["/opt/jdk/bin/java", "-jar", "/app/app.jar"]
