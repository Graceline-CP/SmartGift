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
COPY --from=build /app/build/install/smartgift/app

# Replace 'your-app-name' with your actual app name in build.gradle.kts

# Expose port (same as in your Ktor application.conf)
EXPOSE 8080

# Run the app
CMD ["./bin/smartgift"]
