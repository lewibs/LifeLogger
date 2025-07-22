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

# Package the Lambda function code
data "archive_file" "example" {
  type        = "zip"
  source_file = "./lambda.py"
  output_path = "./lambda.zip"
}

# Lambda function
resource "aws_lambda_function" "atot_lambda" {
  filename         = data.archive_file.example.output_path
  function_name    = "atot_lambda"
  role             = aws_iam_role.example.arn
  handler          = "lambda.handler"
  source_code_hash = data.archive_file.example.output_base64sha256

  runtime = "python3.11" 

  environment {
    variables = {
      ENVIRONMENT = "production"
      LOG_LEVEL   = "info"
    }
  }

  tags = {
    Environment = "production"
    Application = "atot_lambda"
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
  value = aws_lambda_function.atot_lambda.function_name
}

# Lambda function ARN
output "lambda_function_arn" {
  value = aws_lambda_function.atot_lambda.arn
}

# Lambda function invoke ARN (useful for API Gateway)
output "lambda_invoke_arn" {
  value = aws_lambda_function.atot_lambda.invoke_arn
}

# Lambda function version
output "lambda_version" {
  value = aws_lambda_function.atot_lambda.version
}

# Lambda function last modified date
output "lambda_last_modified" {
  value = aws_lambda_function.atot_lambda.last_modified
}

# Lambda function role ARN
output "lambda_role_arn" {
  value = aws_lambda_function.atot_lambda.role
}

# Lambda function runtime
output "lambda_runtime" {
  value = aws_lambda_function.atot_lambda.runtime
}