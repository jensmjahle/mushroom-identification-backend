package ntnu.idi.mushroomidentificationbackend.controller.api;

import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    UserStatsController.class,
    UserStatsControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
class UserStatsControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public StatsService statsService() { return mock(StatsService.class); }
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private StatsService statsService;

  @Test
  void logRegistrationButtonPress_returnsOk() throws Exception {
    mockMvc.perform(post("/api/stats/registration-button-press"))
        .andExpect(status().isOk())
        .andExpect(content().string("Registration button press logged"));

    verify(statsService).logRegistrationButtonPress();
  }
}
