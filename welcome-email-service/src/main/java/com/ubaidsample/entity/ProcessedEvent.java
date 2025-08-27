package com.ubaidsample.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "processed_events", indexes = {
        @Index(name = "idx_processed_event_eventId", columnList = "eventId", unique = true)
})
public class ProcessedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private Instant processedAt = Instant.now();
}