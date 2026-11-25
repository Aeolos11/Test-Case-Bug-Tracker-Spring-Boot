package com.bugtracker.backend.testcase;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.*;
public record TestCaseResponse(@JsonProperty("_id") UUID id, String title,
 String description,String status,String priority,String expectedResult,
 UUID project,UUID createdBy,Instant createdAt,Instant updatedAt,List<Step> steps) {
 static TestCaseResponse from(TestCase c){return new TestCaseResponse(c.id,c.title,c.description,c.status,c.priority,c.expectedResult,c.project,c.createdBy,c.createdAt,c.updatedAt,List.copyOf(c.steps));}
}
