#!/bin/bash

# Build and test script for local development

echo "🚀 Building Shoe Store Application for Render Deployment"

# Clean and build the application
echo "📦 Building application..."
./mvnw clean package -DskipTests

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t shoestore-app .

# Test Docker image locally
echo "🧪 Testing Docker image..."
docker run -d -p 8080:8080 --name shoestore-test \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/postgres \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  shoestore-app

echo "✅ Application started! Test at http://localhost:8080"
echo "🔍 Health check: http://localhost:8080/actuator/health"
echo ""
echo "To stop the test container:"
echo "docker stop shoestore-test && docker rm shoestore-test"