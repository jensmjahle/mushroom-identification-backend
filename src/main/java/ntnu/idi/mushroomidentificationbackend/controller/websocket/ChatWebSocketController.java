package ntnu.idi.mushroomidentificationbackend.controller.websocket;

import java.io.IOException;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestLockedException;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketConnectionHandler;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketErrorHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;
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
  private final WebSocketConnectionHandler webSocketConnectionHandler;
  private final Logger logger = Logger.getLogger(ChatWebSocketController.class.getName());

  public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, MessageService messageService,
      UserRequestService userRequestService, JWTUtil jwtUtil,
      WebSocketErrorHandler webSocketErrorHandler,
      WebSocketConnectionHandler webSocketConnectionHandler) {
    this.messagingTemplate = messagingTemplate;
    this.messageService = messageService;
    this.userRequestService = userRequestService;
    this.jwtUtil = jwtUtil;
    this.webSocketErrorHandler = webSocketErrorHandler;
    this.webSocketConnectionHandler = webSocketConnectionHandler;
  }

  /**
   * Handles incoming messages from WebSocket clients.
   */
  @MessageMapping("/chat/{userRequestId}")
  public void handleMessage(@DestinationVariable String userRequestId,
      @Header("Authorization") String token,
      @Header("simpSessionId") String sessionId,
      NewMessageDTO messageDTO) throws IOException {

    String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
    String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
    try {
      // Validate the token
      jwtUtil.validateChatroomToken(token, userRequestId);

      if (role.equals(AdminRole.SUPERUSER.toString()) || role.equals(
          AdminRole.MODERATOR.toString())) {
        logger.severe(
            "User is a superuser or moderator, checking if the request is locked by the admin");
        userRequestService.isLockedByAdmin(userRequestId, username);
      }

      // Save the message
      MessageDTO message = messageService.saveMessage(messageDTO, userRequestId);

      // Update project status if needed
      userRequestService.updateProjectAfterMessage(userRequestId, message.getSenderType());

      // Broadcast message to the correct chatroom
      messagingTemplate.convertAndSend("/topic/chatroom/" + userRequestId, message);

    }catch (RequestLockedException e) {
      logger.severe("Request is locked by another admin: " + e.getMessage());
      webSocketErrorHandler.sendRequestLockedError(username, e.getMessage());
    } catch (DatabaseOperationException e) {
      logger.severe("Database operation failed: " + e.getMessage());
      webSocketErrorHandler.sendDatabaseError(username, e.getMessage());
    } catch (UnauthorizedAccessException e) {
      webSocketErrorHandler.sendUnauthorizedError(username, e.getMessage());
    } catch (Exception e) {
      webSocketErrorHandler.sendGeneralError(username, "Unexpected error: " + e.getMessage());
    }
    
  }
}
