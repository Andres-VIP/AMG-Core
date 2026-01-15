# AWS Configuration Guide

This document describes all the AWS resources and configurations needed to deploy and run this application.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [AWS Resources to Create](#aws-resources-to-create)
3. [IAM Configuration](#iam-configuration)
4. [ECR Repository Setup](#ecr-repository-setup)
5. [S3 Bucket Setup](#s3-bucket-setup)
6. [GitHub Actions OIDC Setup](#github-actions-oidc-setup)
7. [Deployment Options](#deployment-options)

## Prerequisites

- AWS Account
- AWS CLI installed and configured (for local setup)
- Access to AWS Console with appropriate permissions
- GitHub repository with Actions enabled

## AWS Resources to Create

### 1. S3 Buckets

Create one S3 bucket per environment (dev, staging, production):

```bash
# Development bucket
aws s3api create-bucket \
  --bucket your-app-dev-bucket \
  --region us-east-1

# Staging bucket
aws s3api create-bucket \
  --bucket your-app-stg-bucket \
  --region us-east-1

# Production bucket
aws s3api create-bucket \
  --bucket your-app-prod-bucket \
  --region us-east-1
```

**Note**: Bucket names must be globally unique across all AWS accounts.

### 2. ECR Repository

Create an Amazon ECR repository to store Docker images:

```bash
aws ecr create-repository \
  --repository-name aws-java21-spring-s3 \
  --region us-east-1 \
  --image-scanning-configuration scanOnPush=true
```

This will return a repository URI like: `123456789012.dkr.ecr.us-east-1.amazonaws.com/aws-java21-spring-s3`

## IAM Configuration

### 1. IAM Role for Application Runtime (ECS/EC2/Lambda)

This role will be attached to your compute resource (ECS task, EC2 instance, or Lambda function).

#### Create the Role

```bash
aws iam create-role \
  --role-name AppRuntimeRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }]
  }'
```

**For EC2:**
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": {
      "Service": "ec2.amazonaws.com"
    },
    "Action": "sts:AssumeRole"
  }]
}
```

**For Lambda:**
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": {
      "Service": "lambda.amazonaws.com"
    },
    "Action": "sts:AssumeRole"
  }]
}
```

#### Attach S3 Policy

Create a policy file `s3-access-policy.json`:

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

Attach the policy:

```bash
aws iam put-role-policy \
  --role-name AppRuntimeRole \
  --policy-name S3AccessPolicy \
  --policy-document file://s3-access-policy.json
```

**For ECS**, also attach the ECS task execution role policy:

```bash
aws iam attach-role-policy \
  --role-name AppRuntimeRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
```

### 2. IAM Role for GitHub Actions (OIDC)

This role allows GitHub Actions to push Docker images to ECR.

#### Create OIDC Identity Provider

1. Go to IAM → Identity providers → Add provider
2. Provider type: OpenID Connect
3. Provider URL: `https://token.actions.githubusercontent.com`
4. Audience: `sts.amazonaws.com`
5. Click "Add provider"

#### Create IAM Role for GitHub

Create a trust policy file `github-oidc-trust-policy.json`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::YOUR_ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:YOUR_GITHUB_ORG/YOUR_REPO_NAME:*"
        }
      }
    }
  ]
}
```

Replace:
- `YOUR_ACCOUNT_ID`: Your 12-digit AWS account ID
- `YOUR_GITHUB_ORG`: Your GitHub organization or username
- `YOUR_REPO_NAME`: Your repository name

Create the role:

```bash
aws iam create-role \
  --role-name GitHubActionsRole \
  --assume-role-policy-document file://github-oidc-trust-policy.json
```

#### Attach ECR Permissions

Create a policy file `ecr-push-policy.json`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload"
      ],
      "Resource": "*"
    }
  ]
}
```

Attach the policy:

```bash
aws iam put-role-policy \
  --role-name GitHubActionsRole \
  --policy-name ECRPushPolicy \
  --policy-document file://ecr-push-policy.json
```

## ECR Repository Setup

### Get Repository URI

```bash
aws ecr describe-repositories \
  --repository-names aws-java21-spring-s3 \
  --region us-east-1 \
  --query 'repositories[0].repositoryUri' \
  --output text
```

### Configure Lifecycle Policy (Optional)

To automatically delete old images:

```bash
aws ecr put-lifecycle-policy \
  --repository-name aws-java21-spring-s3 \
  --lifecycle-policy-text '{
    "rules": [{
      "rulePriority": 1,
      "description": "Keep last 10 images",
      "selection": {
        "tagStatus": "any",
        "countType": "imageCountMoreThan",
        "countNumber": 10
      },
      "action": {
        "type": "expire"
      }
    }]
  }'
```

## S3 Bucket Setup

### Enable Versioning (Recommended)

```bash
aws s3api put-bucket-versioning \
  --bucket your-app-dev-bucket \
  --versioning-configuration Status=Enabled
```

### Configure Bucket Policy (Optional)

If you need to restrict access further, create a bucket policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowAppAccess",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::YOUR_ACCOUNT_ID:role/AppRuntimeRole"
      },
      "Action": [
        "s3:ListBucket",
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": [
        "arn:aws:s3:::your-app-dev-bucket",
        "arn:aws:s3:::your-app-dev-bucket/*"
      ]
    }
  ]
}
```

## GitHub Actions OIDC Setup

### 1. Configure GitHub Repository Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions → New repository secret

Add:
- `AWS_ACCOUNT_ID`: Your 12-digit AWS account ID

### 2. Update GitHub Actions Workflow

Edit `.github/workflows/ci-cd.yml` and update:

```yaml
env:
  AWS_REGION: us-east-1  # Change to your region
  ECR_REPOSITORY: aws-java21-spring-s3  # Change to your ECR repo name

# In the Configure AWS credentials step:
role-to-assume: arn:aws:iam::YOUR_ACCOUNT_ID:role/GitHubActionsRole
```

Replace `YOUR_ACCOUNT_ID` with your actual AWS account ID.

## Deployment Options

### Option 1: Amazon ECS (Recommended for Containerized Apps)

#### Create ECS Cluster

```bash
aws ecs create-cluster \
  --cluster-name app-cluster \
  --region us-east-1
```

#### Create Task Definition

Create `task-definition.json`:

```json
{
  "family": "aws-java21-spring-s3",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "executionRoleArn": "arn:aws:iam::YOUR_ACCOUNT_ID:role/AppRuntimeRole",
  "taskRoleArn": "arn:aws:iam::YOUR_ACCOUNT_ID:role/AppRuntimeRole",
  "containerDefinitions": [
    {
      "name": "app",
      "image": "YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/aws-java21-spring-s3:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "AWS_S3_BUCKET",
          "value": "your-app-prod-bucket"
        },
        {
          "name": "AWS_REGION",
          "value": "us-east-1"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/aws-java21-spring-s3",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

Register the task definition:

```bash
aws ecs register-task-definition \
  --cli-input-json file://task-definition.json
```

#### Create ECS Service

```bash
aws ecs create-service \
  --cluster app-cluster \
  --service-name app-service \
  --task-definition aws-java21-spring-s3 \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx],securityGroups=[sg-xxx],assignPublicIp=ENABLED}"
```

### Option 2: AWS Elastic Beanstalk

1. Create Elastic Beanstalk application
2. Create environment with Docker platform
3. Configure environment variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `AWS_S3_BUCKET=your-app-prod-bucket`
   - `AWS_REGION=us-east-1`
4. Deploy using EB CLI or upload Docker image

### Option 3: AWS Lambda

1. Package as JAR (not Docker for Lambda Java runtime)
2. Create Lambda function with Java 21 runtime
3. Set execution role to `AppRuntimeRole`
4. Configure environment variables
5. Set handler: `com.acme.platform.Application::main`
6. Configure API Gateway if needed

## Verification Checklist

- [ ] S3 buckets created for all environments
- [ ] ECR repository created
- [ ] IAM role for application runtime created with S3 permissions
- [ ] IAM role for GitHub Actions created with ECR permissions
- [ ] OIDC identity provider configured in IAM
- [ ] GitHub repository secret `AWS_ACCOUNT_ID` configured
- [ ] GitHub Actions workflow updated with correct role ARN and ECR repository name
- [ ] ECS/EB/Lambda deployment configured (if applicable)
- [ ] Application can access S3 bucket (test with a simple request)

## Testing the Setup

### Test S3 Access Locally

```bash
# Set AWS credentials
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret
export AWS_REGION=us-east-1

# Test S3 access
aws s3 ls s3://your-app-dev-bucket
```

### Test GitHub Actions Workflow

1. Push to `main` branch
2. Check GitHub Actions tab for workflow execution
3. Verify Docker image is pushed to ECR:
   ```bash
   aws ecr list-images --repository-name aws-java21-spring-s3
   ```

## Troubleshooting

### GitHub Actions fails with "Access Denied"

- Verify OIDC identity provider is configured correctly
- Check that the role trust policy includes your GitHub repo
- Ensure `AWS_ACCOUNT_ID` secret is set correctly

### Application can't access S3

- Verify IAM role is attached to compute resource
- Check S3 bucket policy allows the role
- Verify bucket name matches environment variable

### ECR push fails

- Verify GitHub Actions role has ECR permissions
- Check that ECR repository exists
- Ensure AWS region matches in workflow

## Security Best Practices

1. **Never commit AWS credentials** to the repository
2. **Use IAM roles** instead of access keys when possible
3. **Enable MFA** for IAM users with console access
4. **Restrict IAM policies** to minimum required permissions
5. **Use separate buckets** for different environments
6. **Enable S3 versioning** for production buckets
7. **Enable ECR image scanning** for security vulnerabilities
8. **Rotate credentials** regularly if using access keys
9. **Use VPC endpoints** for S3 access from private subnets
10. **Enable CloudTrail** to audit AWS API calls

