package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.dto.request.ChangePasswordDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.CreateAdminDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateProfileDTO;
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

  /**
   * Update the profile of an admin.
   * Checks if the admin exists and
   * if the new email is valid and not taken.
   *
   * @param request the request containing the new profile information
   * @param username the username of the admin
   */
  @Transactional
  public void updateProfile(UpdateProfileDTO request, String username) {
    Optional<Admin> adminOptional = adminRepository.findByUsername(username);
    if (adminOptional.isPresent()) {
      Admin admin = adminOptional.get();
      admin.setFirstname(request.getFirstname());
      admin.setLastname(request.getLastname());
      if (adminRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new UsernameAlreadyExistsException("Email '" + request.getEmail() + "' is already taken.");
      }
      if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
        throw new IllegalArgumentException("Invalid email format");
      }
      admin.setEmail(request.getEmail());
      adminRepository.save(admin);
    } else {
      throw new UnauthorizedAccessException("Admin not found");
    }
  }

  /**
   * Change the password of an admin.
   * Checks if the old password is correct,
   * the new password meets the criteria,
   * if the admin exists and 
   * if the new password and confirm password match.
   * The new password must be at least eight characters long,
   * at most 20 characters long,
   * cannot contain spaces,
   * must contain at least one number,
   * and at least one upper case letter.
   *
   * @param request the request containing the old, new, and confirm password
   * @param username the username of the admin
   */
  public void changePassword(ChangePasswordDTO request, String username) {
    Optional<Admin> adminOptional = adminRepository.findByUsername(username);
    if (adminOptional.isEmpty()) {
      throw new UnauthorizedAccessException("Admin not found");
    }
    Admin admin = adminOptional.get();
    if (!passwordEncoder.matches(request.getOldPassword(), admin.getPasswordHash())) {
      throw new UnauthorizedAccessException("Old password is incorrect");
    }
    if(request.getNewPassword().length() < 8) {
      throw new IllegalArgumentException("New password must be at least 8 characters long");
    }
    if(request.getNewPassword().length() > 20) {
      throw new IllegalArgumentException("New password must be at most 20 characters long");
    }
    if(request.getNewPassword().contains(" ")) {
      throw new IllegalArgumentException("New password cannot contain spaces");
    }
    if(!request.getNewPassword().matches(".*\\d.*")) {
      throw new IllegalArgumentException("New password must contain at least one number");
    }
    if(!request.getNewPassword().matches(".*[A-Z].*")) {
      throw new IllegalArgumentException("New password must contain at least one upper case letter");
    }
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new IllegalArgumentException("New password and confirm password do not match");
    }
    admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    adminRepository.save(admin);
  }
}
