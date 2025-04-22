package ntnu.idi.mushroomidentificationbackend.mapper;

import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;

public class AdminMapper {
private AdminMapper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
  
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
