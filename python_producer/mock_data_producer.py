import os
import glob
import csv
import json
import time
import logging
from kafka import KafkaProducer
from kafka.errors import NoBrokersAvailable

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger('kafka-producer')

def snake_to_camel(snake_str: str) -> str:
    parts = snake_str.split('_')
    return parts[0].lower() + ''.join(word.capitalize() for word in parts[1:])

def convert_keys_to_camelcase(data: dict) -> dict:
    return {snake_to_camel(k): v for k, v in data.items()}

def produce_data():
    try:
        KAFKA_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'bigdata-kafka:9092').split(',')
        TOPIC = os.getenv('KAFKA_TOPIC', 'mock-data-topic')
        DATA_PATH = os.getenv('CSV_PATH', '/opt/data/source_data')

        producer = None
        for attempt in range(5):
            try:
                producer = KafkaProducer(
                    bootstrap_servers=KAFKA_SERVERS,
                    value_serializer=lambda v: json.dumps(v).encode('utf-8'))
                logger.info("Connected to Kafka brokers: %s", KAFKA_SERVERS)
                break
            except NoBrokersAvailable as e:
                if attempt == 4:
                    raise
                logger.warning("Attempt %d/5: Kafka connection failed, retrying...", attempt + 1)
                time.sleep(5)

        for csv_file in glob.glob(os.path.join(DATA_PATH, '*.csv')):
            logger.info("Processing file: %s", csv_file)
            with open(csv_file, 'r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    camel_case_row = convert_keys_to_camelcase(row)
                    producer.send(TOPIC, value=camel_case_row)
            producer.flush()

        logger.info("Data production completed")

    except Exception as e:
        logger.exception("Fatal error in producer: ")
        raise
    finally:
        if producer:
            producer.close()


if __name__ == '__main__':
    produce_data()
