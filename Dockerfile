# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Copy Gradle build and wrapper files first for dependency-layer cache reuse.
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts /workspace/
COPY gradle /workspace/gradle

RUN --mount=type=cache,target=/root/.gradle \
	set -eux; \
	chmod +x gradlew || true; \
	./gradlew --no-daemon dependencies

# Copy the remaining project files.
COPY . .

RUN --mount=type=cache,target=/root/.m2 \
	--mount=type=cache,target=/root/.gradle \
	set -eux; \
	chmod +x gradlew || true; \
	if [ -f mvnw ]; then chmod +x mvnw; fi; \
	if [ -f pom.xml ]; then \
		if [ -f mvnw ]; then \
			./mvnw -q -DskipTests package; \
		else \
			mvn -q -DskipTests package; \
		fi; \
		ARTIFACT="$(find target -maxdepth 1 -type f -name '*.jar' ! -name 'original-*.jar' | head -n 1)"; \
	elif [ -f build.gradle ] || [ -f build.gradle.kts ]; then \
		if [ -f gradlew ]; then \
			./gradlew --no-daemon bootJar; \
		else \
			gradle --no-daemon bootJar; \
		fi; \
		ARTIFACT="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)"; \
	else \
		echo "No Maven (pom.xml) or Gradle (build.gradle[.kts]) build file detected." >&2; \
		exit 1; \
	fi; \
	test -n "$ARTIFACT"; \
	cp "$ARTIFACT" /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN set -eux; \
	addgroup -S spring && adduser -S -G spring spring; \
	apk upgrade --no-cache; \
	apk add --no-cache wget

LABEL org.opencontainers.image.title="narada" \
	org.opencontainers.image.description="Kotlin Spring Boot service" \
	org.opencontainers.image.licenses="Proprietary" \
	org.opencontainers.image.source="https://github.com/whoash/narada"

COPY --from=builder --chown=spring:spring /workspace/app.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-XX:+UseG1GC -XX:InitialRAMPercentage=10.0 -XX:MaxRAMPercentage=60.0 -XX:MaxGCPauseMillis=200 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
	CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

USER spring

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar /app/app.jar"]
