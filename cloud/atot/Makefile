REPO_NAME ?= audio_to_text
TAG ?= latest

ECR_URI = $(shell aws sts get-caller-identity --query Account --output text).dkr.ecr.$(shell aws configure get region).amazonaws.com/$(REPO_NAME)

build:
	docker build -t $(REPO_NAME):$(TAG) .

push: build
	aws ecr get-login-password | docker login --username AWS --password-stdin $(ECR_URI)
	docker tag $(REPO_NAME):$(TAG) $(ECR_URI):$(TAG)
	docker push $(ECR_URI):$(TAG)