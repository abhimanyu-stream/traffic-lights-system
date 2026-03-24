package com.trafficlight.config;

import com.trafficlight.constants.KafkaConstants;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for Traffic Light Controller system.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    @Value("${spring.kafka.producer.transaction-id-prefix}")
    private String transactionIdPrefix;
    
    /**
     * Kafka Admin configuration.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        return new KafkaAdmin(configs);
    }
    
    /**
     * Producer factory configuration with transactions enabled.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Reliability configuration
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Performance configuration
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, KafkaConstants.PRODUCER_BATCH_SIZE);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, KafkaConstants.PRODUCER_LINGER_MS);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, KafkaConstants.PRODUCER_BUFFER_MEMORY);
        
        // Transaction configuration
        configProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionIdPrefix);
        configProps.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, KafkaConstants.TRANSACTION_TIMEOUT_MS);
        
        // Ordering configuration
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    /**
     * Kafka template with transaction support.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        template.setTransactionIdPrefix(transactionIdPrefix);
        return template;
    }
    
    /**
     * Kafka transaction manager.
     */
    @Bean
    public KafkaTransactionManager kafkaTransactionManager() {
        return new KafkaTransactionManager(producerFactory());
    }
    
    /**
     * Consumer factory configuration with manual acknowledgment.
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Offset management
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Performance configuration
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, KafkaConstants.CONSUMER_MAX_POLL_RECORDS);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, KafkaConstants.CONSUMER_SESSION_TIMEOUT_MS);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, KafkaConstants.CONSUMER_HEARTBEAT_INTERVAL_MS);
        
        // Transaction configuration
        configProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        // JSON deserialization configuration
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.trafficlight.events");
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, 
            "stateChangedEvent:com.trafficlight.events.StateChangedEvent," +
            "intersectionUpdatedEvent:com.trafficlight.events.IntersectionUpdatedEvent," +
            "systemHealthEvent:com.trafficlight.events.SystemHealthEvent");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * Kafka listener container factory with manual acknowledgment.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Manual acknowledgment configuration
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Concurrency configuration
        factory.setConcurrency(3);
        
        // Error handling
        factory.setCommonErrorHandler(kafkaErrorHandler());
        
        // Transaction configuration
        factory.getContainerProperties().setTransactionManager(kafkaTransactionManager());
        
        return factory;
    }
    
    /**
     * Kafka error handler for failed message processing.
     */
    @Bean
    public org.springframework.kafka.listener.DefaultErrorHandler kafkaErrorHandler() {
        return new org.springframework.kafka.listener.DefaultErrorHandler();
    }
    
    /**
     * Consumer factory for analytics processing.
     */
    @Bean
    public ConsumerFactory<String, Object> analyticsConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConstants.GROUP_ANALYTICS_PROCESSOR);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Offset management
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Higher throughput for analytics
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        // JSON deserialization configuration
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.trafficlight.events");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * Kafka listener container factory for analytics processing.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> analyticsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(analyticsConsumerFactory());
        
        // Manual acknowledgment configuration
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Higher concurrency for analytics
        factory.setConcurrency(6);
        
        // Error handling
        factory.setCommonErrorHandler(kafkaErrorHandler());
        
        return factory;
    }
    
    /**
     * Consumer factory for batch processing.
     */
    @Bean
    public ConsumerFactory<String, Object> batchConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConstants.GROUP_BATCH_PROCESSOR);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Offset management
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Batch processing configuration
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 10240);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1000);
        
        // JSON deserialization configuration
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.trafficlight.events");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * Kafka listener container factory for batch processing.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(batchConsumerFactory());
        
        // Manual acknowledgment configuration
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Batch processing configuration
        factory.setBatchListener(true);
        factory.setConcurrency(2);
        
        // Error handling
        factory.setCommonErrorHandler(kafkaErrorHandler());
        
        return factory;
    }
}