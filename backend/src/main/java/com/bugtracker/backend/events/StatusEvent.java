package com.bugtracker.backend.events;
import java.time.Instant;
import java.util.UUID;
public record StatusEvent(UUID eventId,UUID testCaseId,String oldStatus,String newStatus,UUID actorId,Instant occurredAt) {}
