package ntnu.idi.mushroomidentificationbackend.controller;

import java.io.IOException;
import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewTextMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.mapper.MessageMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.service.MessageService;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Header;

import java.util.Date;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class ChatController {

  private final SimpMessagingTemplate messagingTemplate;
  private final MessageService messageService;
  private final UserRequestService userRequestService;
 
  private final JWTUtil jwtUtil;

  public ChatController(SimpMessagingTemplate messagingTemplate, MessageService messageService,
      UserRequestService userRequestService, JWTUtil jwtUtil) {
    this.messagingTemplate = messagingTemplate;
    this.messageService = messageService;
    this.userRequestService = userRequestService;
    this.jwtUtil = jwtUtil;
  }

  /**
   * Handles incoming messages from WebSocket clients.
   */
  @MessageMapping("/chat/{userRequestId}")
  public void handleMessage(@DestinationVariable String userRequestId,
      @Header("Authorization") String token,
      NewTextMessageDTO messageDTO) throws IOException {
    
    // Validate the token
    jwtUtil.validateChatroomToken(token, userRequestId);
    
    // Save the message
    MessageDTO message = messageService.saveMessage(messageDTO, userRequestId);
    
    
    // Broadcast message to the correct chatroom
    messagingTemplate.convertAndSend("/topic/chatroom/" + userRequestId, message);
  }

  @GetMapping("/{userRequestId}/history")
  public List<MessageDTO> getChatHistory(
      @PathVariable String userRequestId,
      @RequestHeader("Authorization") String token) {

    jwtUtil.validateChatroomToken(token, userRequestId);
    return messageService.getChatHistory(userRequestId);
  }


}
