package com.bugtracker.backend.testcase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record TestCaseRequest(@Size(max=255) String title, String description,
 @Pattern(regexp="pending|passed|failed|blocked") String status,
 @Pattern(regexp="low|medium|high|critical") String priority,
 String expectedResult, List<@NotNull @Valid Step> steps) {}
