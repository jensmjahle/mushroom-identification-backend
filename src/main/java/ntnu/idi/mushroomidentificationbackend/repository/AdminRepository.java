package ntnu.idi.mushroomidentificationbackend.repository;

import java.util.Date;
import java.util.Optional;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {

  Optional<Admin> findByUsername(String superuserUsername);
  
  int deleteByCreatedAtBefore(Date dateThreshold);

  Optional<Object> findByEmail(String email);
}
