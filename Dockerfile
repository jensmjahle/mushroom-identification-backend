# syntax=docker/dockerfile:1

# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Resolve dependencies before copying sources, so a code change does not
# invalidate the dependency layer
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre

# curl is only here to serve the container healthcheck
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Images are written to /app/uploads, which is mounted as a volume in production.
# The app user must own it, since the process does not run as root.
RUN useradd --system --uid 10001 --shell /usr/sbin/nologin app \
 && mkdir -p /app/uploads \
 && chown -R app:app /app

COPY --from=build --chown=app:app /build/target/*.jar /app/app.jar
COPY --chown=app:app docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080

# Let the JVM size its heap from the container memory limit rather than the host's
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Deliberately still root here (no USER directive): /app/uploads is a bind
# mount in production, and a fresh/restored host directory won't carry the
# ownership baked into the image above. entrypoint.sh fixes that mount
# point's ownership, then drops to the unprivileged app user via setpriv
# before ever running application code.
ENTRYPOINT ["/app/entrypoint.sh"]
