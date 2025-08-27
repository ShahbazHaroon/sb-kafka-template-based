package com.ubaidsample.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private String eventId;
    private Long userId;
    private String name;
    private String email;
    private Instant occurredAt;
    private String version = "1";
}