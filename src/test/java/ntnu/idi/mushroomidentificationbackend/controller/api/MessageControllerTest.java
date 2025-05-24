package ntnu.idi.mushroomidentificationbackend.controller.api;

import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    MessageController.class,
    MessageControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
class MessageControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public JWTUtil jwtUtil() { return mock(JWTUtil.class); }
    @Bean public MessageService messageService() { return mock(MessageService.class); }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private JWTUtil jwtUtil;
  @Autowired private MessageService messageService;

  @Test
  void getChatHistory_validRequest_returnsMessages() throws Exception {
    String userRequestId = "abc123";
    String token = "Bearer validToken";

    MessageDTO message = new MessageDTO("msg001", MessageSenderType.USER, "Hello", new Date());
    when(messageService.getChatHistory(userRequestId)).thenReturn(Collections.singletonList(message));

    mockMvc.perform(get("/api/messages/" + userRequestId)
            .header("Authorization", token)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(jwtUtil).validateChatroomToken(token, userRequestId);
    verify(messageService).getChatHistory(userRequestId);
  }
}
