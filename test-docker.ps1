Write-Host "Building Docker image..." -ForegroundColor Cyan
docker build -t amg-core:test .

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Docker image built successfully" -ForegroundColor Green
    
    Write-Host "Starting application with docker-compose..." -ForegroundColor Cyan
    docker-compose up -d
    
    Write-Host "Waiting for application to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    Write-Host "`nTesting health endpoint..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri http://localhost:8080/healthz -UseBasicParsing | Select-Object -ExpandProperty Content
    
    Write-Host "`nTesting H2 console availability..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri http://localhost:8080/h2-console -UseBasicParsing
        Write-Host "H2 Console status: $($response.StatusCode)" -ForegroundColor Green
    } catch {
        Write-Host "H2 Console check failed: $_" -ForegroundColor Yellow
    }
    
    Write-Host "`nTesting Users API..." -ForegroundColor Cyan
    Write-Host "Creating a user..." -ForegroundColor Yellow
    $userBody = @{
        name = "Test User"
        email = "test@example.com"
        address = "123 Test St"
    } | ConvertTo-Json
    
    try {
        $userResponse = Invoke-RestMethod -Uri http://localhost:8080/api/users -Method POST -Body $userBody -ContentType "application/json"
        Write-Host "Created user:" -ForegroundColor Green
        $userResponse | ConvertTo-Json
    } catch {
        Write-Host "Error creating user: $_" -ForegroundColor Red
    }
    
    Write-Host "`nGetting all users..." -ForegroundColor Yellow
    try {
        $users = Invoke-RestMethod -Uri http://localhost:8080/api/users -Method GET
        Write-Host "Users:" -ForegroundColor Green
        $users | ConvertTo-Json
    } catch {
        Write-Host "Error getting users: $_" -ForegroundColor Red
    }
    
    Write-Host "`nTesting Products API..." -ForegroundColor Cyan
    Write-Host "Creating a product..." -ForegroundColor Yellow
    $productBody = @{
        name = "Test Product"
        description = "A test product"
        price = 99.99
        stock = 10
    } | ConvertTo-Json
    
    try {
        $productResponse = Invoke-RestMethod -Uri http://localhost:8080/api/products -Method POST -Body $productBody -ContentType "application/json"
        Write-Host "Created product:" -ForegroundColor Green
        $productResponse | ConvertTo-Json
    } catch {
        Write-Host "Error creating product: $_" -ForegroundColor Red
    }
    
    Write-Host "`nGetting all products..." -ForegroundColor Yellow
    try {
        $products = Invoke-RestMethod -Uri http://localhost:8080/api/products -Method GET
        Write-Host "Products:" -ForegroundColor Green
        $products | ConvertTo-Json
    } catch {
        Write-Host "Error getting products: $_" -ForegroundColor Red
    }
    
    Write-Host "`n✓ All tests completed!" -ForegroundColor Green
    Write-Host "To stop the application, run: docker-compose down" -ForegroundColor Cyan
} else {
    Write-Host "✗ Docker build failed" -ForegroundColor Red
    exit 1
}

