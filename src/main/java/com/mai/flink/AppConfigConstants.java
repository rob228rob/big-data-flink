package com.mai.flink;

/**
 * я бы очень хотел грузить все из json/yaml конфига, но честно
 * стало очень лень дописывать классы для парсинга конфига так что вот)
 * @author batoyan.rl
 * @since 25.05.2025
 */
public interface AppConfigConstants {

    String KAFKA_BOOTSTRAP_SERVERS = "bigdata-kafka:9092";
    String KAFKA_CONSUMER_TOPIC = "mock-data-topic";
    String KAFKA_OUT_TOPIC = "mock-data-topic-resp";

    String DB_HOST = "bigdata-db";
    String DB_PORT = "5432";
    String DB_NAME = "main_db";
    String DB_USER = "bigdata";
    String DB_PASSWORD = "bigdata";

    String DB_URL = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
}