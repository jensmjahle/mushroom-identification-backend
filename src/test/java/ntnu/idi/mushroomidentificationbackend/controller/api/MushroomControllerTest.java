package ntnu.idi.mushroomidentificationbackend.controller.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import ntnu.idi.mushroomidentificationbackend.dto.response.MushroomDTO;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.service.MushroomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    MushroomController.class,
    MushroomControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
class MushroomControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public JWTUtil jwtUtil() { return mock(JWTUtil.class); }
    @Bean public MushroomService mushroomService() { return mock(MushroomService.class); }
    @Bean public WebSocketNotificationHandler webSocketNotificationHandler() { return mock(WebSocketNotificationHandler.class); }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private JWTUtil jwtUtil;
  @Autowired private MushroomService mushroomService;

  @Test
  void getAllMushrooms_returnsList() throws Exception {
    String userRequestId = "abc123";
    String token = "Bearer validtoken";
    when(mushroomService.getAllMushrooms(userRequestId)).thenReturn(Collections.singletonList(new MushroomDTO()));

    mockMvc.perform(get("/api/mushrooms/" + userRequestId)
            .header("Authorization", token))
        .andExpect(status().isOk());

    verify(jwtUtil).validateChatroomToken(token, userRequestId);
    verify(mushroomService).getAllMushrooms(userRequestId);
  }
}
