package ntnu.idi.mushroomidentificationbackend.controller.websocket;

import java.io.IOException;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestLockedException;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketErrorHandler;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
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
  private final WebSocketNotificationHandler webSocketNotificationHandler;
  private final Logger logger = Logger.getLogger(ChatWebSocketController.class.getName());

  public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, MessageService messageService,
      UserRequestService userRequestService, JWTUtil jwtUtil,
      WebSocketErrorHandler webSocketErrorHandler,
      WebSocketNotificationHandler webSocketNotificationHandler) {
    this.messagingTemplate = messagingTemplate;
    this.messageService = messageService;
    this.userRequestService = userRequestService;
    this.jwtUtil = jwtUtil;
    this.webSocketErrorHandler = webSocketErrorHandler;
    this.webSocketNotificationHandler = webSocketNotificationHandler;
  }

  /**
   * Handles incoming chat messages in a WebSocket connection.
   * This method processes the message, saves it to the database,
   * updates the project status if necessary,
   * and broadcasts the message to the appropriate chatroom.
   * It also handles various exceptions that may occur during processing,
   * including request locking, database operations, and unauthorized access.
   * 
   * @param userRequestId the ID of the user request associated with the chatroom
   * @param token the JWT token for authentication
   * @param sessionId the session ID of the WebSocket connection
   * @param messageDTO the data transfer object containing the new message details
   * @throws IOException if an I/O error occurs during message handling
   */
  @MessageMapping("/chat/{userRequestId}")
  public void handleMessage(@DestinationVariable String userRequestId,
      @Header("Authorization") String token,
      @Header("simpSessionId") String sessionId,
      NewMessageDTO messageDTO) throws IOException {

    String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
    String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
    try {
      jwtUtil.validateChatroomToken(token, userRequestId);

      if (role.equals(AdminRole.SUPERUSER.toString()) || role.equals(AdminRole.MODERATOR.toString())) {
          userRequestService.tryLockRequest(userRequestId, username);
      }


      // Save the message
      MessageDTO message = messageService.saveMessage(messageDTO, userRequestId);

      // Update project status if needed
      userRequestService.updateProjectAfterMessage(userRequestId, message.getSenderType());

      // Broadcast message to the correct chatroom
      messagingTemplate.convertAndSend("/topic/chatroom/" + userRequestId, message);
      
      // Notify observers about the new message
      webSocketNotificationHandler.sendRequestUpdateToObservers(userRequestId, WebsocketNotificationType.NEW_CHAT_MESSAGE);

    }catch (RequestLockedException e) {
      logger.severe("Request is locked by another admin: " + e.getMessage());
      webSocketErrorHandler.sendRequestLockedError(username, e.getMessage());
    } catch (DatabaseOperationException e) {
      logger.severe("Database operation failed: " + e.getMessage());
      webSocketErrorHandler.sendDatabaseError(username, e.getMessage());
    } catch (UnauthorizedAccessException e) {
      webSocketErrorHandler.sendUnauthorizedError(username, e.getMessage());
    } catch (Exception e) {
      logger.severe("Unexpected error: " + e.getMessage());
      webSocketErrorHandler.sendGeneralError(username, "Unexpected error: " + e.getMessage());
    }
    
  }
}
