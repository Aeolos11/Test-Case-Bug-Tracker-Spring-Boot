package com.bugtracker.backend.testcase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
@Entity
@Table(name="test_cases")
public class TestCase {
 @Id @GeneratedValue(strategy=GenerationType.UUID) UUID id;
 @Column(nullable=false) String title;
 @Column(columnDefinition="TEXT") String description;
 @Column(nullable=false,length=20) String status;
 @Column(nullable=false,length=20) String priority;
 @Column(name="expected_result",nullable=false,columnDefinition="TEXT") String expectedResult;
 @Column(name="project_id",nullable=false) UUID project;
 @Column(name="created_by",nullable=false) UUID createdBy;
 @Column(name="created_at",nullable=false) Instant createdAt;
 @Column(name="updated_at",nullable=false) Instant updatedAt;
 @ElementCollection @CollectionTable(name="test_steps",joinColumns=@JoinColumn(name="test_case_id"))
 @OrderColumn(name="position") List<Step> steps=new ArrayList<>();
 protected TestCase() {}
 @PrePersist void createTime(){createdAt=updatedAt=Instant.now();}
 @PreUpdate void updateTime(){updatedAt=Instant.now();}
}
