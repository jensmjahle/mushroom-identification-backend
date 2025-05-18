package ntnu.idi.mushroomidentificationbackend.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntnu.idi.mushroomidentificationbackend.dto.request.LoginRequestDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UserLoginDTO;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
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
    SecurityConfigDev.class
})
class AuthenticationControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public AuthenticationService authenticationService() { return mock(AuthenticationService.class); }
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
}
