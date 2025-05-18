package ntnu.idi.mushroomidentificationbackend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangePasswordDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.CreateAdminDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateProfileDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.service.AdminService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    AdminController.class,
    AdminControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
class AdminControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public JWTUtil jwtUtil() { return mock(JWTUtil.class); }
    @Bean public AdminService adminService() { return mock(AdminService.class); }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private JWTUtil jwtUtil;
  @Autowired private AdminService adminService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final String AUTH_HEADER = "Bearer testToken";

  @Test
  void getAdmin_returnsAdminDTO() throws Exception {
    when(jwtUtil.extractUsername(any())).thenReturn("adminUser");
    when(adminService.getAdminDTO("adminUser")).thenReturn(new AdminDTO());

    mockMvc.perform(get("/api/admin/me")
            .header("Authorization", AUTH_HEADER))
        .andExpect(status().isOk());
  }

  @Test
  void getAllAdminsPaginated_returnsPage() throws Exception {
    Page<AdminDTO> page = new PageImpl<>(Collections.singletonList(new AdminDTO()));
    when(adminService.getAllAdminsPaginated(any())).thenReturn(page);

    mockMvc.perform(get("/api/admin"))
        .andExpect(status().isOk());
  }

  @Test
  void updateProfile_callsServiceAndReturnsOk() throws Exception {
    UpdateProfileDTO dto = new UpdateProfileDTO(null, null, null);
    when(jwtUtil.extractUsername(any())).thenReturn("adminUser");

    mockMvc.perform(put("/api/admin/profile")
            .header("Authorization", AUTH_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    verify(adminService).updateProfile(any(UpdateProfileDTO.class), eq("adminUser"));
  }

  @Test
  void changePassword_callsServiceAndReturnsOk() throws Exception {
    ChangePasswordDTO dto = new ChangePasswordDTO("old", "new", "new");
    when(jwtUtil.extractUsername(any())).thenReturn("adminUser");

    mockMvc.perform(put("/api/admin/password")
            .header("Authorization", AUTH_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    verify(adminService).changePassword(any(ChangePasswordDTO.class), eq("adminUser"));
  }

  @Test
  void deleteAdmin_callsServiceAndReturnsOk() throws Exception {
    when(jwtUtil.extractUsername(any())).thenReturn("adminUser");

    mockMvc.perform(delete("/api/admin/delete")
            .header("Authorization", AUTH_HEADER))
        .andExpect(status().isOk());

    verify(adminService).deleteAdmin("adminUser");
  }

  @Test
  void deleteAdminAsSuperuser_callsServiceAndReturnsOk() throws Exception {
    when(jwtUtil.extractUsername(any())).thenReturn("superAdmin");

    mockMvc.perform(delete("/api/admin/superuser/delete/targetUser")
            .header("Authorization", AUTH_HEADER))
        .andExpect(status().isOk());

    verify(adminService).deleteAdminAsSuperuser("targetUser", "superAdmin");
  }

  @Test
  void createAdmin_callsServiceAndReturnsOk() throws Exception {
    CreateAdminDTO dto = new CreateAdminDTO("newadmin", "Pass1234", "newadmin@example.com", AdminRole.MODERATOR);
    when(jwtUtil.extractUsername(any())).thenReturn("superAdmin");

    mockMvc.perform(post("/api/admin/superuser/create")
            .header("Authorization", AUTH_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    verify(adminService).createAdmin(eq("superAdmin"), any(CreateAdminDTO.class));
  }
}
