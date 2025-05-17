package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.dto.request.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.mapper.MessageMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {

  private MessageRepository messageRepository;
  private UserRequestService userRequestService;
  private MessageService messageService;

  @BeforeEach
  void setUp() {
    messageRepository = mock(MessageRepository.class);
    userRequestService = mock(UserRequestService.class);
    messageService = new MessageService(messageRepository, userRequestService);
  }

  @Test
  void saveMessage_whenRequestNotCompleted_savesAndReturnsDto() {
    UserRequest request = new UserRequest();
    request.setStatus(UserRequestStatus.NEW);

    NewMessageDTO dto = new NewMessageDTO();
    dto.setContent("Hello");
    dto.setSenderType(MessageSenderType.USER);

    Message entity = MessageMapper.fromDtoToEntity(dto, request);

    when(userRequestService.getUserRequest("req1")).thenReturn(request);
    when(messageRepository.save(any())).thenReturn(entity);

    MessageDTO result = messageService.saveMessage(dto, "req1");
    assertEquals(dto.getContent(), result.getContent());
  }

  @Test
  void saveMessage_whenRequestCompleted_throwsException() {
    UserRequest request = new UserRequest();
    request.setStatus(UserRequestStatus.COMPLETED);
    when(userRequestService.getUserRequest("req1")).thenReturn(request);

    NewMessageDTO dto = new NewMessageDTO();

    assertThrows(DatabaseOperationException.class, () ->
        messageService.saveMessage(dto, "req1")
    );
  }

  @Test
  void getAllMessagesToUserRequest_returnsMessages() {
    UserRequest request = new UserRequest();
    when(messageRepository.findByUserRequestOrderByCreatedAtAsc(request)).thenReturn(List.of(new Message()));
    List<Message> result = messageService.getAllMessagesToUserRequest(request);
    assertEquals(1, result.size());
  }

  @Test
  void getMessageDTO_whenFound_returnsDto() {
    Message message = new Message();
    message.setContent("Hi");
    when(messageRepository.findById("msg1")).thenReturn(Optional.of(message));
    MessageDTO result = messageService.getMessageDTO("msg1");
    assertEquals("Hi", result.getContent());
  }

  @Test
  void getChatHistory_returnsList() {
    UserRequest request = new UserRequest();
    Message msg = new Message();
    msg.setContent("Hi");

    when(userRequestService.getUserRequest("req1")).thenReturn(request);
    when(messageRepository.findByUserRequestOrderByCreatedAtAsc(request)).thenReturn(List.of(msg));

    List<MessageDTO> result = messageService.getChatHistory("req1");
    assertEquals(1, result.size());
    assertEquals("Hi", result.get(0).getContent());
  }

  @Test
  void getChatHistory_whenException_thenThrowsDatabaseOperationException() {
    when(userRequestService.getUserRequest("req1")).thenThrow(new RuntimeException("fail"));
    assertThrows(DatabaseOperationException.class, () -> messageService.getChatHistory("req1"));
  }
}
