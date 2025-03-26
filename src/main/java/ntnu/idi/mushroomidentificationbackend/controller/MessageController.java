package ntnu.idi.mushroomidentificationbackend.controller;

import java.util.List;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.MessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
  
  @GetMapping("{userRequestId}/history")
  public List<MessageDTO> getChatHistory(
      @PathVariable String userRequestId,
      @RequestHeader("Authorization") String token) {
    
    logger.info("Getting chat history for userRequestId: " + userRequestId);
    jwtUtil.validateChatroomToken(token, userRequestId);
    return messageService.getChatHistory(userRequestId);
  }
  
  
}

