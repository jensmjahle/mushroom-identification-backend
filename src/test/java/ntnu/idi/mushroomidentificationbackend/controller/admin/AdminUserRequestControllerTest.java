package ntnu.idi.mushroomidentificationbackend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangeRequestStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminUserRequestController.class)
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    AdminUserRequestController.class,
    AdminUserRequestControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
class AdminUserRequestControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public JWTUtil jwtUtil() { return mock(JWTUtil.class); }
    @Bean public UserRequestService userRequestService() { return mock(UserRequestService.class); }
    @Bean public WebSocketNotificationHandler webSocketNotificationHandler() { return mock(WebSocketNotificationHandler.class); }
    @Bean public SessionRegistry sessionRegistry() { return mock(SessionRegistry.class); }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private JWTUtil jwtUtil;
  @Autowired private UserRequestService userRequestService;
  @Autowired private WebSocketNotificationHandler webSocketNotificationHandler;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String AUTH_HEADER = "Bearer testToken";

  @Test
  void getAllRequestsPaginated_returnsPage() throws Exception {
    Page<UserRequestDTO> page = new PageImpl<>(Collections.singletonList(new UserRequestDTO()));
    when(userRequestService.getPaginatedUserRequests(any())).thenReturn(page);

    mockMvc.perform(get("/api/admin/requests"))
        .andExpect(status().isOk());
  }

  @Test
  void getNumberOfRequests_returnsCount() throws Exception {
    when(userRequestService.getNumberOfRequests(UserRequestStatus.NEW)).thenReturn(5L);

    mockMvc.perform(get("/api/admin/requests/count")
            .param("status", "NEW"))
        .andExpect(status().isOk());
  }

  @Test
  void getRequestById_returnsRequest() throws Exception {
    when(userRequestService.getUserRequestDTO("id1")).thenReturn(new UserRequestDTO());

    mockMvc.perform(get("/api/admin/requests/id1"))
        .andExpect(status().isOk());
  }

  @Test
  void changeRequestStatus_callsServiceAndReturnsMessage() throws Exception {
    ChangeRequestStatusDTO dto = new ChangeRequestStatusDTO("id1", UserRequestStatus.COMPLETED);
    when(jwtUtil.extractUsername(any())).thenReturn("adminUser");

    mockMvc.perform(post("/api/admin/requests/change-status")
            .header("Authorization", AUTH_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    verify(userRequestService).changeRequestStatus(argThat(dto1 ->
        dto.getUserRequestId().equals("id1") &&
            dto.getNewStatus() == UserRequestStatus.COMPLETED
    ));

  }

  @Test
  void getNextRequestFromQueue_returnsRequestOrNoContent() throws Exception {
    when(userRequestService.getNextRequestFromQueue()).thenReturn(new UserRequestDTO());

    mockMvc.perform(get("/api/admin/requests/next"))
        .andExpect(status().isOk());
  }
}
