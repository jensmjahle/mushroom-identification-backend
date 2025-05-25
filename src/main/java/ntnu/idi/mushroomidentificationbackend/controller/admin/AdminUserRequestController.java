package ntnu.idi.mushroomidentificationbackend.controller.admin;

import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangeRequestStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling user requests in the admin interface.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/requests")
public class AdminUserRequestController {
  private final Logger logger = Logger.getLogger(AdminUserRequestController.class.getName());
  private final UserRequestService userRequestService;
  private final JWTUtil jwtUtil;
  private final WebSocketNotificationHandler webSocketNotificationHandler;
  private final SessionRegistry sessionRegistry;

  /**
   * Retrieves all user requests with pagination and optional filtering by status.
   * 
   * @param status the status of the user requests to filter by, can be null for all requests
   * @param exclude if true, excludes requests with the specified status; if false, includes only those requests
   * @param pageable the pagination information including page number and size
   * @return ResponseEntity containing a paginated list of UserRequestDTO objects
   */
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

  /**
   * Retrieves the number of user requests based on their status.
   * 
   * @param status the status of the user requests to count
   * @return ResponseEntity containing the count of user requests with the specified status
   */
  @GetMapping("/count")
  public ResponseEntity<Long> getNumberOfRequests(@RequestParam UserRequestStatus status) {
    return ResponseEntity.ok(userRequestService.getNumberOfRequests(status));
  }

  /**
   * Retrieves a specific user request by its ID.
   * 
   * @param userRequestId the ID of the user request to retrieve
   * @return ResponseEntity containing the UserRequestDTO for the specified user request
   */
  @GetMapping("/{userRequestId}")
  public ResponseEntity<UserRequestDTO> getRequest(@PathVariable String userRequestId) {
    logger.info(() -> String.format("Received request for user request %s", userRequestId));
    UserRequestDTO userRequestDTO = userRequestService.getUserRequestDTO(userRequestId);
    return ResponseEntity.ok(userRequestDTO);
  }

  /**
   * Changes the status of a user request.
   * This endpoint allows admins to change the status of a user request
   * to one of the predefined statuses such as COMPLETED, NEW, or PENDING.
   * 
   * @param token the JWT token for authentication, which contains the admin's username
   * @param changeRequestStatusDTO the data transfer object containing the user request ID and the new status
   * @return ResponseEntity containing a message indicating the success of the operation
   */
  @PostMapping("/change-status")
  public ResponseEntity<String> changeRequestStatus(@RequestHeader("Authorization") String token,
      @RequestBody ChangeRequestStatusDTO changeRequestStatusDTO) {
    logger.info(() -> String.format("Received request to change status of user request %s to %s",
        changeRequestStatusDTO.getUserRequestId(), changeRequestStatusDTO.getNewStatus()));
    String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
      userRequestService.tryLockRequest(changeRequestStatusDTO.getUserRequestId(), username);
      userRequestService.changeRequestStatus(changeRequestStatusDTO);
      webSocketNotificationHandler.sendRequestUpdateToObservers(changeRequestStatusDTO.getUserRequestId(),
          WebsocketNotificationType.STATUS_CHANGED);
      String msg = switch (changeRequestStatusDTO.getNewStatus()) {
        case COMPLETED -> "The request was marked as completed.";
        case NEW -> "The request was placed back into the queue.";
        case PENDING -> "The request was put on hold.";
        default -> "Status changed successfully.";
      };
      return ResponseEntity.ok(msg);
  }

  /**
   * Retrieves the next user request from the queue.
   * This endpoint is used to fetch the next user request
   * that is currently under review.
   * 
    * @return ResponseEntity containing the next UserRequestDTO
   */
  @GetMapping("/next")
  public ResponseEntity<Object> getNextRequestFromQueue() {
    logger.info("Received request for next user request in queue");
    UserRequestDTO userRequestDTO = userRequestService.getNextRequestFromQueue();

    if (userRequestDTO == null) {
      return ResponseEntity.noContent().build();
    }
    webSocketNotificationHandler.sendRequestUpdateToObservers(userRequestDTO.getUserRequestId(),
        WebsocketNotificationType.REQUEST_CURRENTLY_UNDER_REVIEW);
    return ResponseEntity.ok(userRequestDTO);
  }

}
