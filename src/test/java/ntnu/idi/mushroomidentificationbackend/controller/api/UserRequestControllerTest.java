package ntnu.idi.mushroomidentificationbackend.controller.api;

import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    UserRequestController.class,
    UserRequestControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
class UserRequestControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public JWTUtil jwtUtil() { return mock(JWTUtil.class); }
    @Bean public UserRequestService userRequestService() { return mock(UserRequestService.class); }
    @Bean public WebSocketNotificationHandler webSocketNotificationHandler() { return mock(WebSocketNotificationHandler.class); }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private JWTUtil jwtUtil;
  @Autowired private UserRequestService userRequestService;
  @Autowired private WebSocketNotificationHandler webSocketNotificationHandler;

  @Test
  void getRequest_returnsUserRequestDTO() throws Exception {
    String token = "Bearer testtoken";
    String requestId = "abc123";
    UserRequestDTO dto = new UserRequestDTO(requestId, new Date(), new Date(), null, 3, null, null);

    when(jwtUtil.extractUsername("testtoken")).thenReturn(requestId);
    when(userRequestService.getUserRequestDTO(requestId)).thenReturn(dto);

    mockMvc.perform(get("/api/requests/me")
            .header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userRequestId").value(requestId));

    verify(webSocketNotificationHandler)
        .sendRequestUpdateToObservers(requestId, WebsocketNotificationType.USER_LOGGED_IN);
  }
}
