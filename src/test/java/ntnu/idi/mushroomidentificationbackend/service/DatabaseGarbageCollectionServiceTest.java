package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class DatabaseGarbageCollectionServiceTest {

  private AdminRepository adminRepository;
  private MessageRepository messageRepository;
  private UserRequestRepository userRequestRepository;
  private GarbageCollectionService garbageService;

  @BeforeEach
  void setUp() {
    adminRepository = mock(AdminRepository.class);
    messageRepository = mock(MessageRepository.class);
    userRequestRepository = mock(UserRequestRepository.class);
    garbageService = new GarbageCollectionService(userRequestRepository);
  }

  @Test
  void deleteOutdatedData_executesWithoutErrorAndLogsCounts() {
    when(messageRepository.deleteByCreatedAtBefore(any())).thenReturn(3);
    when(userRequestRepository.deleteByCreatedAtBefore(any())).thenReturn(5);

    garbageService.deleteOutdatedData(6);

    verify(messageRepository, times(1)).deleteByCreatedAtBefore(any());
    verify(userRequestRepository, times(1)).deleteByCreatedAtBefore(any());
  }

  @Test
  void deleteOutdatedData_handlesExceptionGracefully() {
    when(messageRepository.deleteByCreatedAtBefore(any())).thenThrow(new RuntimeException("fail"));

    garbageService.deleteOutdatedData(6);
    verify(messageRepository).deleteByCreatedAtBefore(any());
  }
}
