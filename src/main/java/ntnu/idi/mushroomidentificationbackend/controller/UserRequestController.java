package ntnu.idi.mushroomidentificationbackend.controller;

import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/request")

public class UserRequestController {

  private final UserRequestService userRequestService;
  private final Logger logger = Logger.getLogger(UserRequestController.class.getName());
  
  @Autowired
  public UserRequestController(UserRequestService userRequestService) {
    this.userRequestService = userRequestService;
  }

  @PostMapping("/create")
  public ResponseEntity<String> createUserRequest(@ModelAttribute NewUserRequestDTO newUserRequestDTO) {
    logger.info("Received new user request at " + System.currentTimeMillis());
    // Call service to process the user request and generate a reference code
    String referenceCode = userRequestService.processNewUserRequest(newUserRequestDTO);

    return ResponseEntity.ok(referenceCode);
  }
}
