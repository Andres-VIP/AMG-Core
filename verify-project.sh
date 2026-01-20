#!/bin/bash

echo "=== Verifying Project Setup ==="
echo ""

echo "1. Checking dependencies in build.gradle..."
if grep -q "spring-boot-starter-webflux" build.gradle; then
    echo "   ✅ WebFlux found"
else
    echo "   ❌ WebFlux NOT found"
    exit 1
fi

if grep -q "spring-boot-starter-cache" build.gradle; then
    echo "   ✅ Cache found"
else
    echo "   ❌ Cache NOT found"
    exit 1
fi

if grep -q "springdoc-openapi" build.gradle; then
    echo "   ✅ Swagger found"
else
    echo "   ❌ Swagger NOT found"
    exit 1
fi

echo ""
echo "2. Compiling project..."
./gradlew clean build -x test
if [ $? -eq 0 ]; then
    echo "   ✅ Compilation successful"
else
    echo "   ❌ Compilation failed"
    exit 1
fi

echo ""
echo "3. Running tests..."
./gradlew test
if [ $? -eq 0 ]; then
    echo "   ✅ All tests passed"
else
    echo "   ❌ Tests failed"
    exit 1
fi

echo ""
echo "=== Verification Complete ==="
