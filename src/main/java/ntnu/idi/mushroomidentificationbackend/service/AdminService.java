package ntnu.idi.mushroomidentificationbackend.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.model.dto.CreateAdminDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;
import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service

public class AdminService {
  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;

public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
    this.adminRepository = adminRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public void createModerator(String superuserUsername, CreateAdminDTO dto) {
    Optional<Admin> superuser = adminRepository.findByUsername(superuserUsername);
    if (superuser.isEmpty() || !superuser.get().isSuperuser()) {
      throw new SecurityException("Unauthorized: Only superusers can create new moderators");
    }

    Admin newModerator = new Admin();
    newModerator.setUsername(dto.getUsername());
    newModerator.setPasswordHash(passwordEncoder.encode(dto.getPassword())); // Hash password
    newModerator.setEmail(dto.getEmail());
    newModerator.setRole(AdminRole.MODERATOR);
    adminRepository.save(newModerator);
  }
  
}
