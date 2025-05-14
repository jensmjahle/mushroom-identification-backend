package ntnu.idi.mushroomidentificationbackend.controller.api;

import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/stats")
public class UserStatsController {
  private final StatsService statsService;
  private static final Logger logger = Logger.getLogger(UserStatsController.class.getName());

  @PostMapping("/registration-button-press")
  public ResponseEntity<String> logRegistrationButtonPress(){
    LogHelper.info(logger, "Registration button pressed");
    statsService.logRegistrationButtonPress();
    return ResponseEntity.ok("Registration button press logged");
  }
}
