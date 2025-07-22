  # 1. Authenticate Docker with ECR
  aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 715006185945.dkr.ecr.us-west-2.amazonaws.com

  # 2. Build the Docker image
  docker build -t atot-lambda .

  # 3. Tag for ECR
  docker tag atot-lambda:latest 715006185945.dkr.ecr.us-west-2.amazonaws.com/atot-lambda:latest

  # 4. Push to ECR
  docker push 715006185945.dkr.ecr.us-west-2.amazonaws.com/atot-lambda:latest

  # 5. Then run terraform apply
  terraform apply -auto-approve
