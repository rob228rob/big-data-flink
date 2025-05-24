package com.mai.flink;

import com.mai.flink.converter.MockRecordConverter;
import com.mai.flink.dto.MockRecordDto;
import com.mai.flink.model.MockRecord;
import com.mai.flink.services.dao.StarSchemeDao;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerFlinkJob {
    private static final Logger log = LoggerFactory.getLogger(ConsumerFlinkJob.class);

    public static void main(String[] args) throws Exception {
        Properties config = loadConfiguration();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        configureAndRunJob(env, config);
    }

    private static Properties loadConfiguration() throws ClassNotFoundException {
        Properties config = new Properties();
        config.setProperty("app.kafka.bootstrap-servers", "bigdata-kafka:9092");
        config.setProperty("app.kafka.consumer-topic", "mock-data-topic");
        config.setProperty("app.flink.out-kafka-topic", "mock-data-topic-resp");
        config.setProperty("db.url", "jdbc:postgresql://bigdata-db:5432/main_db");
        config.setProperty("db.user", "bigdata");
        config.setProperty("db.password", "bigdata");
        return config;
    }

    private static void configureAndRunJob(StreamExecutionEnvironment env, Properties cf) throws Exception {
        String kafkaServers = cf.getProperty("app.kafka.bootstrap-servers", "bigdata-kafka:9092");
        String kafkaConsumerTopic = cf.getProperty("app.kafka.consumer-topic", "mock-data-topic");
        String outKafkaTopic = cf.getProperty("app.flink.out-kafka-topic", "mock-data-topic-resp");

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaServers)
                .setTopics(kafkaConsumerTopic)
                .setGroupId("flink-group-id")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaRecordSerializationSchema<String> recordSerializationSchema =
                KafkaRecordSerializationSchema.builder()
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .setTopic(outKafkaTopic)
                        .build();

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers(kafkaServers)
                .setRecordSerializer(recordSerializationSchema)
                .build();

        DataStream<String> kafkaDataStream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka_Source");

        DataStream<String> processedStream = kafkaDataStream
                .map(new DaoMapper(cf))
                .returns(String.class);

        processedStream.sinkTo(kafkaSink);

        env.execute("Flink-Kafka-to-Star-Schema-Job");
    }

    public static class DaoMapper extends RichMapFunction<String, String> {
        private transient StarSchemeDao dao;
        private transient ObjectMapper mapper;
        private transient HikariDataSource dataSource;

        private final Properties config;

        public DaoMapper(Properties config) {
            this.config = config;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            mapper = new ObjectMapper();
            Class.forName("org.postgresql.Driver");
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getProperty("db.url"));
            hikariConfig.setUsername(config.getProperty("db.user"));
            hikariConfig.setPassword(config.getProperty("db.password"));
            hikariConfig.setMaximumPoolSize(5);
            hikariConfig.setConnectionTimeout(3000);
            hikariConfig.setLeakDetectionThreshold(6000);
            dataSource = new HikariDataSource(hikariConfig);
            dao = new StarSchemeDao(dataSource);
            try (Connection conn = dataSource.getConnection()) {
                log.info("Successfully initialized database connection");
            } catch (Exception e) {
                log.error("Failed to initialize database connection", e);
                throw new RuntimeException("Database initialization failed", e);
            }
        }

        @Override
        public String map(String json) {
            if (json == null || json.isEmpty()) {
                return "Json is null or emtpy";
            }

            try {
                var recordDto = mapper.readValue(json, MockRecordDto.class);
                MockRecord mockRecord = MockRecordConverter.fromDto(recordDto);
                dao.process(mockRecord);
                return json;
            } catch (Exception e) {
                log.error("Error processing record: {}", json, e);
                return e.getMessage() + "stacktrace: " + Arrays.toString(e.getStackTrace());
            }
        }

        @Override
        public void close() throws Exception {
            if (dataSource != null) {
                dataSource.close();
                log.info("Database connection pool closed");
            }
        }
    }

    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private int status;
    }
}