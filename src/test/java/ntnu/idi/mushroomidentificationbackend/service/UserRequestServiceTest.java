package ntnu.idi.mushroomidentificationbackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import ntnu.idi.mushroomidentificationbackend.dto.request.ChangeRequestStatusDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserRequestServiceTest {

  @Autowired
  private UserRequestService userRequestService;

  @Autowired
  private UserRequestRepository userRequestRepository;

  @Test
  void changeRequestStatus_throwsExceptionIfUserRequestNotFound() {
    ChangeRequestStatusDTO dto = new ChangeRequestStatusDTO();
    dto.setUserRequestId("99999"); 
    dto.setNewStatus(UserRequestStatus.PENDING);

    Exception exception = assertThrows(RuntimeException.class, () -> userRequestService.changeRequestStatus(dto));
    assertTrue(exception.getMessage().contains("User request not found"));
  }
}
