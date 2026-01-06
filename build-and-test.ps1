# Build and test script for Windows PowerShell

Write-Host "🚀 Building Shoe Store Application for Render Deployment" -ForegroundColor Green

# Clean and build the application
Write-Host "📦 Building application..." -ForegroundColor Yellow
./mvnw.cmd clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}

# Build Docker image
Write-Host "🐳 Building Docker image..." -ForegroundColor Yellow
docker build -t shoestore-app .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed!" -ForegroundColor Red
    exit 1
}

# Test Docker image locally
Write-Host "🧪 Testing Docker image..." -ForegroundColor Yellow
docker run -d -p 8080:8080 --name shoestore-test `
  -e SPRING_PROFILES_ACTIVE=dev `
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/postgres `
  -e SPRING_DATASOURCE_USERNAME=postgres `
  -e SPRING_DATASOURCE_PASSWORD=password `
  shoestore-app

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Application started! Test at http://localhost:8080" -ForegroundColor Green
    Write-Host "🔍 Health check: http://localhost:8080/actuator/health" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To stop the test container:" -ForegroundColor Yellow
    Write-Host "docker stop shoestore-test; docker rm shoestore-test" -ForegroundColor White
} else {
    Write-Host "❌ Failed to start container!" -ForegroundColor Red
}