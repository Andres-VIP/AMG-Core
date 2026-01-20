# Deployment Guide

This guide explains how to build and deploy the application using Docker.

## What Goes in the Java Project

### 1. GitHub Actions Workflow File

**Location**: `.github/workflows/ci-cd.yml`

This file contains the CI/CD pipeline definition that:
- Runs tests on every push and pull request
- Builds the Docker image

**No configuration needed** - the workflow is ready to use.

### 2. Dockerfile

**Location**: `Dockerfile`

The Dockerfile is already configured with:
- Multi-stage build (build stage + runtime stage)
- Java 21 runtime
- Minimal Alpine Linux base image
- Proper port exposure (8080)

**No changes needed** unless you need custom build steps.

### 3. Application Configuration Files

**Locations**:
- `src/main/resources/application.yml` (base config)
- `src/main/resources/application-dev.yml` (development)
- `src/main/resources/application-stg.yml` (staging)
- `src/main/resources/application-prod.yml` (production)

**What's configured**:
- Spring Boot application settings
- H2 database configuration
- Actuator endpoints
- Logging levels
- Cache configuration
- Retry configuration

**No changes needed** - these are already set up correctly.

### 4. Build Configuration

**Location**: `build.gradle`

Already configured with:
- Java 21
- Spring Boot 3.2.7
- All necessary dependencies

**No changes needed**.

## Running the Application

### Using Docker Compose

```bash
docker-compose up -d
```

The application will be available at `http://localhost:8080`

### Using Docker

```bash
docker build -t amg-core:latest .
docker run -p 8080:8080 amg-core:latest
```

## Quick Setup Checklist

### Local Development
- [ ] Docker Desktop installed and running
- [ ] Port 8080 available
- [ ] Run `docker-compose up -d`

### CI/CD
- [ ] GitHub Actions workflow is configured
- [ ] Push to `main` or `develop` branch to trigger pipeline

## Testing the Pipeline

1. **Push to main branch**: The workflow should:
   - Run tests
   - Build Docker image

2. **Check GitHub Actions**: Go to Actions tab to see workflow execution

## Troubleshooting

### "Docker build fails"
- Verify Docker Desktop is running
- Check that all dependencies are available

### "Workflow doesn't trigger"
- Check that the branch name matches the workflow trigger (`main` or `develop`)
- Verify GitHub Actions is enabled for the repository

## Summary

**The project is ready for deployment:**
- Docker configuration is complete
- CI/CD pipeline is configured
- Application can be built and run with Docker
