Write-Host "=== Verifying Project Setup ===" -ForegroundColor Cyan
Write-Host ""

Write-Host "1. Checking dependencies in build.gradle..." -ForegroundColor Yellow
$webflux = Select-String -Path build.gradle -Pattern "spring-boot-starter-webflux"
$cache = Select-String -Path build.gradle -Pattern "spring-boot-starter-cache"
$swagger = Select-String -Path build.gradle -Pattern "springdoc-openapi"

if ($webflux) {
    Write-Host "   ✅ WebFlux found" -ForegroundColor Green
} else {
    Write-Host "   ❌ WebFlux NOT found" -ForegroundColor Red
    exit 1
}

if ($cache) {
    Write-Host "   ✅ Cache found" -ForegroundColor Green
} else {
    Write-Host "   ❌ Cache NOT found" -ForegroundColor Red
    exit 1
}

if ($swagger) {
    Write-Host "   ✅ Swagger found" -ForegroundColor Green
} else {
    Write-Host "   ❌ Swagger NOT found" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. Compiling project..." -ForegroundColor Yellow
./gradlew clean build -x test
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed" -ForegroundColor Red
    exit 13
}

Write-Host ""
Write-Host "3. Running tests..." -ForegroundColor Yellow
./gradlew test
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ All tests passed" -ForegroundColor Green
} else {
    Write-Host "   ❌ Tests failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Verification Complete ===" -ForegroundColor Green
