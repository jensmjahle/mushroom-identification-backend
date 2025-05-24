package ntnu.idi.mushroomidentificationbackend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateMushroomStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MushroomDTO;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.service.MushroomService;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminMushroomController.class)
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    AdminMushroomController.class,
    AdminMushroomControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
class AdminMushroomControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public JWTUtil jwtUtil() { return mock(JWTUtil.class); }
    @Bean public MushroomService mushroomService() { return mock(MushroomService.class); }
    @Bean public UserRequestService userRequestService() { return mock(UserRequestService.class); }
    @Bean public WebSocketNotificationHandler webSocketNotificationHandler() { return mock(WebSocketNotificationHandler.class); }
    @Bean public SessionRegistry sessionRegistry() { return mock(SessionRegistry.class); }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private JWTUtil jwtUtil;
  @Autowired private MushroomService mushroomService;
  @Autowired private UserRequestService userRequestService;
  @Autowired private WebSocketNotificationHandler webSocketNotificationHandler;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void updateMushroomStatus_callsServiceAndReturnsOk() throws Exception {
    String userRequestId = "request123";
    String token = "Bearer testToken";
    UpdateMushroomStatusDTO dto = new UpdateMushroomStatusDTO();
    MushroomDTO response = new MushroomDTO();

    when(jwtUtil.extractUsername(any())).thenReturn("adminUser");
    when(mushroomService.updateMushroomStatus(eq(userRequestId), any(UpdateMushroomStatusDTO.class))).thenReturn(response);

    mockMvc.perform(post("/api/admin/mushrooms/" + userRequestId + "/status")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    verify(userRequestService).tryLockRequest(userRequestId, "adminUser");
    verify(mushroomService).updateMushroomStatus(eq(userRequestId), any(UpdateMushroomStatusDTO.class));
    verify(userRequestService).updateRequest(userRequestId);
    verify(webSocketNotificationHandler).sendRequestUpdateToObservers(eq(userRequestId), any());
  }
}
