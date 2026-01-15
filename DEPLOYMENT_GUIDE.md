# Deployment Guide

This guide explains what needs to be configured in the Java project and in AWS for the CI/CD pipeline to work.

## What Goes in the Java Project

### 1. GitHub Actions Workflow File

**Location**: `.github/workflows/ci-cd.yml`

This file contains the CI/CD pipeline definition that:
- Runs tests on every push and pull request
- Builds the Docker image
- Pushes the image to Amazon ECR
- Optionally deploys to ECS

**What to configure**:
- `AWS_REGION`: Your AWS region (default: `us-east-1`)
- `ECR_REPOSITORY`: Your ECR repository name (default: `aws-java21-spring-s3`)
- `AWS_ROLE_ARN`: IAM role ARN for GitHub Actions (set as GitHub secret)

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
- AWS S3 bucket and region (via environment variables)
- Actuator endpoints
- Logging levels

**No changes needed** - these are already set up correctly.

### 4. Build Configuration

**Location**: `build.gradle`

Already configured with:
- Java 21
- Spring Boot 3.2.7
- AWS SDK v2
- All necessary dependencies

**No changes needed**.

## What Needs to be Configured in AWS

### 1. GitHub Repository Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions

**Required secrets**:
- `AWS_ACCOUNT_ID`: Your 12-digit AWS account ID (e.g., `123456789012`)
- `AWS_ROLE_ARN`: ARN of the IAM role for GitHub Actions (e.g., `arn:aws:iam::123456789012:role/GitHubActionsRole`)

**Optional secrets** (for ECS deployment):
- `ECS_CLUSTER`: Name of your ECS cluster
- `ECS_SERVICE`: Name of your ECS service

### 2. AWS IAM Configuration

#### Step 1: Create OIDC Identity Provider

1. Go to AWS Console → IAM → Identity providers
2. Click "Add provider"
3. Provider type: **OpenID Connect**
4. Provider URL: `https://token.actions.githubusercontent.com`
5. Audience: `sts.amazonaws.com`
6. Click "Add provider"

#### Step 2: Create IAM Role for GitHub Actions

1. Go to IAM → Roles → Create role
2. Trusted entity type: **Web identity**
3. Identity provider: Select the GitHub OIDC provider you just created
4. Audience: `sts.amazonaws.com`
5. Add condition:
   - Condition key: `token.actions.githubusercontent.com:sub`
   - Operator: `StringLike`
   - Value: `repo:YOUR_GITHUB_ORG/YOUR_REPO_NAME:*`
   - Replace with your actual GitHub org and repo name
6. Click "Next"
7. Add permissions: Create a policy with these permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload"
      ],
      "Resource": "arn:aws:ecr:*:*:repository/aws-java21-spring-s3"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecs:UpdateService",
        "ecs:DescribeServices"
      ],
      "Resource": "*",
      "Condition": {
        "StringEquals": {
          "ecs:cluster": "your-cluster-name"
        }
      }
    }
  ]
}
```

8. Name the role: `GitHubActionsRole`
9. Create the role
10. Copy the role ARN and add it to GitHub secrets as `AWS_ROLE_ARN`

### 3. Amazon ECR Repository

#### Create the Repository

```bash
aws ecr create-repository \
  --repository-name aws-java21-spring-s3 \
  --region us-east-1 \
  --image-scanning-configuration scanOnPush=true
```

Or via AWS Console:
1. Go to ECR → Repositories → Create repository
2. Name: `aws-java21-spring-s3`
3. Enable "Scan on push"
4. Create repository

#### Get Repository URI

The repository URI will be: `YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/aws-java21-spring-s3`

### 4. S3 Buckets

Create one bucket per environment:

```bash
# Development
aws s3api create-bucket \
  --bucket your-app-dev-bucket \
  --region us-east-1

# Staging
aws s3api create-bucket \
  --bucket your-app-stg-bucket \
  --region us-east-1

# Production
aws s3api create-bucket \
  --bucket your-app-prod-bucket \
  --region us-east-1
```

### 5. IAM Role for Application Runtime

This role will be used by your application when it runs (ECS task, EC2 instance, etc.).

1. Go to IAM → Roles → Create role
2. Trusted entity: **AWS service**
3. Use case: **ECS Task** (or EC2, Lambda, depending on your deployment)
4. Create a policy with S3 permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-app-dev-bucket",
        "arn:aws:s3:::your-app-stg-bucket",
        "arn:aws:s3:::your-app-prod-bucket"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": [
        "arn:aws:s3:::your-app-dev-bucket/*",
        "arn:aws:s3:::your-app-stg-bucket/*",
        "arn:aws:s3:::your-app-prod-bucket/*"
      ]
    }
  ]
}
```

5. Name the role: `AppRuntimeRole`
6. Attach this role to your ECS task definition, EC2 instance, or Lambda function

## Quick Setup Checklist

### In GitHub:
- [ ] Add `AWS_ACCOUNT_ID` secret
- [ ] Add `AWS_ROLE_ARN` secret
- [ ] (Optional) Add `ECS_CLUSTER` and `ECS_SERVICE` secrets
- [ ] Update workflow file with correct `ECR_REPOSITORY` name if different

### In AWS:
- [ ] Create OIDC identity provider for GitHub
- [ ] Create IAM role for GitHub Actions with ECR permissions
- [ ] Create ECR repository
- [ ] Create S3 buckets for each environment
- [ ] Create IAM role for application runtime with S3 permissions
- [ ] (If using ECS) Create ECS cluster and service

## Testing the Pipeline

1. **Push to main branch**: The workflow should:
   - Run tests
   - Build Docker image
   - Push to ECR
   - (If configured) Deploy to ECS

2. **Verify in ECR**:
   ```bash
   aws ecr list-images --repository-name aws-java21-spring-s3
   ```

3. **Check GitHub Actions**: Go to Actions tab to see workflow execution

## Troubleshooting

### "Access Denied" when pushing to ECR
- Verify IAM role has correct ECR permissions
- Check that OIDC provider is configured correctly
- Verify the role trust policy includes your GitHub repo

### "Missing secret: AWS_ACCOUNT_ID"
- Add the secret in GitHub repository settings
- Ensure the secret name matches exactly

### Workflow doesn't trigger
- Check that the branch name matches the workflow trigger (`main` or `develop`)
- Verify GitHub Actions is enabled for the repository

## Summary

**In the Java project**: Everything is already configured. You only need to:
- Update the workflow file with your ECR repository name (if different)
- Ensure GitHub secrets are set

**In AWS**: You need to create:
- OIDC identity provider
- IAM role for GitHub Actions
- ECR repository
- S3 buckets
- IAM role for application runtime

See `AWS_SETUP.md` for detailed step-by-step instructions.

