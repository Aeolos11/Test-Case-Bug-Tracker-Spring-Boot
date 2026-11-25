package com.bugtracker.backend.events;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.slf4j.LoggerFactory;
@Component
public class StatusEventMessaging {
 private final KafkaTemplate<String,String> kafka;
 private final JdbcTemplate jdbc;
 public StatusEventMessaging(KafkaTemplate<String,String> kafka,JdbcTemplate jdbc){this.kafka=kafka;this.jdbc=jdbc;}
 @Bean NewTopic testCaseTopic(){return TopicBuilder.name("test-case-events").partitions(1).replicas(1).build();}
 @TransactionalEventListener
 public void publish(StatusEvent e){
  String payload=String.join("|",e.eventId().toString(),e.testCaseId().toString(),e.oldStatus(),e.newStatus(),e.actorId().toString(),e.occurredAt().toString());
  kafka.send("test-case-events",e.testCaseId().toString(),payload).whenComplete((result,error)->{if(error!=null)LoggerFactory.getLogger(getClass()).error("Status event delivery failed",error);});
 }
 @KafkaListener(topics="test-case-events")
 public void consume(String payload){
  String[] p=payload.split("\\|",-1);
  if(p.length!=6)throw new IllegalArgumentException("Invalid event");
  jdbc.update("INSERT INTO test_case_events(event_id,test_case_id,old_status,new_status,actor_id,occurred_at) VALUES (?,?,?,?,?,?) ON CONFLICT(event_id) DO NOTHING",UUID.fromString(p[0]),UUID.fromString(p[1]),p[2],p[3],UUID.fromString(p[4]),OffsetDateTime.ofInstant(Instant.parse(p[5]),ZoneOffset.UTC));
 }
}
