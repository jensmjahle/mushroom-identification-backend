package ntnu.idi.mushroomidentificationbackend.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntnu.idi.mushroomidentificationbackend.dto.request.LoginRequestDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UserLoginDTO;
import ntnu.idi.mushroomidentificationbackend.exception.TooManyRequestsException;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.handler.GlobalExceptionHandler;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.LoginAttemptService;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.security.TokenBlocklistService;
import ntnu.idi.mushroomidentificationbackend.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    AuthenticationController.class,
    AuthenticationControllerTest.TestConfig.class,
    SecurityConfigDev.class,
    LoginAttemptService.class,
    GlobalExceptionHandler.class
})
class AuthenticationControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public AuthenticationService authenticationService() { return mock(AuthenticationService.class); }
    @Bean public JWTUtil jwtUtil() { return mock(JWTUtil.class); }
    @Bean public TokenBlocklistService tokenBlocklistService() { return mock(TokenBlocklistService.class); }
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void adminLogin_returnsToken() throws Exception {
    LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "password");

    when(authenticationService.authenticate("admin", "password")).thenReturn("token123");

    mockMvc.perform(post("/auth/admin/login")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("token123"));
  }

  @Test
  void userLogin_returnsToken() throws Exception {
    UserLoginDTO userLoginDTO = new UserLoginDTO("refcode123");

    when(authenticationService.authenticateUserRequest("refcode123")).thenReturn("userToken456");

    mockMvc.perform(post("/auth/user/login")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userLoginDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("userToken456"));
  }

  @Test
  void adminLogin_repeatedFailures_isLockedOutWith429() throws Exception {
    // Uses a username not touched by the other tests in this class, since LoginAttemptService
    // state is shared across tests via the cached Spring test context.
    LoginRequestDTO loginRequest = new LoginRequestDTO("attacker", "wrong");
    when(authenticationService.authenticate("attacker", "wrong"))
        .thenThrow(new UnauthorizedAccessException("Invalid username or password"));

    for (int i = 0; i < 5; i++) {
      mockMvc.perform(post("/auth/admin/login")
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(loginRequest)))
          .andExpect(status().isUnauthorized());
    }

    mockMvc.perform(post("/auth/admin/login")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isTooManyRequests());
  }
}
