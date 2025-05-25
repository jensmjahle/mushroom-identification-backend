package ntnu.idi.mushroomidentificationbackend.mapper;

import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;

/**
 * Utility class for mapping Admin entities to AdminDTOs.
 */
public class AdminMapper {
private AdminMapper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Maps an Admin entity to an AdminDTO.
   * This method converts the Admin entity to a DTO
   * which can be used in the response layer
   * to avoid exposing the entity directly.
   *
   * @param admin Admin entity to be converted to DTO.
   * @return AdminDTO containing the admin's details.
   */
  public static AdminDTO fromEntityToDto(Admin admin) {
    AdminDTO adminDTO = new AdminDTO();
    adminDTO.setUsername(admin.getUsername());
    adminDTO.setEmail(admin.getEmail());
    adminDTO.setRole(admin.getRole());
    adminDTO.setFirstname(admin.getFirstname());
    adminDTO.setLastname(admin.getLastname());
    return adminDTO;
  }
}
