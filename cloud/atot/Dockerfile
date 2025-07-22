FROM public.ecr.aws/lambda/python:3.11

RUN yum update -y && yum install -y gcc gcc-c++ make

RUN pip install --upgrade pip

# Force install CPU-only versions with specific constraints
RUN pip install --no-cache-dir \
    --extra-index-url https://download.pytorch.org/whl/cpu \
    torch==2.0.0+cpu \
    torchaudio==2.0.0+cpu

# Install other dependencies without allowing CUDA variants
RUN pip install --no-cache-dir \
    numpy==1.24.3 \
    tiktoken \
    regex \
    tqdm \
    more-itertools \
    ffmpeg-python

# Install whisper without dependencies to avoid conflicts
RUN pip install --no-cache-dir --no-deps openai-whisper

COPY lambda.py ${LAMBDA_TASK_ROOT}
CMD ["lambda.handler"]