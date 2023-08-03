# Use the official OpenJDK image as a parent image
FROM openjdk:17-jdk-slim-buster

# Set the working directory to /app
WORKDIR /app

# Copy the packaged JAR file into the container at /app
COPY  target/booking-service-0.0.1-SNAPSHOT.jar booking-service.jar

# Expose port 9903
EXPOSE 9003

# Run the JAR file when the container starts
CMD ["java", "-jar", "booking-service.jar"]
