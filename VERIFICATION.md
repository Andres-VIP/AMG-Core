# Verification Guide

## Code Structure Verification

### ✅ Dependencies
- H2 database dependency added to `build.gradle`
- Spring Data JPA dependency added to `build.gradle`
- All imports are correct and use Jakarta EE (jakarta.persistence.*)

### ✅ Entity Classes
- `User.java`: Correctly annotated with JPA annotations
- `Product.java`: Correctly annotated with JPA annotations
- Both entities have proper validation constraints
- Both entities follow JavaBean conventions

### ✅ Repositories
- `UserRepository.java`: Extends JpaRepository correctly
- `ProductRepository.java`: Extends JpaRepository correctly
- Custom query methods are properly defined

### ✅ Controllers
- `UserController.java`: Full CRUD operations implemented
- `ProductController.java`: Full CRUD operations implemented
- Both follow the same pattern as existing `S3Controller`
- Proper use of `@Valid` for validation
- Proper HTTP status codes (200, 201, 404, 409, 204)

### ✅ Configuration
- `application.yml`: H2 database properly configured
- H2 console enabled at `/h2-console`
- JPA configured with `ddl-auto: update` for automatic schema creation

### ✅ Docker Configuration
- `Dockerfile`: Multi-stage build with Java 21
- `docker-compose.yml`: Properly configured with environment variables

## Testing Instructions

### Prerequisites
1. Docker Desktop must be running
2. Port 8080 must be available

### Step 1: Build and Start with Docker Compose
```bash
docker-compose up -d
```

### Step 2: Verify Application Started
```bash
# Check logs
docker-compose logs -f

# Test health endpoint
curl http://localhost:8080/healthz
# Expected: "ok"
```

### Step 3: Verify H2 Database Console
1. Open browser: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:testdb`
3. Username: `sa`
4. Password: (empty)
5. Click Connect
6. Run query: `SELECT * FROM USERS;` or `SELECT * FROM PRODUCTS;`

### Step 4: Test Users API
```bash
# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","address":"123 Main St"}'

# Get all users
curl http://localhost:8080/api/users

# Get user by ID (replace 1 with actual ID)
curl http://localhost:8080/api/users/1

# Update user
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"John Updated","email":"john@example.com","address":"456 New St"}'

# Delete user
curl -X DELETE http://localhost:8080/api/users/1
```

### Step 5: Test Products API
```bash
# Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"High-performance laptop","price":999.99,"stock":10}'

# Get all products
curl http://localhost:8080/api/products

# Search products by name
curl "http://localhost:8080/api/products?name=laptop"

# Get product by ID
curl http://localhost:8080/api/products/1

# Update product
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Pro","description":"Updated description","price":1299.99,"stock":5}'

# Delete product
curl -X DELETE http://localhost:8080/api/products/1
```

### Step 6: Verify Data Persistence
1. Create some users and products via API
2. Access H2 console at http://localhost:8080/h2-console
3. Verify data exists in `USERS` and `PRODUCTS` tables

### Step 7: Stop Application
```bash
docker-compose down
```

## Expected Results

### Compilation
- ✅ Project compiles successfully with Java 21
- ✅ All dependencies resolve correctly
- ✅ No compilation errors

### Runtime
- ✅ Application starts successfully
- ✅ H2 database initializes automatically
- ✅ Tables are created automatically (ddl-auto: update)
- ✅ H2 console is accessible at /h2-console

### API Endpoints
- ✅ All CRUD operations work correctly
- ✅ Validation works (returns 400 for invalid data)
- ✅ 404 returned for non-existent resources
- ✅ 409 returned for duplicate emails
- ✅ Data persists in H2 database

## Troubleshooting

### Issue: Docker build fails
- **Solution**: Ensure Docker Desktop is running
- **Solution**: Check that port 8080 is not in use

### Issue: Application doesn't start
- **Solution**: Check logs with `docker-compose logs`
- **Solution**: Verify Java 21 is used in Docker (check Dockerfile)

### Issue: H2 console not accessible
- **Solution**: Verify `spring.h2.console.enabled=true` in application.yml
- **Solution**: Check that application started successfully

### Issue: Tables not created
- **Solution**: Verify `spring.jpa.hibernate.ddl-auto=update` in application.yml
- **Solution**: Check application logs for JPA initialization messages

