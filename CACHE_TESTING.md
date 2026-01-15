# Testing Cache Functionality

## What Was Added

### 1. Dependencies
- **Spring WebFlux**: For reactive programming
- **Spring Cache**: For caching functionality

### 2. Cache Configuration
- Cache type: Simple (in-memory)
- Cache names: `products`, `users`, `productStats`

### 3. New Services and Controllers

#### ProductCacheService
Service with cache annotations:
- `@Cacheable`: Caches method results
- `@CachePut`: Updates cache when saving
- `@CacheEvict`: Clears cache when deleting

#### ProductCacheController
REST endpoints to test cache:
- `/api/cache/products` - CRUD with cache
- `/api/cache/products/stats/*` - Statistics with cache
- `/api/cache/products/cache/clear` - Clear cache manually

#### ReactiveProductController
Reactive endpoints using WebFlux:
- `/api/reactive/products` - Reactive product operations

## How to Test Cache

### Step 1: Start the Application
```bash
docker-compose up -d
```

### Step 2: Create a Product (First Time - No Cache)
```powershell
$body = @{
    name = "Test Product"
    description = "Testing cache"
    price = 99.99
    stock = 10
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:8080/api/cache/products -Method POST -Body $body -ContentType "application/json"
```

**Note the ID** from the response (e.g., `id: 1`)

### Step 3: Get Product by ID (First Call - Hits Database)
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/cache/products/1 -Method GET
```

**Check the logs** - You should see:
```
Fetching product from database: 1
```

### Step 4: Get Product by ID Again (Second Call - Uses Cache)
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/cache/products/1 -Method GET
```

**Check the logs** - You should NOT see the "Fetching product from database" message because it's using cache!

### Step 5: Test Statistics Cache
```powershell
# First call - hits database
Invoke-RestMethod -Uri http://localhost:8080/api/cache/products/stats/total-value -Method GET

# Second call - uses cache
Invoke-RestMethod -Uri http://localhost:8080/api/cache/products/stats/total-value -Method GET
```

### Step 6: Test Cache Eviction (Delete)
```powershell
# Delete product - this clears the cache
Invoke-RestMethod -Uri http://localhost:8080/api/cache/products/1 -Method DELETE

# Try to get it again - will hit database (not found)
Invoke-RestMethod -Uri http://localhost:8080/api/cache/products/1 -Method GET
```

### Step 7: Clear All Cache Manually
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/cache/products/cache/clear -Method POST
```

### Step 8: Test Reactive Endpoints (WebFlux)
```powershell
# Get all products reactively
Invoke-RestMethod -Uri http://localhost:8080/api/reactive/products -Method GET

# Stream products (Server-Sent Events)
# Open in browser: http://localhost:8080/api/reactive/products/stream
```

## Cache Behavior

### @Cacheable
- **First call**: Executes method, stores result in cache
- **Subsequent calls**: Returns cached value (doesn't execute method)
- **Example**: `findById()`, `findAll()`, `calculateTotalInventoryValue()`

### @CachePut
- Always executes method
- Updates cache with result
- **Example**: `save()` - updates cache for the saved product

### @CacheEvict
- Executes method
- Removes entries from cache
- **Example**: `deleteById()` - removes product from cache

## Verifying Cache in Logs

Watch the application logs:
```bash
docker-compose logs -f amg-core
```

You'll see:
- **First call**: `Fetching product from database: 1`
- **Cached call**: No log message (using cache)
- **After delete**: `Deleting product from database: 1` (cache cleared)

## Cache Keys

- `products::1` - Single product by ID
- `products::all` - All products list
- `products::name:laptop` - Products by name
- `productStats::totalValue` - Total inventory value
- `productStats::count` - Product count

## Performance Test

```powershell
# Measure time for first call (database)
Measure-Command { Invoke-RestMethod -Uri http://localhost:8080/api/cache/products/1 -Method GET }

# Measure time for second call (cache) - should be faster!
Measure-Command { Invoke-RestMethod -Uri http://localhost:8080/api/cache/products/1 -Method GET }
```

