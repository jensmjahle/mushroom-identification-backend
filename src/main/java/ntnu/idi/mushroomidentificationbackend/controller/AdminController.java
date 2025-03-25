package ntnu.idi.mushroomidentificationbackend.controller;

import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangeRequestStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
  private final Logger logger = Logger.getLogger(AdminController.class.getName());
  private final UserRequestService userRequestService;

  public AdminController(UserRequestService userRequestService) {
    this.userRequestService = userRequestService;
  }


  @GetMapping("requests/paginated")
  public ResponseEntity<Page<UserRequestDTO>> getAllRequestsPaginated(Pageable pageable) {
    logger.info(() -> String.format("Received request for all user requests - page: %d, size: %d",
        pageable.getPageNumber(), pageable.getPageSize()));
    return ResponseEntity.ok(userRequestService.getPaginatedUserRequests(pageable));
  }
  
  @PostMapping("requests/change-status")
  public ResponseEntity<String> changeRequestStatus(@RequestBody ChangeRequestStatusDTO changeRequestStatusDTO) {
    logger.info(() -> String.format("Received request to change status of user request %s to %s", changeRequestStatusDTO.getUserRequestId(), changeRequestStatusDTO.getNewStatus()));
    userRequestService.changeRequestStatus(changeRequestStatusDTO);
    return ResponseEntity.ok("Status changed successfully");
  }
  
  @GetMapping("requests/count")
  public ResponseEntity<Long> getNumberOfRequests(@RequestParam UserRequestStatus status) {
    logger.info(() -> String.format("Received request for number of user requests with status %s", status));
    return ResponseEntity.ok(userRequestService.getNumberOfRequests(status));
  }
  
  @GetMapping("requests/{userRequestId}")
  public ResponseEntity<UserRequestDTO> getRequest(@PathVariable String userRequestId) {
    logger.info(() -> String.format("Received request for user request %s", userRequestId));
    return ResponseEntity.ok(userRequestService.getUserRequestDTO(userRequestId));
  }
  
}
