#!/bin/bash

echo "Building Docker image..."
docker build -t amg-core:test .

if [ $? -eq 0 ]; then
    echo "✓ Docker image built successfully"
    
    echo "Starting application with docker-compose..."
    docker-compose up -d
    
    echo "Waiting for application to start..."
    sleep 10
    
    echo "Testing health endpoint..."
    curl -s http://localhost:8080/healthz
    
    echo -e "\n\nTesting H2 console availability..."
    curl -s -o /dev/null -w "H2 Console status: %{http_code}\n" http://localhost:8080/h2-console
    
    echo -e "\nTesting Users API..."
    echo "Creating a user..."
    curl -s -X POST http://localhost:8080/api/users \
        -H "Content-Type: application/json" \
        -d '{"name":"Test User","email":"test@example.com","address":"123 Test St"}' | jq .
    
    echo -e "\nGetting all users..."
    curl -s http://localhost:8080/api/users | jq .
    
    echo -e "\nTesting Products API..."
    echo "Creating a product..."
    curl -s -X POST http://localhost:8080/api/products \
        -H "Content-Type: application/json" \
        -d '{"name":"Test Product","description":"A test product","price":99.99,"stock":10}' | jq .
    
    echo -e "\nGetting all products..."
    curl -s http://localhost:8080/api/products | jq .
    
    echo -e "\n\n✓ All tests completed!"
    echo "To stop the application, run: docker-compose down"
else
    echo "✗ Docker build failed"
    exit 1
fi

