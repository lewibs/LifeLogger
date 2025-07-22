FROM public.ecr.aws/lambda/python:3.11

RUN pip install --upgrade pip

# Copy requirements and install Python dependencies
COPY requirements.txt ${LAMBDA_TASK_ROOT}
RUN pip install --no-cache-dir -r /app/requirements.txt

# Copy function code
COPY lambda.py ${LAMBDA_TASK_ROOT}

# Set the CMD to your handler
CMD ["lambda.handler"]