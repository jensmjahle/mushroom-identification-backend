package ntnu.idi.mushroomidentificationbackend.service;

import java.util.Date;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangePasswordDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.CreateAdminDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateProfileDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.exception.InvalidInputException;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.exception.UserNotFoundException;
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
  public void createAdmin(String superuserUsername, CreateAdminDTO dto) {
    Optional<Admin> superuser = adminRepository.findByUsername(superuserUsername);

    // 1. Check superuser rights
    if (superuser.isEmpty() || !superuser.get().isSuperuser()) {
      throw new UnauthorizedAccessException("Only superusers can create admins");
    }

    // 2. Normalize and validate username
    String normalizedUsername = dto.getUsername().trim().toLowerCase();
    if (adminRepository.findByUsername(normalizedUsername).isPresent()) {
      throw new UsernameAlreadyExistsException("Username '" + normalizedUsername + "' is already taken.");
    }

    // 3. Validate password
    validatePassword(dto.getPassword());

    // 4. Normalize and validate email
    String normalizedEmail = dto.getEmail().trim().toLowerCase();
    validateEmail(normalizedEmail);

    // 5. Check email uniqueness
    Optional<Admin> existingByEmail = adminRepository.findByEmail(normalizedEmail);
    if (existingByEmail.isPresent()) {
      throw new IllegalArgumentException("Email '" + normalizedEmail + "' is already taken.");
    }

    // 6. Create and save the admin
    Admin newAdmin = new Admin();
    newAdmin.setUsername(normalizedUsername);
    newAdmin.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
    newAdmin.setEmail(normalizedEmail);
    newAdmin.setRole(dto.getRole());
    newAdmin.setCreatedAt(new Date());

    adminRepository.save(newAdmin);
  }
  private void validateEmail(String email) {
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    if (!email.matches(emailRegex)) {
      throw new InvalidInputException("Invalid email format");
    }
  }


  private void validatePassword(String password) {
    if (password.length() < 8) {
      throw new IllegalArgumentException("Password must be at least 8 characters long");
    }
    if (password.length() > 50) {
      throw new IllegalArgumentException("Password must be at most 50 characters long");
    }
    if (password.contains(" ")) {
      throw new IllegalArgumentException("Password cannot contain spaces");
    }
    if (!password.matches(".*\\d.*")) {
      throw new IllegalArgumentException("Password must contain at least one number");
    }
    if (!password.matches(".*[A-Z].*")) {
      throw new IllegalArgumentException("Password must contain at least one upper case letter");
    }
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

    if (adminOptional.isEmpty()) {
      throw new UnauthorizedAccessException("Admin not found");
    }

    Admin admin = adminOptional.get();

    // Normalize and validate email
    String normalizedEmail = request.getEmail().trim().toLowerCase();
    if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
      throw new InvalidInputException("Invalid email format");
    }

    Optional<Admin> existingByEmail = adminRepository.findByEmail(normalizedEmail);
    if (existingByEmail.isPresent() && !existingByEmail.get().getUsername().equals(admin.getUsername())) {
      throw new IllegalArgumentException("Email '" + normalizedEmail + "' is already taken.");
    }

    // Apply updates
    admin.setEmail(normalizedEmail);
    admin.setFirstname(request.getFirstname());
    admin.setLastname(request.getLastname());

    adminRepository.save(admin);
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
    if(request.getNewPassword().length() > 50) {
      throw new IllegalArgumentException("New password must be at most 50 characters long");
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

  /**
   * Delete an admin from the system.
   *
   * @param username the username of the admin to delete
   */
  public void deleteAdmin(String username) {
    Optional<Admin> adminOptional = adminRepository.findByUsername(username);
    if (adminOptional.isEmpty()) {
      throw new UserNotFoundException("Admin not found");
    }
    Admin admin = adminOptional.get();
    adminRepository.delete(admin);
  }

  public AdminDTO getAdminDTO(String username) {
    Optional<Admin> adminOptional = adminRepository.findByUsername(username);
    if (adminOptional.isEmpty()) {
      throw new UserNotFoundException("Admin not found");
    }
    Admin admin = adminOptional.get();
    return AdminMapper.fromEntityToDto(admin);
  }
  
  public Admin getAdmin(String username) {
    Optional<Admin> adminOptional = adminRepository.findByUsername(username);
    if (adminOptional.isEmpty()) {
      throw new UserNotFoundException("Admin not found");
    }
    return adminOptional.get();
  }

  public void deleteAdminAsSuperuser(String username, String adminUsername) {
    Optional<Admin> superuserOptional = adminRepository.findByUsername(adminUsername.trim());
    if (superuserOptional.isEmpty() || !superuserOptional.get().isSuperuser()) {
      throw new UnauthorizedAccessException("Only superusers can delete admins");
    }
    Optional<Admin> adminOptional = adminRepository.findByUsername(username.trim());
    if (adminOptional.isEmpty()) {
      throw new UserNotFoundException("Admin not found");
    }
    Admin admin = adminOptional.get();
    if (admin.isSuperuser()) {
      throw new UnauthorizedAccessException("Cannot delete a superuser");
    }
    adminRepository.delete(admin);
  }
}
