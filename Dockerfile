# Use OpenJDK 17 image
FROM gradle:8.7.0-jdk17 AS build

# Copy project files
COPY . /app

WORKDIR /app

# Build the application
RUN gradle installDist

# Use a lightweight JRE image to run the app
FROM openjdk:17-slim
WORKDIR /app

# Copy the built app from the previous stage
COPY --from=build /app/build/install/smartgift/app /app

# Expose port
EXPOSE 8080

# Run the app
CMD ["./bin/smartgift"]
