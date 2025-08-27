package com.ubaidsample.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubaidsample.dto.request.UserRequest;
import com.ubaidsample.entity.OutboxEvent;
import com.ubaidsample.entity.User;
import com.ubaidsample.event.UserRegisteredEvent;
import com.ubaidsample.repository.OutboxEventRepository;
import com.ubaidsample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.new-user-events:new-user-events}")
    private String topic;

    @Transactional
    public User register(UserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        User saved = userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(saved.getId());
        event.setName(saved.getName());
        event.setEmail(saved.getEmail());
        event.setOccurredAt(Instant.now());

        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent oe = new OutboxEvent();
            oe.setEventId(event.getEventId());
            oe.setTopic(topic);
            oe.setPayload(payload);
            oe.setStatus("PENDING");
            outboxRepo.save(oe);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize outbox event", e);
        }

        return saved;
    }
}
