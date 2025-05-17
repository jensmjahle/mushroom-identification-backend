package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.mockito.Mockito.*;

class DatabaseGarbageCollectionServiceTest {

  private AdminRepository adminRepository;
  private MessageRepository messageRepository;
  private UserRequestRepository userRequestRepository;
  private DatabaseGarbageCollectionService garbageService;

  @BeforeEach
  void setUp() {
    adminRepository = mock(AdminRepository.class);
    messageRepository = mock(MessageRepository.class);
    userRequestRepository = mock(UserRequestRepository.class);
    garbageService = new DatabaseGarbageCollectionService(adminRepository, messageRepository, userRequestRepository);
  }

  @Test
  void deleteOutdatedData_executesWithoutErrorAndLogsCounts() {
    when(messageRepository.deleteByCreatedAtBefore(any())).thenReturn(3);
    when(userRequestRepository.deleteByCreatedAtBefore(any())).thenReturn(5);

    garbageService.deleteOutdatedData();

    verify(messageRepository, times(1)).deleteByCreatedAtBefore(any());
    verify(userRequestRepository, times(1)).deleteByCreatedAtBefore(any());
  }

  @Test
  void deleteOutdatedData_handlesExceptionGracefully() {
    when(messageRepository.deleteByCreatedAtBefore(any())).thenThrow(new RuntimeException("fail"));

    garbageService.deleteOutdatedData();
    verify(messageRepository).deleteByCreatedAtBefore(any());
  }
}
