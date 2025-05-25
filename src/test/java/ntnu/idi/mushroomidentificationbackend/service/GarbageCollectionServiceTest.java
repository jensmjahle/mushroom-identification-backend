package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GarbageCollectionServiceTest {

  private UserRequestRepository userRequestRepository;
  private GarbageCollectionService garbageService;

  @BeforeEach
  void setUp() {
    userRequestRepository = mock(UserRequestRepository.class);
    garbageService = new GarbageCollectionService(userRequestRepository);
  }

  @Test
  void deleteOutdatedData_executesWithoutErrorAndLogsCounts() {
    when(userRequestRepository.findByCreatedAtBefore(any(Date.class)))
        .thenReturn(Collections.emptyList());
    when(userRequestRepository.deleteByCreatedAtBefore(any(Date.class)))
        .thenReturn(5);

    garbageService.deleteOutdatedData(6);

    verify(userRequestRepository, times(1))
        .findByCreatedAtBefore(any(Date.class));
    verify(userRequestRepository, times(1))
        .deleteByCreatedAtBefore(any(Date.class));
  }

  @Test
  void deleteOutdatedData_handlesExceptionGracefully() {
    when(userRequestRepository.findByCreatedAtBefore(any(Date.class)))
        .thenReturn(Collections.emptyList());
    when(userRequestRepository.deleteByCreatedAtBefore(any(Date.class)))
        .thenThrow(new RuntimeException("fail"));

    garbageService.deleteOutdatedData(6);

    verify(userRequestRepository, times(1))
        .deleteByCreatedAtBefore(any(Date.class));
  }
}
