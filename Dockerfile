FROM node:22-bookworm-slim AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk AS backend-builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/
COPY --from=frontend-builder /app/frontend/dist ./frontend/dist
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV APP_DATA_ROOT=/app/data
COPY --from=backend-builder /app/target/baby-steps-0.0.1-SNAPSHOT.jar /app/app.jar
RUN mkdir -p /app/data/config /app/data/uploads
VOLUME ["/app/data"]
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
