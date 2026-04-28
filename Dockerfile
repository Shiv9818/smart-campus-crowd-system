FROM openjdk:17-jdk-slim

WORKDIR /app

COPY backend ./backend

WORKDIR /app/backend

RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/*.jar"]