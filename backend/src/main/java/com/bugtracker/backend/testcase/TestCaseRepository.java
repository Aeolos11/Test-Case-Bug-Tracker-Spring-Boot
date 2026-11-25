package com.bugtracker.backend.testcase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TestCaseRepository extends JpaRepository<TestCase,UUID> {
 List<TestCase> findByProjectOrderByCreatedAtDesc(UUID project);
}
