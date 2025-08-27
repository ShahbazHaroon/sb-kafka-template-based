package com.ubaidsample.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubaidsample.entity.OutboxEvent;
import com.ubaidsample.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Simple scheduled poller - tune schedule in prod or use Debezium-based CDC
    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:2000}")
    public void publishPending() {
        List<OutboxEvent> pendings = outboxRepo.findTop100ByStatusOrderByCreatedAtAsc("PENDING");
        for (OutboxEvent oe : pendings) {
            try {
                // Optionally set key (userId) by parsing payload
                JsonNode root = objectMapper.readTree(oe.getPayload());
                String key = root.has("userId") ? root.get("userId").asText() : null;
                ProducerRecord<String,String> record = new ProducerRecord<>(oe.getTopic(), key, oe.getPayload());
                // Add correlation id header
                record.headers().add(new RecordHeader("eventId", oe.getEventId().getBytes(StandardCharsets.UTF_8)));

                ListenableFuture<SendResult<String,String>> fut = kafkaTemplate.send(record);
                fut.addCallback(result -> {
                    oe.setStatus("SENT");
                    oe.setSentAt(Instant.now());
                    outboxRepo.save(oe);
                    log.info("Outbox event {} sent to topic {}", oe.getEventId(), oe.getTopic());
                }, ex -> {
                    log.error("Failed sending outbox event {} - will retry later", oe.getEventId(), ex);
                    oe.setStatus("FAILED");
                    outboxRepo.save(oe);
                });

            } catch (Exception ex) {
                log.error("Error publishing outbox event id=" + oe.getEventId(), ex);
                oe.setStatus("FAILED");
                outboxRepo.save(oe);
            }
        }
    }
}