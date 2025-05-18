package ntnu.idi.mushroomidentificationbackend.controller.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    ImageController.class,
    ImageControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
class ImageControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public JWTUtil jwtUtil() { return mock(JWTUtil.class); }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private JWTUtil jwtUtil;

  @Test
  void getImage_validToken_returnsImage() throws Exception {
    String token = "validToken";
    String internalPath = new ClassPathResource("static/test-image.jpg").getFile().getAbsolutePath();

    when(jwtUtil.validateSignedImageUrl(token)).thenReturn(internalPath);

    mockMvc.perform(get("/api/images").param("token", token))
        .andExpect(status().isOk());

    verify(jwtUtil).validateSignedImageUrl(token);
  }

  @Test
  void getImage_invalidToken_returnsUnauthorized() throws Exception {
    when(jwtUtil.validateSignedImageUrl("invalidToken")).thenReturn(null);

    mockMvc.perform(get("/api/images").param("token", "invalidToken"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getImage_fileNotFound_returnsNotFound() throws Exception {
    when(jwtUtil.validateSignedImageUrl("validToken")).thenReturn(Path.of("nonexistent.jpg").toString());

    mockMvc.perform(get("/api/images").param("token", "validToken"))
        .andExpect(status().isNotFound());
  }
}
