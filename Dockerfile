FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY backend ./backend

WORKDIR /app/backend

RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/*.jar"]