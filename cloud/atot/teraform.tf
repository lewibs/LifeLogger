# Configure the AWS Provider
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "us-west-2"  # Change to your preferred region
}

#S3
# Generate random suffix for bucket name (S3 bucket names must be globally unique)
resource "random_string" "bucket_suffix" {
  length  = 8
  special = false
  upper   = false
}

# Create S3 bucket
resource "aws_s3_bucket" "example_bucket" {
  bucket = "life-logger-${random_string.bucket_suffix.result}"
  
  tags = {
    Name        = "LifeLogs"
    Environment = "Prod"
  }
}


# Block public access (recommended for security)
resource "aws_s3_bucket_public_access_block" "example_pab" {
  bucket = aws_s3_bucket.example_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# LAMBDA 
# IAM role for Lambda execution
data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "example" {
  name               = "atot_iam"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

# Lambda function
resource "aws_lambda_function" "audio_to_text" {
  function_name    = "audio_to_text"
  role             = aws_iam_role.example.arn
  timeout          = 60
  image_uri        = "715006185945.dkr.ecr.us-west-2.amazonaws.com/audio_to_text:latest"
  package_type     = "Image"
   

  environment {
    variables = {
      ENVIRONMENT = "production"
      LOG_LEVEL   = "info"
    }
  }

  tags = {
    Environment = "production"
    Application = "audio_to_text"
  }
}

# Output the bucket name and ARN
output "bucket_name" {
  value = aws_s3_bucket.example_bucket.id
}

output "bucket_arn" {
  value = aws_s3_bucket.example_bucket.arn
}

output "bucket_domain_name" {
  value = aws_s3_bucket.example_bucket.bucket_domain_name
}

# Lambda function name
output "lambda_function_name" {
  value = aws_lambda_function.audio_to_text.function_name
}

# Lambda function ARN
output "lambda_function_arn" {
  value = aws_lambda_function.audio_to_text.arn
}

# Lambda function invoke ARN (useful for API Gateway)
output "lambda_invoke_arn" {
  value = aws_lambda_function.audio_to_text.invoke_arn
}

# Lambda function version
output "lambda_version" {
  value = aws_lambda_function.audio_to_text.version
}

# Lambda function last modified date
output "lambda_last_modified" {
  value = aws_lambda_function.audio_to_text.last_modified
}

# Lambda function role ARN
output "lambda_role_arn" {
  value = aws_lambda_function.audio_to_text.role
}

# Lambda function runtime
output "lambda_runtime" {
  value = aws_lambda_function.audio_to_text.runtime
}