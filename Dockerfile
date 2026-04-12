# =========================
# Stage 1: Build
# =========================
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xms128m", "-Xmx320m", \
  "-Xss256k", \
  "-XX:+UseG1GC", \
  "-XX:MaxMetaspaceSize=96m", \
  "-XX:ReservedCodeCacheSize=64m", \
  "-XX:+UseContainerSupport", \
  "-XX:+OptimizeStringConcat", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]