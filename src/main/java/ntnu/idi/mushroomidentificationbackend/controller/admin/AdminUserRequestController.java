package ntnu.idi.mushroomidentificationbackend.controller.admin;

import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangeRequestStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminUserRequestController {
  private final Logger logger = Logger.getLogger(AdminUserRequestController.class.getName());
  private final UserRequestService userRequestService;

  public AdminUserRequestController(UserRequestService userRequestService) {
    this.userRequestService = userRequestService;
  }

  @GetMapping
  public ResponseEntity<Page<UserRequestDTO>> getAllRequestsPaginated(
      @RequestParam(required = false) UserRequestStatus status,
      @RequestParam(required = false, defaultValue = "false") boolean exclude,
      Pageable pageable
  ) {
    logger.info(() -> String.format("Request for user requests - page: %d, size: %d, status: %s, exclude: %s",
        pageable.getPageNumber(), pageable.getPageSize(), status, exclude));

    if (status != null) {
      if (exclude) {
        return ResponseEntity.ok(userRequestService.getPaginatedRequestsExcludingStatus(status, pageable));
      } else {
        return ResponseEntity.ok(userRequestService.getPaginatedRequestsByStatus(status, pageable));
      }
    }

    return ResponseEntity.ok(userRequestService.getPaginatedUserRequests(pageable));
  }


  @GetMapping("/count")
  public ResponseEntity<Long> getNumberOfRequests(@RequestParam UserRequestStatus status) {
    logger.info(() -> String.format("Received request for number of user requests with status %s", status));
    return ResponseEntity.ok(userRequestService.getNumberOfRequests(status));
  }

  @GetMapping("/{userRequestId}")
  public ResponseEntity<UserRequestDTO> getRequest(@PathVariable String userRequestId) {
    logger.info(() -> String.format("Received request for user request %s", userRequestId));
    return ResponseEntity.ok(userRequestService.getUserRequestDTO(userRequestId));
  }

  @PostMapping("/change-status")
  public ResponseEntity<String> changeRequestStatus(@RequestBody ChangeRequestStatusDTO changeRequestStatusDTO) {
    logger.info(() -> String.format("Received request to change status of user request %s to %s",
        changeRequestStatusDTO.getUserRequestId(), changeRequestStatusDTO.getNewStatus()));
    userRequestService.changeRequestStatus(changeRequestStatusDTO);
    return ResponseEntity.ok("Status changed successfully");
  }
}
