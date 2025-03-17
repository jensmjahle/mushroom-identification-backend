package ntnu.idi.mushroomidentificationbackend.controller;


import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestWithMessagesDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
public class UserRequestController {
  private final JWTUtil jwtUtil;
  private final UserRequestService userRequestService;
  private final Logger logger = Logger.getLogger(UserRequestController.class.getName());
  
  @Autowired
  public UserRequestController(JWTUtil jwtUtil, UserRequestService userRequestService) {
    this.jwtUtil = jwtUtil;
    this.userRequestService = userRequestService;
  }

  @PostMapping("/create")
  public ResponseEntity<String> createUserRequest(@ModelAttribute NewUserRequestDTO newUserRequestDTO) {
    logger.info("Received new user request");
    String referenceCode = userRequestService.processNewUserRequest(newUserRequestDTO);
    return ResponseEntity.ok(referenceCode);
  }
  
  @GetMapping("/retrieve")
  public ResponseEntity<UserRequestWithMessagesDTO> getRequest(@RequestHeader("Authorization") String token) {
    logger.info("Retrieving user request");

    String userRequestId = jwtUtil.extractUsername(token.replace("Bearer ", ""));
    
    return ResponseEntity.ok(userRequestService.retrieveUserRequest(userRequestId));
  }
  
  @GetMapping("/paginated")
  public ResponseEntity<Page<UserRequestDTO>> getAllRequestsPaginated(Pageable pageable) {
    logger.info(() -> String.format("Received request for all user requests - page: %d, size: %d",
        pageable.getPageNumber(), pageable.getPageSize()));
    return ResponseEntity.ok(userRequestService.getPaginatedUserRequests(pageable));
  }
}
