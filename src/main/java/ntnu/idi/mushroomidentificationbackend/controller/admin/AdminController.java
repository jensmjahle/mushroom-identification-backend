package ntnu.idi.mushroomidentificationbackend.controller.admin;

import jakarta.validation.Valid;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangePasswordDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.CreateAdminDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateProfileDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.AdminService;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling admin-related requests.
 * This controller provides endpoints for managing admin profiles,
 * including viewing, updating, and deleting admin accounts,
 * as well as creating new admin accounts by superusers.
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminService adminService;
  private final JWTUtil jwtUtil;
  private static final String BEARER = "Bearer ";
  private static final Logger logger = Logger.getLogger(AdminController.class.getName());

  /**
   * Retrieves the details of the currently authenticated admin.
   * This endpoint extracts the admin's username
   * from the JWT token in the Authorization header
   * and returns the admin's details.
   * 
   * @param authHeader the Authorization header containing the JWT token
   * @return ResponseEntity containing the AdminDTO with admin details
   */
  @GetMapping("/me")
  public ResponseEntity<AdminDTO> getAdmin(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
    String token = authHeader.replace(BEARER, "").trim();
    String username = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Received request for admin details - username: {0}", username);
    return ResponseEntity.ok(adminService.getAdminDTO(username));
  }

  /**
   * Retrieves a paginated list of all admins.
   * This endpoint allows superusers to view all admins
   * in a paginated format,
   * making it easier to manage large numbers of admins.
   *
   * @param pageable the pagination information including page number and size
   * @return ResponseEntity containing a Page of AdminDTO objects
   */
  @GetMapping
  public ResponseEntity<Page<AdminDTO>> getAllAdminsPaginated(Pageable pageable) {
    LogHelper.info(logger, "Received request for all admins - page: {0}, size: {1}",
        pageable.getPageNumber(), pageable.getPageSize());
    return ResponseEntity.ok(adminService.getAllAdminsPaginated(pageable));
  }

  /**
   * Updates the profile of the currently authenticated admin.
   * This endpoint allows admins to update their profile information,
   * such as name, email, and other details.
   * 
   * @param authHeader the Authorization header containing the JWT token
   * @param request the UpdateProfileDTO containing the new profile information
   * @return ResponseEntity indicating the success of the operation
   */
  @PutMapping("/profile")
  public ResponseEntity<?> updateProfile(@Valid @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
      @RequestBody UpdateProfileDTO request) {
    String token = authHeader.replace(BEARER, "").trim();
    String username = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Profile update requested for admin: {0}", username);
    adminService.updateProfile(request, username);
    return ResponseEntity.ok().build();
  }

  /**
   * Changes the password of the currently authenticated admin.
   * This endpoint allows admins to change their password
   * by providing the current password and the new password.
   * 
   * @param authHeader the Authorization header containing the JWT token
   * @param request the ChangePasswordDTO containing the current and new passwords
   * @return ResponseEntity indicating the success of the operation
   */
  @PutMapping("/password")
  public ResponseEntity<?> changePassword(@Valid @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
      @RequestBody ChangePasswordDTO request) {
    String token = authHeader.replace(BEARER, "").trim();
    String username = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Password change requested for admin: {0}", username);
    adminService.changePassword(request, username);
    return ResponseEntity.ok().build();
  }

  /**
   * Deletes the currently authenticated admin's account.
   * This endpoint allows admins to delete their own accounts,
   * removing all associated data.
   * 
   * @param authHeader the Authorization header containing the JWT token
   * @return ResponseEntity indicating the success of the operation
   */
  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteAdmin(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
    String token = authHeader.replace(BEARER, "").trim();
    String username = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Delete account requested for admin: {0}", username);
    adminService.deleteAdmin(username);
    return ResponseEntity.ok().build();
  }

  /**
   * Deletes an admin account as a superuser.
   * This endpoint allows superusers to delete any admin account
   * by providing the admin's username.
   * Only superusers can access this endpoint.
   * Only moderators can be deleted by superusers.
   * A superuser cannot delete another superuser.
   * 
   * @param authHeader the Authorization header containing the JWT token
   * @param username the username of the admin to be deleted
   * @return ResponseEntity indicating the success of the operation
   */
  @DeleteMapping("superuser/delete/{username}")
  public ResponseEntity<?> deleteAdmin(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathVariable String username) {
    String token = authHeader.replace(BEARER, "").trim();
    String adminUsername = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Deleting admin {0}, performed by: {1}",username, adminUsername);
    adminService.deleteAdminAsSuperuser(username, adminUsername);
    return ResponseEntity.ok().build();
  }

  /**
   * Creates a new admin account as a superuser.
   * This endpoint allows superusers to create new admin accounts
   * by providing the necessary details in the CreateAdminDTO.
   * * Only superusers can access this endpoint.
   * 
   * @param authHeader the Authorization header containing the JWT token
   * @param request the CreateAdminDTO containing the new admin's details
   * @return ResponseEntity indicating the success of the operation
   */
  @PostMapping("/superuser/create")
  public ResponseEntity<?> createAdmin(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
      @Valid @RequestBody CreateAdminDTO request
  ) {
    String token = authHeader.replace(BEARER, "").trim();
    String superuserUsername = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Superuser {0} is creating a new admin: {1}", superuserUsername, request.getUsername());
    adminService.createAdmin(superuserUsername, request);
    return ResponseEntity.ok().build();
  }
}
