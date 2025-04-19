# Giai đoạn 1: Build ứng dụng
FROM openjdk:21-jdk AS builder

WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng
FROM openjdk:21-jdk

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE ${SERVER_PORT}
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${SERVER_PORT}"]
