package com.bugtracker.backend.testcase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Embeddable
public class Step {
 @Column(name="step_num",nullable=false) @Positive public int stepNum;
 @Column(nullable=false,columnDefinition="TEXT") @NotBlank public String action;
 @Column(name="expected_result",nullable=false,columnDefinition="TEXT") @NotBlank public String expectedResult;
 public Step() {}
}
