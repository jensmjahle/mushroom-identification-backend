package ntnu.idi.mushroomidentificationbackend.controller;

import ntnu.idi.mushroomidentificationbackend.model.dto.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/request")
public class UserRequestController {

  private final UserRequestService userRequestService;

  @Autowired
  public UserRequestController(UserRequestService userRequestService) {
    this.userRequestService = userRequestService;
  }

  @PostMapping("/create")
  public ResponseEntity<String> createUserRequest(@RequestBody NewUserRequestDTO newUserRequestDTO) {
    // Call service to process the user request and generate a reference code
    String referenceCode = userRequestService.processNewUserRequest(newUserRequestDTO);

    return ResponseEntity.ok(referenceCode);
  }
}
