package ntnu.idi.mushroomidentificationbackend.handler;


import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketExceptionHandler {

  @MessageExceptionHandler(DatabaseOperationException.class)
  @SendToUser("/queue/errors")
  public Map<String, String> handleDatabaseError(DatabaseOperationException ex) {
    return Map.of(
        "type", "DATABASE_ERROR",
        "message", ex.getMessage()
    );
  }

  @MessageExceptionHandler(UnauthorizedAccessException.class)
  @SendToUser("/queue/errors")
  public Map<String, String> handleUnauthorizedError(UnauthorizedAccessException ex) {
    return Map.of(
        "type", "UNAUTHORIZED",
        "message", ex.getMessage()
    );
  }

  @MessageExceptionHandler(Exception.class)
  @SendToUser("/queue/errors")
  public Map<String, String> handleAllOtherErrors(Exception ex) {
    return Map.of(
        "type", "GENERAL_ERROR",
        "message", "Unexpected WebSocket error: " + ex.getMessage()
    );
  }
}
