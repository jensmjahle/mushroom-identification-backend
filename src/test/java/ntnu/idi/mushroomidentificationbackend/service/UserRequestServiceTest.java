package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.dto.request.ChangeRequestStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.exception.RequestLockedException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.*;
import ntnu.idi.mushroomidentificationbackend.security.ReferenceCodeGenerator;
import ntnu.idi.mushroomidentificationbackend.security.SecretsConfig;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRequestServiceTest {

  private UserRequestRepository userRequestRepository;
  private MessageRepository messageRepository;
  private MushroomService mushroomService;
  private AdminService adminService;
  private MushroomRepository mushroomRepository;
  private ImageRepository imageRepository;
  private ReferenceCodeGenerator referenceCodeGenerator;
  private SessionRegistry sessionRegistry;
  private SecretsConfig secretsConfig;
  private UserRequestService userRequestService;

  @BeforeEach
  void setUp() {
    userRequestRepository = mock(UserRequestRepository.class);
    messageRepository = mock(MessageRepository.class);
    mushroomService = mock(MushroomService.class);
    adminService = mock(AdminService.class);
    mushroomRepository = mock(MushroomRepository.class);
    imageRepository = mock(ImageRepository.class);
    referenceCodeGenerator = mock(ReferenceCodeGenerator.class);
    sessionRegistry = mock(SessionRegistry.class);
    secretsConfig = mock(SecretsConfig.class);

    userRequestService = new UserRequestService(
        userRequestRepository,
        messageRepository,
        mushroomService,
        adminService,
        mushroomRepository,
        imageRepository,
        referenceCodeGenerator,
        sessionRegistry,
        secretsConfig
    );
  }

  @Test
  void generateReferenceCode_returnsUniqueCode() {
    when(referenceCodeGenerator.generateCode()).thenReturn("abc123");
    when(userRequestRepository.findByPasswordHash(any())).thenReturn(Optional.empty());
    String result = userRequestService.generateReferenceCode();
    assertNotNull(result);
  }



  @Test
  void getUserRequest_whenNotFound_thenThrow() {
    when(userRequestRepository.findByUserRequestId(any())).thenReturn(Optional.empty());
    assertThrows(RequestNotFoundException.class, () ->
        userRequestService.getUserRequest("nonexistent")
    );
  }

  @Test
  void changeRequestStatus_updatesStatusCorrectly() {
    UserRequest request = new UserRequest();
    request.setStatus(UserRequestStatus.NEW);
    when(userRequestRepository.findByUserRequestId("123")).thenReturn(Optional.of(request));

    ChangeRequestStatusDTO dto = new ChangeRequestStatusDTO();
    dto.setUserRequestId("123");
    dto.setNewStatus(UserRequestStatus.IN_PROGRESS);

    userRequestService.changeRequestStatus(dto);
    assertEquals(UserRequestStatus.IN_PROGRESS, request.getStatus());
    verify(userRequestRepository).save(request);
  }

  @Test
  void updateProjectAfterMessage_updatesStatus() {
    UserRequest request = new UserRequest();
    request.setStatus(UserRequestStatus.PENDING);
    when(userRequestRepository.findByUserRequestId("123")).thenReturn(Optional.of(request));

    userRequestService.updateProjectAfterMessage("123", MessageSenderType.USER);

    assertEquals(UserRequestStatus.NEW, request.getStatus());
    verify(userRequestRepository).save(request);
  }

  @Test
  void updateProjectAfterMessage_whenCompletedAndUserReplies_setsFollowUpFlagWithoutChangingStatus() {
    UserRequest request = new UserRequest();
    request.setStatus(UserRequestStatus.COMPLETED);
    when(userRequestRepository.findByUserRequestId("123")).thenReturn(Optional.of(request));

    userRequestService.updateProjectAfterMessage("123", MessageSenderType.USER);

    assertEquals(UserRequestStatus.COMPLETED, request.getStatus());
    assertTrue(request.isHasFollowUp());
    verify(userRequestRepository).save(request);
  }

  @Test
  void updateProjectAfterMessage_whenCompletedAndAdminReplies_clearsFollowUpFlag() {
    UserRequest request = new UserRequest();
    request.setStatus(UserRequestStatus.COMPLETED);
    request.setHasFollowUp(true);
    when(userRequestRepository.findByUserRequestId("123")).thenReturn(Optional.of(request));

    userRequestService.updateProjectAfterMessage("123", MessageSenderType.ADMIN);

    assertEquals(UserRequestStatus.COMPLETED, request.getStatus());
    assertFalse(request.isHasFollowUp());
    verify(userRequestRepository).save(request);
  }

  @Test
  void tryLockRequest_whenAlreadyLocked_thenThrow() {
    UserRequest request = new UserRequest();
    Admin otherAdmin = new Admin();
    otherAdmin.setUsername("other");
    request.setAdmin(otherAdmin);
    when(userRequestRepository.findByUserRequestId("123")).thenReturn(Optional.of(request));

    Admin currentAdmin = new Admin();
    currentAdmin.setUsername("me");
    when(adminService.getAdmin("me")).thenReturn(currentAdmin);

    assertThrows(RequestLockedException.class, () ->
        userRequestService.tryLockRequest("123", "me")
    );
  }

  @Test
  void forceReleaseRequest_clearsOwnerAndReopensRegardlessOfOwner() {
    UserRequest request = new UserRequest();
    Admin owner = new Admin();
    owner.setUsername("someModerator");
    request.setAdmin(owner);
    request.setStatus(UserRequestStatus.IN_PROGRESS);
    when(userRequestRepository.findByUserRequestId("123")).thenReturn(Optional.of(request));

    userRequestService.forceReleaseRequest("123");

    assertNull(request.getAdmin());
    assertEquals(UserRequestStatus.NEW, request.getStatus());
    verify(userRequestRepository).save(request);
  }

  @Test
  void getFilteredUserRequests_returnsMappedResults() {
    UserRequest req = new UserRequest();
    req.setUserRequestId("123");
    Page<UserRequest> page = new PageImpl<>(List.of(req));

    when(userRequestRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
    when(mushroomRepository.countByUserRequest(req)).thenReturn(2L);
    when(mushroomService.getBasketSummaryBadges("123")).thenReturn(List.of());
    when(mushroomService.getMushroomStatusCounts("123")).thenReturn(Map.of());

    Page result = userRequestService.getFilteredUserRequests(null, false, null, null, PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }
}
