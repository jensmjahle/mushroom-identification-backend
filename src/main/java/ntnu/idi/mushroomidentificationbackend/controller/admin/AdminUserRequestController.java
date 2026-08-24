package ntnu.idi.mushroomidentificationbackend.controller.admin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
   * Retrieves user requests, optionally filtered by status and/or a createdAt date range, sorted and
   * paginated per the given Pageable (defaulting to sorted by updatedAt, most recent first).
   *
   * @param status the status of the user requests to filter by, can be null for all requests
   * @param exclude if true, excludes requests with the specified status; if false, includes only those requests
   * @param from the start of the createdAt range to filter by (inclusive), or null to not filter by date
   * @param to the end of the createdAt range to filter by (inclusive), or null to not filter by date
   * @param unpaged if true, returns every matching request in a single page instead of paginating
   * @param pageable the pagination and sort information including page number, size, and sort
   * @return ResponseEntity containing a paginated list of UserRequestDTO objects
   */
  @GetMapping
  public ResponseEntity<Page<UserRequestDTO>> getAllRequestsPaginated(
      @RequestParam(required = false) UserRequestStatus status,
      @RequestParam(required = false, defaultValue = "false") boolean exclude,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false, defaultValue = "false") boolean unpaged,
      @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
      Pageable pageable
  ) {
    logger.info(() -> String.format(
        "Request for user requests - page: %d, size: %d, sort: %s, status: %s, exclude: %s, from: %s, to: %s, unpaged: %s",
        pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), status, exclude, from, to, unpaged));

    Date fromDate = from == null ? null : Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date toDate = to == null ? null
        : Date.from(to.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
    Pageable effectivePageable = unpaged ? Pageable.unpaged(pageable.getSort()) : pageable;

    return ResponseEntity.ok(
        userRequestService.getFilteredUserRequests(status, exclude, fromDate, toDate, effectivePageable));
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
   * Forcibly releases a user request from whichever admin currently owns it and puts it back
   * into the queue. Superuser-only: lets a superuser reclaim a request whose assigned
   * moderator has stopped responding, without needing to be the current owner themselves.
   *
   * @param userRequestId the ID of the user request to release
   * @return ResponseEntity containing a confirmation message
   */
  @PostMapping("/{userRequestId}/release")
  public ResponseEntity<String> releaseRequest(@PathVariable String userRequestId) {
    logger.info(() -> String.format("Received request to force-release user request %s", userRequestId));
    userRequestService.forceReleaseRequest(userRequestId);
    webSocketNotificationHandler.sendRequestUpdateToObservers(userRequestId, WebsocketNotificationType.STATUS_CHANGED);
    webSocketNotificationHandler.sendRequestUpdateToObservers(userRequestId, WebsocketNotificationType.ADMIN_LEFT_REQUEST);
    return ResponseEntity.ok("The request was released and placed back into the queue.");
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
