package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.dto.request.ChangePasswordDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.CreateAdminDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateProfileDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.exception.*;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;
import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import ntnu.idi.mushroomidentificationbackend.mapper.AdminMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

  private AdminRepository adminRepository;
  private PasswordEncoder passwordEncoder;
  private AdminService adminService;

  @BeforeEach
  void setUp() {
    adminRepository = mock(AdminRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    adminService = new AdminService(adminRepository, passwordEncoder);
  }

  @Test
  void createAdmin() {
    CreateAdminDTO dto = new CreateAdminDTO();
    dto.setUsername("Admin123");
    dto.setPassword("StrongPass1");
    dto.setEmail("admin@example.com");
    dto.setRole(AdminRole.SUPERUSER);

    Admin superuser = new Admin();
    superuser.setUsername("superadmin");
    superuser.setRole(AdminRole.SUPERUSER);

    when(adminRepository.findByUsername("superadmin")).thenReturn(Optional.of(superuser));
    when(adminRepository.findByUsername("admin123")).thenReturn(Optional.empty());
    when(adminRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("StrongPass1")).thenReturn("encodedPass");

    assertDoesNotThrow(() -> adminService.createAdmin("superadmin", dto));
    verify(adminRepository, times(1)).save(any(Admin.class));
  }

  @Test
  void getAllAdminsPaginated() {
    Admin admin = new Admin();
    admin.setUsername("admin");
    admin.setEmail("admin@example.com");
    admin.setRole(AdminRole.SUPERUSER);

    Page<Admin> page = new PageImpl<>(List.of(admin));
    Pageable pageable = PageRequest.of(0, 10);

    when(adminRepository.findAll(pageable)).thenReturn(page);

    Page<AdminDTO> result = adminService.getAllAdminsPaginated(pageable);

    assertEquals(1, result.getContent().size());
    assertEquals("admin", result.getContent().get(0).getUsername());
  }

  @Test
  void updateProfile() {
    Admin admin = new Admin();
    admin.setUsername("admin");
    admin.setEmail("old@example.com");

    UpdateProfileDTO dto = new UpdateProfileDTO();
    dto.setFirstname("New");
    dto.setLastname("Name");
    dto.setEmail("new@example.com");

    when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(adminRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

    adminService.updateProfile(dto, "admin");

    assertEquals("new@example.com", admin.getEmail());
    assertEquals("New", admin.getFirstname());
    assertEquals("Name", admin.getLastname());
  }

  @Test
  void changePassword() {
    ChangePasswordDTO dto = new ChangePasswordDTO();
    dto.setOldPassword("OldPass1");
    dto.setNewPassword("NewPass1");
    dto.setConfirmPassword("NewPass1");

    Admin admin = new Admin();
    admin.setUsername("admin");
    admin.setPasswordHash("hashedOld");

    when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(passwordEncoder.matches("OldPass1", "hashedOld")).thenReturn(true);
    when(passwordEncoder.encode("NewPass1")).thenReturn("hashedNew");

    adminService.changePassword(dto, "admin");

    verify(adminRepository).save(admin);
    assertEquals("hashedNew", admin.getPasswordHash());
  }

  @Test
  void deleteAdmin() {
    Admin admin = new Admin();
    admin.setUsername("admin");

    when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

    adminService.deleteAdmin("admin");

    verify(adminRepository).delete(admin);
  }

  @Test
  void getAdminDTO() {
    Admin admin = new Admin();
    admin.setUsername("admin");
    admin.setEmail("admin@example.com");
    admin.setRole(AdminRole.SUPERUSER);

    when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

    AdminDTO dto = adminService.getAdminDTO("admin");

    assertEquals("admin", dto.getUsername());
    assertEquals("admin@example.com", dto.getEmail());
  }

  @Test
  void getAdmin() {
    Admin admin = new Admin();
    admin.setUsername("admin");

    when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

    Admin result = adminService.getAdmin("admin");

    assertEquals("admin", result.getUsername());
  }

  @Test
  void deleteAdminAsSuperuser() {
    Admin superuser = new Admin();
    superuser.setUsername("super");
    superuser.setRole(AdminRole.SUPERUSER);

    Admin normalAdmin = new Admin();
    normalAdmin.setUsername("admin");
    normalAdmin.setRole(AdminRole.MODERATOR);

    when(adminRepository.findByUsername("super")).thenReturn(Optional.of(superuser));
    when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(normalAdmin));

    adminService.deleteAdminAsSuperuser("admin", "super");

    verify(adminRepository).delete(normalAdmin);
  }
}
