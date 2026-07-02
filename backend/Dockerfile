FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# -Xmx256m: caps heap at half of Render's free-tier 512MB, leaving room for metaspace,
#   thread stacks and other off-heap/native memory so the JVM doesn't get OOM-killed.
# -Xss256k: smaller per-thread stack than the 1MB default; this app has no deep
#   recursion, so it's safe and saves memory as concurrent threads grow.
# -XX:MaxMetaspaceSize=96m: bounds class-metadata memory, a common cause of small-
#   container OOMs.
# -XX:+UseSerialGC: the free tier is single-core; serial GC has lower memory/CPU
#   overhead than the default G1 for a small heap on one core.
ENTRYPOINT ["java", "-Xmx256m", "-Xss256k", "-XX:MaxMetaspaceSize=96m", "-XX:+UseSerialGC", "-jar", "app.jar", "--spring.profiles.active=render"]
