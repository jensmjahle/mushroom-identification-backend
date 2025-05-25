package ntnu.idi.mushroomidentificationbackend.controller.api;

import java.util.List;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.MessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling chat messages related to user requests.
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {
  Logger logger = Logger.getLogger(MessageController.class.getName());
  MessageService messageService;
  JWTUtil jwtUtil;
  
  public MessageController(MessageService messageService, JWTUtil jwtUtil) {
    this.messageService = messageService;
    this.jwtUtil = jwtUtil;
  }

  /**
   * Retrieves the chat history for a specific user request.
   * This endpoint fetches all messages
   * associated with a user request ID,
   * allowing users to view their chat history
   * with the system.
   * 
   * @param userRequestId the ID of the user request for which chat history is to be retrieved
   * @param token the JWT token for authentication
   * @return List of MessageDTO objects representing the chat history
   */
  @GetMapping("{userRequestId}")
  public List<MessageDTO> getChatHistory(
      @PathVariable String userRequestId,
      @RequestHeader("Authorization") String token) {
    jwtUtil.validateChatroomToken(token, userRequestId);
    return messageService.getChatHistory(userRequestId);
  }
  
  
}

