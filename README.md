# AMG-Core

A production-ready Spring Boot microservice built with Java 21 that provides RESTful APIs for example entities (Users and Products) with H2 database, and S3 text object operations. The service is designed for deployment using Docker containers.

## Technology Stack

- **Java**: 21 (LTS)
- **Framework**: Spring Boot 3.2.7
- **Build Tool**: Gradle 8.8
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA / Hibernate
- **Reactive**: Spring WebFlux
- **Caching**: Spring Cache (Simple Cache)
- **AWS SDK**: AWS SDK for Java v2 (2.25.62) - S3 module
- **Container**: Docker (multi-stage build with Alpine Linux)
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Documentation**: SpringDoc OpenAPI 3 (Swagger UI)
- **Monitoring**: Spring Boot Actuator

## Features

- RESTful APIs for example entities (Users and Products) with full CRUD operations
- H2 in-memory database with automatic schema generation
- **Spring Cache** with cacheable methods for improved performance
- **Spring WebFlux** reactive endpoints for non-blocking operations
- RESTful API for S3 text object operations
- Environment-based configuration (dev, staging, production)
- Comprehensive error handling with proper HTTP status codes
- Input validation using Jakarta Bean Validation
- Structured logging for operations and errors
- Health check endpoints for monitoring
- Docker containerization with Docker Compose for easy deployment
- CI/CD ready with GitHub Actions support
- AWS IAM role-based authentication (no hardcoded credentials)

## Prerequisites

- **Java 21** (JDK) - LTS version
- **Docker** and **Docker Compose** (for containerized builds and deployment)
- **AWS Account** with:
  - An S3 bucket
  - IAM role or user with appropriate S3 permissions
- **Gradle 8+** (optional, for local builds without Docker)

## Project Structure

```
AMG-Core/
├── src/
│   ├── main/
│   │   ├── java/com/acme/platform/
│   │   │   ├── api/                    # REST controllers
│   │   │   │   ├── exception/         # Global exception handler
│   │   │   │   ├── HealthController.java
│   │   │   │   ├── S3Controller.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── ProductController.java
│   │   │   ├── config/                 # Configuration classes
│   │   │   │   ├── properties/         # Configuration properties
│   │   │   │   └── S3Config.java       # AWS S3 client configuration
│   │   │   ├── model/                  # JPA entities
│   │   │   │   ├── User.java
│   │   │   │   └── Product.java
│   │   │   ├── repository/             # JPA repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── ProductRepository.java
│   │   │   ├── service/                # Business logic
│   │   │   │   └── S3ObjectService.java
│   │   │   └── Application.java        # Spring Boot entry point
│   │   └── resources/
│   │       ├── application.yml         # Base configuration
│   │       ├── application-dev.yml     # Development profile
│   │       ├── application-stg.yml     # Staging profile
│   │       └── application-prod.yml     # Production profile
│   └── test/                           # Unit tests
├── build.gradle                         # Gradle build configuration
├── Dockerfile                           # Multi-stage Docker build
├── docker-compose.yml                   # Docker Compose configuration
├── gradle.properties                    # Gradle settings
├── settings.gradle                      # Gradle project settings
└── README.md
```

## Configuration

### Database

The application uses H2 in-memory database by default. The H2 console is available at `/h2-console` for database inspection.

- **Database URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (empty)
- **Schema**: Automatically created on startup using JPA `ddl-auto: update`

### Environment Variables

The application uses environment variables for runtime configuration:

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `AWS_S3_BUCKET` | Yes | `change-me` | S3 bucket name |
| `AWS_REGION` | No | `us-east-1` | AWS region |
| `SPRING_PROFILES_ACTIVE` | Recommended | - | Active Spring profile (`dev`, `stg`, `prod`) |

### Spring Profiles

The application supports three environment profiles:

- **dev**: Development environment
- **stg**: Staging environment
- **prod**: Production environment

Each profile can override default configuration values. Profile-specific settings are defined in `application-{profile}.yml` files.

### AWS Credentials

The service uses the AWS SDK default credentials provider chain, which checks credentials in the following order:

1. Environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
2. Java system properties
3. AWS credentials file (`~/.aws/credentials`)
4. IAM role (when running on AWS infrastructure)

**For local development:**
```bash
# Option 1: Environment variables
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key

# Option 2: AWS CLI profile
aws configure --profile default
```

**For AWS deployment:**
Attach an IAM role to your compute resource (EC2, ECS, Lambda) with appropriate S3 permissions. The SDK will automatically use the role's temporary credentials.

### IAM Permissions

The IAM role or user needs the following S3 permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:ListBucket"],
      "Resource": "arn:aws:s3:::your-bucket-name"
    },
    {
      "Effect": "Allow",
      "Action": ["s3:GetObject", "s3:PutObject"],
      "Resource": "arn:aws:s3:::your-bucket-name/*"
    }
  ]
}
```

## Running the Application

### Local Development with Gradle

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=dev
export AWS_S3_BUCKET=my-dev-bucket
export AWS_REGION=us-east-1

# Run the application
./gradlew clean bootRun
```

### Running with Docker Compose

```bash
# Start the application
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

The application will be available at `http://localhost:8080`.

### Local Development with Docker

```bash
# Build the Docker image
docker build -t amg-core:local .

# Run the container
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e AWS_S3_BUCKET=my-dev-bucket \
  -e AWS_REGION=us-east-1 \
  -e AWS_ACCESS_KEY_ID=your-key \
  -e AWS_SECRET_ACCESS_KEY=your-secret \
  amg-core:local
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with coverage (if configured)
./gradlew test jacocoTestReport
```

## API Endpoints

### Health Check

- **GET** `/healthz`
  - Returns: `200 OK` with body `"ok"`
  - Description: Simple health check endpoint

- **GET** `/actuator/health`
  - Returns: JSON health status
  - Description: Spring Boot Actuator health endpoint

### User Management

- **GET** `/api/users`
  - Description: Get all users
  - Returns: `200 OK` with JSON array of users
  - Example: `GET /api/users`

- **GET** `/api/users/{id}`
  - Description: Get user by ID
  - Path Parameters:
    - `id` (required): User ID
  - Returns: `200 OK` with user JSON, or `404 Not Found`
  - Example: `GET /api/users/1`

- **POST** `/api/users`
  - Description: Create a new user
  - Request Body: JSON user object
    ```json
    {
      "name": "John Doe",
      "email": "john@example.com",
      "address": "123 Main St"
    }
    ```
  - Returns: `201 Created` with created user, or `409 Conflict` if email exists
  - Example: `POST /api/users`

- **PUT** `/api/users/{id}`
  - Description: Update an existing user
  - Path Parameters:
    - `id` (required): User ID
  - Request Body: JSON user object
  - Returns: `200 OK` with updated user, or `404 Not Found`
  - Example: `PUT /api/users/1`

- **DELETE** `/api/users/{id}`
  - Description: Delete a user
  - Path Parameters:
    - `id` (required): User ID
  - Returns: `204 No Content`, or `404 Not Found`
  - Example: `DELETE /api/users/1`

### Product Management

- **GET** `/api/products`
  - Description: Get all products, optionally filtered by name
  - Query Parameters:
    - `name` (optional): Filter products by name (case-insensitive)
  - Returns: `200 OK` with JSON array of products
  - Example: `GET /api/products` or `GET /api/products?name=laptop`

- **GET** `/api/products/{id}`
  - Description: Get product by ID
  - Path Parameters:
    - `id` (required): Product ID
  - Returns: `200 OK` with product JSON, or `404 Not Found`
  - Example: `GET /api/products/1`

- **POST** `/api/products`
  - Description: Create a new product
  - Request Body: JSON product object
    ```json
    {
      "name": "Laptop",
      "description": "High-performance laptop",
      "price": 999.99,
      "stock": 10
    }
    ```
  - Returns: `201 Created` with created product
  - Example: `POST /api/products`

- **PUT** `/api/products/{id}`
  - Description: Update an existing product
  - Path Parameters:
    - `id` (required): Product ID
  - Request Body: JSON product object
  - Returns: `200 OK` with updated product, or `404 Not Found`
  - Example: `PUT /api/products/1`

- **DELETE** `/api/products/{id}`
  - Description: Delete a product
  - Path Parameters:
    - `id` (required): Product ID
  - Returns: `204 No Content`, or `404 Not Found`
  - Example: `DELETE /api/products/1`

### Product Management with Cache

These endpoints use Spring Cache for improved performance:

- **GET** `/api/cache/products`
  - Description: Get all products (cached)
  - Returns: `200 OK` with JSON array of products
  - Note: First call hits database, subsequent calls use cache

- **GET** `/api/cache/products/{id}`
  - Description: Get product by ID (cached)
  - Returns: `200 OK` with product JSON, or `404 Not Found`
  - Note: Cached by product ID

- **GET** `/api/cache/products/search?name={name}`
  - Description: Search products by name (cached)
  - Query Parameters:
    - `name` (required): Product name to search
  - Returns: `200 OK` with JSON array of products

- **POST** `/api/cache/products`
  - Description: Create a new product (updates cache)
  - Request Body: JSON product object
  - Returns: `201 Created` with created product

- **DELETE** `/api/cache/products/{id}`
  - Description: Delete a product (clears cache)
  - Returns: `204 No Content`

- **GET** `/api/cache/products/stats/total-value`
  - Description: Calculate total inventory value (cached)
  - Returns: `200 OK` with total value
  - Example: `{"totalValue": 9999.99}`

- **GET** `/api/cache/products/stats/count`
  - Description: Get product count (cached)
  - Returns: `200 OK` with count
  - Example: `{"count": 10}`

- **POST** `/api/cache/products/cache/clear`
  - Description: Manually clear all product caches
  - Returns: `200 OK` with success message

### Reactive Product Endpoints (WebFlux)

These endpoints use Spring WebFlux for reactive/non-blocking operations:

- **GET** `/api/reactive/products`
  - Description: Get all products reactively
  - Returns: `200 OK` with NDJSON stream of products
  - Content-Type: `application/x-ndjson`

- **GET** `/api/reactive/products/{id}`
  - Description: Get product by ID reactively
  - Returns: `200 OK` with product JSON, or `404 Not Found`

- **POST** `/api/reactive/products`
  - Description: Create a new product reactively
  - Request Body: JSON product object
  - Returns: `201 Created` with created product

- **GET** `/api/reactive/products/stream`
  - Description: Stream products with Server-Sent Events
  - Returns: `200 OK` with text/event-stream
  - Note: Products are streamed one per second for demonstration

### S3 Operations

- **GET** `/api/s3/keys?prefix={optional}`
  - Description: List all object keys in the S3 bucket
  - Query Parameters:
    - `prefix` (optional): Filter keys by prefix
  - Returns: `200 OK` with JSON array of key strings
  - Example: `GET /api/s3/keys?prefix=folder/`

- **POST** `/api/s3/text?key={key}`
  - Description: Upload a text object to S3
  - Query Parameters:
    - `key` (required): S3 object key
  - Request Body: Plain text (`text/plain`)
  - Returns: `202 Accepted`
  - Example: `POST /api/s3/text?key=documents/readme.txt` with body `"Hello World"`

- **GET** `/api/s3/text?key={key}`
  - Description: Download a text object from S3
  - Query Parameters:
    - `key` (required): S3 object key
  - Returns: `200 OK` with plain text content
  - Example: `GET /api/s3/text?key=documents/readme.txt`

### API Documentation

When running the application, Swagger UI is available at:
- **GET** `/swagger-ui.html` - Interactive API documentation

### H2 Database Console

The H2 database console is available at:
- **GET** `/h2-console` - H2 database management console

## Error Handling

The application includes comprehensive error handling:

- **400 Bad Request**: Invalid input parameters or validation failures
- **404 Not Found**: Resource does not exist (user, product, or S3 object)
- **409 Conflict**: Resource conflict (e.g., duplicate email)
- **500 Internal Server Error**: Unexpected errors or operation failures

Error responses follow this format:
```json
{
  "error": "Error type",
  "message": "Human-readable error message"
}
```

## Building and Packaging

### Build JAR

```bash
./gradlew clean bootJar
```

The executable JAR will be created in `build/libs/AMG-Core-0.0.1.jar`

### Build Docker Image

```bash
docker build -t amg-core:0.0.1 .
```

The Dockerfile uses a multi-stage build:
1. Build stage: Uses Gradle to compile and package the application
2. Runtime stage: Uses Alpine Linux with JRE 21 for minimal image size

## CI/CD with GitHub Actions

The project is configured for CI/CD using GitHub Actions with AWS OIDC authentication.

### Required GitHub Secrets

- `AWS_ACCOUNT_ID`: Your 12-digit AWS account ID

### Workflow Configuration

Update the workflow file (`.github/workflows/ci-cd.yml`) with:

- `role-to-assume`: IAM role ARN for GitHub OIDC
- `ECR_REPOSITORY`: Amazon ECR repository name
- `AWS_REGION`: Target AWS region

### Workflow Steps

1. Checkout code
2. Set up Java 21
3. Run tests
4. Build application
5. Build Docker image
6. Authenticate to AWS ECR using OIDC
7. Push Docker image to ECR
8. Deploy (ECS, Elastic Beanstalk, etc.)

## Deployment to AWS

### Amazon ECS

1. Push Docker image to ECR (via CI/CD or manually)
2. Create ECS task definition referencing the ECR image
3. Create ECS service with the task definition
4. Ensure ECS task role has S3 permissions

### AWS Elastic Beanstalk

1. Build and push Docker image to ECR
2. Create Elastic Beanstalk application
3. Create environment with Docker platform
4. Configure environment variables
5. Deploy using EB CLI or console

### AWS Lambda

1. Package as JAR (not Docker for Lambda)
2. Create Lambda function with Java 21 runtime
3. Configure execution role with S3 permissions
4. Set environment variables
5. Configure API Gateway if needed

### Environment Variables for Deployment

Set these in your deployment platform:

```bash
SPRING_PROFILES_ACTIVE=prod
AWS_S3_BUCKET=your-production-bucket
AWS_REGION=us-east-1
```

**Note**: Do not set `AWS_ACCESS_KEY_ID` or `AWS_SECRET_ACCESS_KEY` when using IAM roles.

## Monitoring and Observability

### Actuator Endpoints

The application exposes Spring Boot Actuator endpoints:

- `/actuator/health` - Health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics format

### Logging

The application uses SLF4J with Logback for structured logging. Log levels can be configured per profile in the YAML files.

## Development

### Code Style

- Follow Java naming conventions
- Use dependency injection (constructor injection preferred)
- Keep services focused and single-responsibility
- Write unit tests for business logic
- Use meaningful variable and method names

### Testing

- Unit tests use JUnit 5 and Mockito
- Controller tests use `@WebMvcTest` for isolated testing
- Service tests use `@ExtendWith(MockitoExtension.class)` for pure unit testing
- Integration tests can use `@SpringBootTest` for full context

## Troubleshooting

### Common Issues

**Issue**: `S3Exception: Access Denied`
- **Solution**: Verify IAM role/user has correct S3 permissions and bucket policy

**Issue**: `NoSuchKeyException` when getting objects
- **Solution**: Verify the object key exists and is correct (case-sensitive)

**Issue**: Application fails to start with credential errors
- **Solution**: Ensure AWS credentials are configured via environment variables, credentials file, or IAM role

**Issue**: Docker build fails
- **Solution**: Ensure Docker has sufficient resources and network access to download dependencies

## Security Considerations

- Never commit AWS credentials to the repository
- Use IAM roles instead of access keys when possible
- Enable MFA for IAM users with console access
- Restrict S3 bucket policies to minimum required permissions
- Use environment-specific configurations
- Keep dependencies updated for security patches

## License

Proprietary. Internal use only.

## Support

For issues, questions, or contributions, please contact the development team.
