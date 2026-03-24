package com.trafficlight.controller;

import com.trafficlight.constants.ApiConstants;
import com.trafficlight.dto.response.ApiResponse;
import com.trafficlight.utils.ResponseBuilder;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * REST Controller for Kafka Admin operations and topic data access.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-28
 */
@RestController
@RequestMapping(ApiConstants.TRAFFIC_SERVICE_BASE + "/kafka")
@Validated
public class KafkaAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaAdminController.class);
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String defaultGroupId;
    
    private final KafkaAdmin kafkaAdmin;
    
    @Autowired
    public KafkaAdminController(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }
    
    /**
     * Get all Kafka topics.
     * 
     * @return list of topic names
     */
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> getTopics() {
        logger.info("Getting all Kafka topics");
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> topicNames = adminClient.listTopics().names().get();
            List<String> sortedTopics = new ArrayList<>(topicNames);
            Collections.sort(sortedTopics);
            
            logger.info("Found {} Kafka topics", sortedTopics.size());
            return ResponseBuilder.success(sortedTopics);
            
        } catch (Exception e) {
            logger.error("Error getting Kafka topics", e);
            return ResponseBuilder.success(Arrays.asList("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get topic metadata.
     * 
     * @param topicName the topic name
     * @return topic metadata
     */
    @GetMapping("/topics/{topicName}/metadata")
    public ResponseEntity<ApiResponse<Object>> getTopicMetadata(@PathVariable String topicName) {
        logger.info("Getting metadata for topic: {}", topicName);
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Map<String, TopicDescription> descriptions = adminClient.describeTopics(Collections.singletonList(topicName)).all().get();
            TopicDescription description = descriptions.get(topicName);
            
            if (description == null) {
                Map<String, Object> notFound = new HashMap<>();
                notFound.put("topicName", topicName);
                notFound.put("message", "Topic not found");
                notFound.put("exists", false);
                notFound.put("queriedAt", LocalDateTime.now());
                return ResponseBuilder.success(notFound);
            }
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("name", description.name());
            metadata.put("isInternal", description.isInternal());
            metadata.put("partitionCount", description.partitions().size());
            metadata.put("partitions", description.partitions().toString());
            metadata.put("queriedAt", LocalDateTime.now());
            metadata.put("exists", true);
            metadata.put("message", "Topic metadata retrieved successfully");
            
            return ResponseBuilder.success(metadata);
            
        } catch (Exception e) {
            logger.error("Error getting topic metadata for: {}", topicName, e);
            Map<String, Object> error = new HashMap<>();
            error.put("topicName", topicName);
            error.put("message", "Error retrieving topic metadata: " + e.getMessage());
            error.put("exists", false);
            error.put("queriedAt", LocalDateTime.now());
            return ResponseBuilder.success(error);
        }
    }
    
    /**
     * Get messages from a topic (latest messages).
     * 
     * @param topicName the topic name
     * @param limit maximum number of messages to retrieve (default: 10)
     * @return list of messages
     */
    @GetMapping("/topics/{topicName}/messages")
    public ResponseEntity<ApiResponse<List<Object>>> getTopicMessages(
            @PathVariable String topicName,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Getting latest {} messages from topic: {}", limit, topicName);
        
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-admin-reader-" + System.currentTimeMillis());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, limit);
        
        List<Object> messages = new ArrayList<>();
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            // Get topic partitions
            List<TopicPartition> partitions = consumer.partitionsFor(topicName)
                .stream()
                .map(partitionInfo -> new TopicPartition(topicName, partitionInfo.partition()))
                .collect(Collectors.toList());
            
            if (partitions.isEmpty()) {
                Map<String, Object> noPartitions = new HashMap<>();
                noPartitions.put("topicName", topicName);
                noPartitions.put("message", "Topic not found or has no partitions");
                noPartitions.put("messageCount", 0);
                noPartitions.put("queriedAt", LocalDateTime.now());
                messages.add(noPartitions);
                return ResponseBuilder.success(messages);
            }
            
            consumer.assign(partitions);
            
            // Seek to end and then back to get latest messages
            consumer.seekToEnd(partitions);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            
            for (TopicPartition partition : partitions) {
                long endOffset = endOffsets.get(partition);
                long startOffset = Math.max(0, endOffset - limit);
                consumer.seek(partition, startOffset);
            }
            
            // Poll for messages
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
            
            for (ConsumerRecord<String, String> record : records) {
                Map<String, Object> message = new HashMap<>();
                message.put("topic", record.topic());
                message.put("partition", record.partition());
                message.put("offset", record.offset());
                message.put("key", record.key());
                message.put("value", record.value());
                message.put("timestamp", record.timestamp());
                message.put("receivedAt", LocalDateTime.now());
                messages.add(message);
            }
            
            if (messages.isEmpty()) {
                Map<String, Object> noMessages = new HashMap<>();
                noMessages.put("topicName", topicName);
                noMessages.put("message", "No messages found in topic");
                noMessages.put("messageCount", 0);
                noMessages.put("queriedAt", LocalDateTime.now());
                messages.add(noMessages);
            }
            
            logger.info("Retrieved {} messages from topic: {}", messages.size(), topicName);
            return ResponseBuilder.success(messages);
            
        } catch (Exception e) {
            logger.error("Error getting messages from topic: {}", topicName, e);
            Map<String, Object> error = new HashMap<>();
            error.put("topicName", topicName);
            error.put("message", "Error retrieving messages: " + e.getMessage());
            error.put("messageCount", 0);
            error.put("queriedAt", LocalDateTime.now());
            messages.add(error);
            return ResponseBuilder.success(messages);
        }
    }
    
    /**
     * Get all consumer groups.
     * 
     * @return list of consumer group names
     */
    @GetMapping("/consumer-groups")
    public ResponseEntity<ApiResponse<List<String>>> getConsumerGroups() {
        logger.info("Getting all Kafka consumer groups");
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Collection<ConsumerGroupListing> groups = adminClient.listConsumerGroups().all().get();
            List<String> groupNames = groups.stream()
                .map(ConsumerGroupListing::groupId)
                .sorted()
                .collect(Collectors.toList());
            
            logger.info("Found {} consumer groups", groupNames.size());
            return ResponseBuilder.success(groupNames);
            
        } catch (Exception e) {
            logger.error("Error getting consumer groups", e);
            return ResponseBuilder.success(Arrays.asList("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get consumer group details.
     * 
     * @param groupId the consumer group ID
     * @return consumer group details
     */
    @GetMapping("/consumer-groups/{groupId}")
    public ResponseEntity<ApiResponse<Object>> getConsumerGroupDetails(@PathVariable String groupId) {
        logger.info("Getting details for consumer group: {}", groupId);
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Map<String, ConsumerGroupDescription> descriptions = adminClient.describeConsumerGroups(Collections.singletonList(groupId)).all().get();
            ConsumerGroupDescription description = descriptions.get(groupId);
            
            if (description == null) {
                Map<String, Object> notFound = new HashMap<>();
                notFound.put("groupId", groupId);
                notFound.put("message", "Consumer group not found");
                notFound.put("exists", false);
                notFound.put("queriedAt", LocalDateTime.now());
                return ResponseBuilder.success(notFound);
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("groupId", description.groupId());
            details.put("state", description.state().toString());
            details.put("partitionAssignor", description.partitionAssignor());
            details.put("memberCount", description.members().size());
            details.put("coordinator", description.coordinator().toString());
            details.put("isSimpleConsumerGroup", description.isSimpleConsumerGroup());
            details.put("queriedAt", LocalDateTime.now());
            details.put("exists", true);
            details.put("message", "Consumer group details retrieved successfully");
            
            return ResponseBuilder.success(details);
            
        } catch (Exception e) {
            logger.error("Error getting consumer group details for: {}", groupId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("groupId", groupId);
            error.put("message", "Error retrieving consumer group details: " + e.getMessage());
            error.put("exists", false);
            error.put("queriedAt", LocalDateTime.now());
            return ResponseBuilder.success(error);
        }
    }
    
    /**
     * Get Kafka cluster information.
     * 
     * @return cluster information
     */
    @GetMapping("/cluster")
    public ResponseEntity<ApiResponse<Object>> getClusterInfo() {
        logger.info("Getting Kafka cluster information");
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            var clusterDescription = adminClient.describeCluster();
            var clusterId = clusterDescription.clusterId().get();
            var nodes = clusterDescription.nodes().get();
            var controller = clusterDescription.controller().get();
            
            Map<String, Object> clusterInfo = new HashMap<>();
            clusterInfo.put("clusterId", clusterId);
            clusterInfo.put("nodeCount", nodes.size());
            clusterInfo.put("controllerNode", controller.toString());
            clusterInfo.put("bootstrapServers", bootstrapServers);
            clusterInfo.put("queriedAt", LocalDateTime.now());
            clusterInfo.put("message", "Cluster information retrieved successfully");
            
            return ResponseBuilder.success(clusterInfo);
            
        } catch (Exception e) {
            logger.error("Error getting cluster information", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Error retrieving cluster information: " + e.getMessage());
            error.put("bootstrapServers", bootstrapServers);
            error.put("queriedAt", LocalDateTime.now());
            return ResponseBuilder.success(error);
        }
    }
    
    /**
     * Get topic statistics.
     * 
     * @param topicName the topic name
     * @return topic statistics
     */
    @GetMapping("/topics/{topicName}/stats")
    public ResponseEntity<ApiResponse<Object>> getTopicStatistics(@PathVariable String topicName) {
        logger.info("Getting statistics for topic: {}", topicName);
        
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-stats-reader-" + System.currentTimeMillis());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            List<TopicPartition> partitions = consumer.partitionsFor(topicName)
                .stream()
                .map(partitionInfo -> new TopicPartition(topicName, partitionInfo.partition()))
                .collect(Collectors.toList());
            
            if (partitions.isEmpty()) {
                Map<String, Object> notFound = new HashMap<>();
                notFound.put("topicName", topicName);
                notFound.put("message", "Topic not found");
                notFound.put("exists", false);
                notFound.put("queriedAt", LocalDateTime.now());
                return ResponseBuilder.success(notFound);
            }
            
            consumer.assign(partitions);
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            
            long totalMessages = 0;
            for (TopicPartition partition : partitions) {
                long beginning = beginningOffsets.get(partition);
                long end = endOffsets.get(partition);
                totalMessages += (end - beginning);
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("topicName", topicName);
            stats.put("partitionCount", partitions.size());
            stats.put("totalMessages", totalMessages);
            stats.put("beginningOffsets", beginningOffsets.toString());
            stats.put("endOffsets", endOffsets.toString());
            stats.put("queriedAt", LocalDateTime.now());
            stats.put("exists", true);
            stats.put("message", "Topic statistics retrieved successfully");
            
            return ResponseBuilder.success(stats);
            
        } catch (Exception e) {
            logger.error("Error getting topic statistics for: {}", topicName, e);
            Map<String, Object> error = new HashMap<>();
            error.put("topicName", topicName);
            error.put("message", "Error retrieving topic statistics: " + e.getMessage());
            error.put("exists", false);
            error.put("queriedAt", LocalDateTime.now());
            return ResponseBuilder.success(error);
        }
    }
}