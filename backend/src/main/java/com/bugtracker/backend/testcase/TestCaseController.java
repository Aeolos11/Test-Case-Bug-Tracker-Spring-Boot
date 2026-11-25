package com.bugtracker.backend.testcase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api")
public class TestCaseController {
 private final TestCaseService service;
 public TestCaseController(TestCaseService service){this.service=service;}
 private UUID user(Jwt jwt){return UUID.fromString(jwt.getSubject());}
 @PostMapping("/projects/{id}/create-testcase") @ResponseStatus(HttpStatus.CREATED)
 public Map<String,String> create(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody TestCaseRequest q){return service.create(id,user(jwt),q);}
 @GetMapping("/projects/{id}/testcases") public List<TestCaseResponse> list(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt){return service.list(id,user(jwt));}
 @GetMapping("/testcases/{id}") public TestCaseResponse get(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt){return service.get(id,user(jwt));}
 @PutMapping("/testcases/{id}") public TestCaseResponse update(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody TestCaseRequest q){return service.update(id,user(jwt),q);}
 @DeleteMapping("/testcases/{id}") public Map<String,String> delete(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt){return service.delete(id,user(jwt));}
}
