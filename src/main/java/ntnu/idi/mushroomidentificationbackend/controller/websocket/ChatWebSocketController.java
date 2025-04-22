package ntnu.idi.mushroomidentificationbackend.controller.websocket;

import java.io.IOException;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.service.MessageService;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Header;

@Controller
public class ChatWebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private final MessageService messageService;
  private final JWTUtil jwtUtil;

  public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, MessageService messageService,
      JWTUtil jwtUtil) {
    this.messagingTemplate = messagingTemplate;
    this.messageService = messageService;
    this.jwtUtil = jwtUtil;
  }

  /**
   * Handles incoming messages from WebSocket clients.
   */
  @MessageMapping("/chat/{userRequestId}")
  public void handleMessage(@DestinationVariable String userRequestId,
      @Header("Authorization") String token,
      NewMessageDTO messageDTO) throws IOException {
    
    // Validate the token
    jwtUtil.validateChatroomToken(token, userRequestId);
    System.out.println(messageDTO.toString());
    
    // Save the message
    MessageDTO message = messageService.saveMessage(messageDTO, userRequestId);
    
    // Broadcast message to the correct chatroom
    messagingTemplate.convertAndSend("/topic/chatroom/" + userRequestId, message);
  }
}
