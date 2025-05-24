FROM python:3.9-slim
#
#RUN mkdir -p /opt/data /app

WORKDIR /app

COPY ./python_producer/requirements.txt .
COPY ./python_producer/mock_data_producer.py .

RUN pip install --no-cache-dir -r requirements.txt

COPY ./source_data /opt/data/source_data
ENV CSV_PATH=/opt/data/source_data

ENV KAFKA_BOOTSTRAP_SERVERS=bigdata-kafka:9092

CMD ["sh", "-c", "test -d ${CSV_PATH} && python mock_data_producer.py || echo 'Error: Data directory not found' && exit 1"]