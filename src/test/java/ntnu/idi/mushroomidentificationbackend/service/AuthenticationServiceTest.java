package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.exception.UserNotFoundException;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;
import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

  private AdminRepository adminRepository;
  private JWTUtil jwtUtil;
  private PasswordEncoder passwordEncoder;
  private UserRequestRepository userRequestRepository;
  private UserRequestService userRequestService;
  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    adminRepository = mock(AdminRepository.class);
    jwtUtil = mock(JWTUtil.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userRequestRepository = mock(UserRequestRepository.class);
    userRequestService = mock(UserRequestService.class);
    authenticationService = new AuthenticationService(adminRepository, jwtUtil, passwordEncoder, userRequestRepository, userRequestService);
  }

  @Test
  void authenticate_validCredentials_returnsToken() {
    Admin admin = new Admin();
    admin.setUsername("admin");
    admin.setPasswordHash("hashed-password");
    admin.setRole(AdminRole.SUPERUSER);

    when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(passwordEncoder.matches("password", "hashed-password")).thenReturn(true);
    when(jwtUtil.generateToken("admin", "SUPERUSER")).thenReturn("token");

    String result = authenticationService.authenticate("admin", "password");
    assertEquals("token", result);
  }

  @Test
  void authenticate_invalidUsername_throwsUserNotFound() {
    when(adminRepository.findByUsername("admin")).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> authenticationService.authenticate("admin", "password"));
  }

  @Test
  void authenticate_invalidPassword_throwsUnauthorized() {
    Admin admin = new Admin();
    admin.setUsername("admin");
    admin.setPasswordHash("hashed-password");

    when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

    assertThrows(UnauthorizedAccessException.class, () -> authenticationService.authenticate("admin", "wrong"));
  }

  @Test
  void authenticateUserRequest_validReference_returnsToken() {
    UserRequest request = new UserRequest();
    request.setUserRequestId("req123");
    request.setPasswordHash("hashed");

    when(userRequestService.hashReferenceCodeForLookup("refCode")).thenReturn("lookupKey");
    when(userRequestRepository.findByLookUpKey("lookupKey")).thenReturn(Optional.of(request));
    when(passwordEncoder.matches("refCode", "hashed")).thenReturn(true);
    when(jwtUtil.generateToken("req123", "USER")).thenReturn("token");

    String result = authenticationService.authenticateUserRequest("refCode");
    assertEquals("token", result);
  }

  @Test
  void authenticateUserRequest_invalidReference_throwsNotFound() {
    when(userRequestService.hashReferenceCodeForLookup("refCode")).thenReturn("lookupKey");
    when(userRequestRepository.findByLookUpKey("lookupKey")).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> authenticationService.authenticateUserRequest("refCode"));
  }

  @Test
  void authenticateUserRequest_wrongPassword_throwsNotFound() {
    UserRequest request = new UserRequest();
    request.setPasswordHash("hashed");

    when(userRequestService.hashReferenceCodeForLookup("refCode")).thenReturn("lookupKey");
    when(userRequestRepository.findByLookUpKey("lookupKey")).thenReturn(Optional.of(request));
    when(passwordEncoder.matches("refCode", "hashed")).thenReturn(false);

    assertThrows(RequestNotFoundException.class, () -> authenticationService.authenticateUserRequest("refCode"));
  }
}
