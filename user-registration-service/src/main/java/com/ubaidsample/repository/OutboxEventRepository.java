package com.ubaidsample.repository;

import com.ubaidsample.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(String status);
    Optional<OutboxEvent> findByEventId(String eventId);
}