package ntnu.idi.mushroomidentificationbackend.controller.websocket;

import java.io.IOException;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketErrorHandler;
import ntnu.idi.mushroomidentificationbackend.service.MessageService;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Header;

@Controller
public class ChatWebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private final MessageService messageService;
  private final UserRequestService userRequestService;
  private final JWTUtil jwtUtil;
  private final WebSocketErrorHandler webSocketErrorHandler;

  public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, MessageService messageService,
      UserRequestService userRequestService, JWTUtil jwtUtil,
      WebSocketErrorHandler webSocketErrorHandler) {
    this.messagingTemplate = messagingTemplate;
    this.messageService = messageService;
    this.userRequestService = userRequestService;
    this.jwtUtil = jwtUtil;
    this.webSocketErrorHandler = webSocketErrorHandler;
  }

  /**
   * Handles incoming messages from WebSocket clients.
   */
  @MessageMapping("/chat/{userRequestId}")
  public void handleMessage(@DestinationVariable String userRequestId,
      @Header("Authorization") String token,
      NewMessageDTO messageDTO) throws IOException {

    String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
    
    try {
    // Validate the token
    jwtUtil.validateChatroomToken(token, userRequestId);
    
    // Save the message
    MessageDTO message = messageService.saveMessage(messageDTO, userRequestId);
      
    // Update project status if needed
    userRequestService.updateProjectAfterMessage(userRequestId, message.getSenderType());

      // Broadcast message to the correct chatroom
      messagingTemplate.convertAndSend("/topic/chatroom/" + userRequestId, message);
      
    } catch (DatabaseOperationException e) {
      webSocketErrorHandler.sendDatabaseError(username, e.getMessage());
    } catch (UnauthorizedAccessException e) {
      webSocketErrorHandler.sendUnauthorizedError(username, e.getMessage());
    } catch (Exception e) {
      webSocketErrorHandler.sendGeneralError(username, "Unexpected error: " + e.getMessage());
    }
    
  }
}
