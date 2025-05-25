package ntnu.idi.mushroomidentificationbackend.controller.api;

import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling user statistics-related requests.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/stats")
public class UserStatsController {
  private final StatsService statsService;
  private static final Logger logger = Logger.getLogger(UserStatsController.class.getName());

  /**
   * Logs a registration button press event.
   * This endpoint is used to track when the registration button is pressed
   * by users, allowing the application to gather statistics
   * on user interactions with the registration process.
   * 
   * @return ResponseEntity indicating the success of the operation
   */
  @PostMapping("/registration-button-press")
  public ResponseEntity<String> logRegistrationButtonPress(){
    LogHelper.info(logger, "Registration button pressed");
    statsService.logRegistrationButtonPress();
    return ResponseEntity.ok("Registration button press logged");
  }
}
