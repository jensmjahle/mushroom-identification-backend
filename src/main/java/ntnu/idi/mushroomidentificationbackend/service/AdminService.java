package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.dto.request.CreateAdminDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.exception.UsernameAlreadyExistsException;
import ntnu.idi.mushroomidentificationbackend.mapper.AdminMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;
import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // Check if the superuser exists and is a superuser
    if (superuser.isEmpty() || !superuser.get().isSuperuser()) {
      throw new UnauthorizedAccessException("Only superusers can create moderators");
    }
    // Check if the username is already taken
    if (adminRepository.findByUsername(dto.getUsername()).isPresent()) {
      throw new UsernameAlreadyExistsException("Username '" + dto.getUsername() + "' is already taken.");
    }
    
    Admin newModerator = new Admin();
    newModerator.setUsername(dto.getUsername());
    newModerator.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
    newModerator.setEmail(dto.getEmail());
    newModerator.setRole(AdminRole.MODERATOR);
    adminRepository.save(newModerator);
  }

  /**
   * Get all admins in the system.
   *
   * @param pageable the pagination information
   * @return a page of AdminDTO objects
   */
  public Page<AdminDTO> getAllAdminsPaginated(Pageable pageable) {
    return adminRepository.findAll(pageable)
        .map(AdminMapper::fromEntityToDto);
  }
}
