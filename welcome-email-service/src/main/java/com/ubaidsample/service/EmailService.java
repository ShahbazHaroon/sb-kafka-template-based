package com.ubaidsample.service;

import com.ubaidsample.entity.ProcessedEvent;
import com.ubaidsample.event.UserRegisteredEvent;
import com.ubaidsample.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ProcessedEventRepository processedRepo;

    public void handle(UserRegisteredEvent event) {
        if (processedRepo.existsByEventId(event.getEventId())) {
            log.info("Event {} already processed; skipping", event.getEventId());
            return;
        }

        // Simulate email sending - replace with JavaMailSender or external email client
        try {
            log.info("Sending welcome email to {} <{}>", event.getName(), event.getEmail());
            // TODO: actual send logic here (JavaMailSender) with retry / async as needed

            // After successful send persist processed event
            ProcessedEvent pe = new ProcessedEvent();
            pe.setEventId(event.getEventId());
            processedRepo.save(pe);

        } catch (Exception ex) {
            log.error("Failed to send welcome email for event {}", event.getEventId(), ex);
            throw ex; // allow error handler to kick in (retry / DLT)
        }
    }
}
