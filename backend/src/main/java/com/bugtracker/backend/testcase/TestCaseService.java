package com.bugtracker.backend.testcase;
import com.bugtracker.backend.project.*;
import com.bugtracker.backend.events.StatusEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.*;
@Service @Transactional
public class TestCaseService {
 private final TestCaseRepository cases;
 private final ProjectRepository projects;
 private final ApplicationEventPublisher events;
 public TestCaseService(TestCaseRepository cases,ProjectRepository projects,ApplicationEventPublisher events){this.cases=cases;this.projects=projects;this.events=events;}
 private void access(UUID projectId,UUID user,boolean ownerOnly){
  Project p=projects.findById(projectId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found"));
  if(p.getOwner().getId().equals(user)) return;
  if(!ownerOnly && p.getMembers().stream().anyMatch(m->m.getId().equals(user))) return;
  throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No access to this project");
 }
 private TestCase find(UUID id){return cases.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Test case not found"));}
 private void apply(TestCase c,TestCaseRequest q){
  if(q.title()!=null)c.title=q.title().trim();
  if(q.description()!=null)c.description=q.description();
  if(q.status()!=null)c.status=q.status();
  if(q.priority()!=null)c.priority=q.priority();
  if(q.expectedResult()!=null)c.expectedResult=q.expectedResult();
  if(q.steps()!=null){c.steps.clear();c.steps.addAll(q.steps());}
  if(c.title==null || c.title.isBlank() || c.expectedResult==null || c.expectedResult.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Title and expected result are required");
 }
 public Map<String,String> create(UUID project,UUID user,TestCaseRequest q){
  access(project,user,false);TestCase c=new TestCase();c.project=project;c.createdBy=user;c.status="pending";c.priority="medium";apply(c,q);cases.save(c);
  return Map.of("message","Test Case \""+c.title+"\" has been successfully created");
 }
 @Transactional(readOnly=true) public List<TestCaseResponse> list(UUID project,UUID user){access(project,user,false);return cases.findByProjectOrderByCreatedAtDesc(project).stream().map(TestCaseResponse::from).toList();}
 @Transactional(readOnly=true) public TestCaseResponse get(UUID id,UUID user){TestCase c=find(id);access(c.project,user,false);return TestCaseResponse.from(c);}
 public TestCaseResponse update(UUID id,UUID user,TestCaseRequest q){
  TestCase c=find(id);access(c.project,user,false);String old=c.status;apply(c,q);c.updatedAt=Instant.now();cases.saveAndFlush(c);
  if(!old.equals(c.status))events.publishEvent(new StatusEvent(UUID.randomUUID(),id,old,c.status,user,Instant.now()));
  return TestCaseResponse.from(c);
 }
 public Map<String,String> delete(UUID id,UUID user){TestCase c=find(id);access(c.project,user,true);cases.delete(c);return Map.of("message","Test case successfully deleted");}
}
