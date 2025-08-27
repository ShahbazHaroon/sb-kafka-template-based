package com.ubaidsample.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status", columnList = "status"),
        @Index(name = "idx_outbox_eventId", columnList = "eventId", unique = true)
})
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String topic;

    @Lob
    @Column(nullable = false)
    private String payload; // JSON

    @Column(nullable = false)
    private String status; // PENDING, SENT, FAILED

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant sentAt;
}
