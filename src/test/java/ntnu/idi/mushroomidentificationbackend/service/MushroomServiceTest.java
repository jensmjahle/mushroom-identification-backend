package ntnu.idi.mushroomidentificationbackend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ntnu.idi.mushroomidentificationbackend.dto.request.AddImagesToMushroomDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateMushroomStatusDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.ImageProcessingException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.BasketBadgeType;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;
import ntnu.idi.mushroomidentificationbackend.repository.ImageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MushroomRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

class MushroomServiceTest {

  private MushroomRepository mushroomRepository;
  private UserRequestRepository userRequestRepository;
  private ImageRepository imageRepository;
  private JWTUtil jwtUtil;
  private MushroomService mushroomService;

  @BeforeEach
  void setUp() {
    mushroomRepository = mock(MushroomRepository.class);
    userRequestRepository = mock(UserRequestRepository.class);
    imageRepository = mock(ImageRepository.class);
    jwtUtil = mock(JWTUtil.class);
    mushroomService = new MushroomService(mushroomRepository, userRequestRepository, imageRepository, jwtUtil);
  }

  @Test
  void updateMushroomStatus_whenUserRequestOrMushroomIsMissing_thenThrowException() {
    String requestId = "nonexistent";
    UUID mushroomId = UUID.randomUUID();

    UpdateMushroomStatusDTO dto = new UpdateMushroomStatusDTO();
    dto.setMushroomId(String.valueOf(mushroomId));

    when(userRequestRepository.findByUserRequestId(requestId)).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> mushroomService.updateMushroomStatus(requestId, dto));

    UserRequest request = new UserRequest();
    request.setUserRequestId(requestId);
    when(userRequestRepository.findByUserRequestId(requestId)).thenReturn(Optional.of(request));
    when(mushroomRepository.findById(String.valueOf(mushroomId))).thenReturn(Optional.empty());

    assertThrows(DatabaseOperationException.class, () -> mushroomService.updateMushroomStatus(requestId, dto));
  }

  @Test
  void getBasketSummaryBadges_handlesAllStatuses() {
    String requestId = "req-badges";
    UserRequest request = new UserRequest();
    request.setUserRequestId(requestId);

    List<Mushroom> mushrooms = new ArrayList<>();
    for (MushroomStatus status : MushroomStatus.values()) {
      if (status != MushroomStatus.NOT_PROCESSED) {
        Mushroom m = new Mushroom();
        m.setMushroomStatus(status);
        mushrooms.add(m);
      }
    }

    when(userRequestRepository.findByUserRequestId(requestId)).thenReturn(Optional.of(request));
    when(mushroomRepository.findByUserRequest(Optional.of(request))).thenReturn(mushrooms);

    List<BasketBadgeType> badges = mushroomService.getBasketSummaryBadges(requestId);

    assertTrue(badges.contains(BasketBadgeType.ALL_MUSHROOMS_PROCESSED));
    assertTrue(badges.contains(BasketBadgeType.TOXIC_MUSHROOM_PRESENT));
    assertTrue(badges.contains(BasketBadgeType.PSYCHOACTIVE_MUSHROOM_PRESENT));
    assertTrue(badges.contains(BasketBadgeType.NON_PSILOCYBIN_MUSHROOM_PRESENT));
    assertTrue(badges.contains(BasketBadgeType.UNKNOWN_MUSHROOM_PRESENT));
    assertTrue(badges.contains(BasketBadgeType.UNIDENTIFIABLE_MUSHROOM_PRESENT));
    assertTrue(badges.contains(BasketBadgeType.BAD_PICTURES_MUSHROOM_PRESENT));
  }
  @Test
  void addImagesToMushroom_handlesMissingRequestAndMushroom() {
    String requestId = "invalid-req";
    UUID mushroomId = UUID.randomUUID();

    AddImagesToMushroomDTO dto = new AddImagesToMushroomDTO();
    dto.setMushroomId(String.valueOf(mushroomId));
    dto.setImages(List.of(mock(MultipartFile.class)));

    when(userRequestRepository.findByUserRequestId(requestId)).thenReturn(Optional.empty());
    assertThrows(RequestNotFoundException.class, () -> mushroomService.addImagesToMushroom(requestId, dto));

    UserRequest request = new UserRequest();
    request.setUserRequestId(requestId);
    when(userRequestRepository.findByUserRequestId(requestId)).thenReturn(Optional.of(request));
    when(mushroomRepository.findById(String.valueOf(mushroomId))).thenReturn(Optional.empty());

    assertThrows(DatabaseOperationException.class, () -> mushroomService.addImagesToMushroom(requestId, dto));
  }

  @Test
  void addImagesToMushroom_handlesIOException() {
    String requestId = "req123";
    UUID mushroomId = UUID.randomUUID();
    UserRequest request = new UserRequest();
    request.setUserRequestId(requestId);

    Mushroom mushroom = new Mushroom();
    mushroom.setMushroomId(String.valueOf(mushroomId));
    mushroom.setUserRequest(request);

    MultipartFile badFile = mock(MultipartFile.class);
    AddImagesToMushroomDTO dto = new AddImagesToMushroomDTO();
    dto.setMushroomId(String.valueOf(mushroomId));
    dto.setImages(List.of(badFile));

    when(userRequestRepository.findByUserRequestId(requestId)).thenReturn(Optional.of(request));
    when(mushroomRepository.findById(String.valueOf(mushroomId))).thenReturn(Optional.of(mushroom));
    try (var mock = Mockito.mockStatic(ImageService.class)) {
      mock.when(() -> ImageService.saveImage(any(), any(), any())).thenThrow(new IOException("fail"));
      assertThrows(ImageProcessingException.class, () -> mushroomService.addImagesToMushroom(requestId, dto));
    }
  }
}
