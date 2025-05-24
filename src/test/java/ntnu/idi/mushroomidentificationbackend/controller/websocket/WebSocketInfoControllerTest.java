package ntnu.idi.mushroomidentificationbackend.controller.websocket;

import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {
    WebSocketInfoController.class,
    WebSocketInfoControllerTest.TestConfig.class,
    ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev.class
})
@ActiveProfiles("dev")
class WebSocketInfoControllerTest {


  @Configuration
  static class TestConfig {
    @Bean
    public SessionRegistry sessionRegistry() {
      return mock(SessionRegistry.class);
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SessionRegistry sessionRegistry;

  @Test
  void getOnlineAdminCount_returnsCorrectValue() throws Exception {
    when(sessionRegistry.countActiveGlobalAdmins()).thenReturn(5L);

    mockMvc.perform(get("/api/websocket/admins/online-count"))
        .andExpect(status().isOk())
        .andExpect(content().string("5"));
  }
}
