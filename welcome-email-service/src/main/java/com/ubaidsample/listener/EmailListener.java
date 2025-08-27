package com.ubaidsample.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubaidsample.event.UserRegisteredEvent;
import com.ubaidsample.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailListener {

    private final EmailService service;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic.new-user-events:new-user-events}", groupId = "welcome-email-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String,String> record, Acknowledgment ack) {
        String payload = record.value();
        try {
            UserRegisteredEvent event = objectMapper.readValue(payload, UserRegisteredEvent.class);

            // Optional: extract correlation id header
            Header hdr = record.headers().lastHeader("eventId");
            if (hdr != null) {
                MDC.put("eventId", new String(hdr.value(), StandardCharsets.UTF_8));
            }

            service.handle(event);
            ack.acknowledge(); // if using MANUAL ack mode. If not using ack mode, remove.
        } catch (Exception ex) {
            log.error("Failed processing message {} from topic {}", record.offset(), record.topic(), ex);
            throw new RuntimeException(ex); // will be handled by DefaultErrorHandler (retries -> DLT)
        } finally {
            MDC.clear();
        }
    }
}
