package com.bugtracker.backend.testcase;
import com.bugtracker.backend.project.*;
import com.bugtracker.backend.user.User;
import com.bugtracker.backend.events.StatusEvent;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class TestCaseServiceTest {
 TestCaseRepository cases=mock(TestCaseRepository.class);
 ProjectRepository projects=mock(ProjectRepository.class);
 ApplicationEventPublisher events=mock(ApplicationEventPublisher.class);
 TestCaseService service=new TestCaseService(cases,projects,events);
 UUID owner=UUID.randomUUID(),projectId=UUID.randomUUID(),caseId=UUID.randomUUID();
 TestCase c=new TestCase();
 @BeforeEach void setup(){User u=mock(User.class);when(u.getId()).thenReturn(owner);when(projects.findById(projectId)).thenReturn(Optional.of(new Project("P","",u)));c.id=caseId;c.project=projectId;c.title="T";c.expectedResult="OK";c.status="pending";c.priority="medium";when(cases.findById(caseId)).thenReturn(Optional.of(c));}
 @Test void ownerCreates(){service.create(projectId,owner,new TestCaseRequest("T",null,null,null,"OK",null));verify(cases).save(any());}
 @Test void outsiderDenied(){assertEquals(HttpStatus.FORBIDDEN,assertThrows(ResponseStatusException.class,()->service.get(caseId,UUID.randomUUID())).getStatusCode());}
 @Test void statusChangePublishes(){service.update(caseId,owner,new TestCaseRequest(null,null,"passed",null,null,null));verify(events).publishEvent(any(StatusEvent.class));assertEquals("passed",c.status);}
 @Test void unchangedStatusDoesNotPublish(){service.update(caseId,owner,new TestCaseRequest(null,null,"pending",null,null,null));verifyNoInteractions(events);}
 @Test void ownerDeletes(){service.delete(caseId,owner);verify(cases).delete(c);}
}
