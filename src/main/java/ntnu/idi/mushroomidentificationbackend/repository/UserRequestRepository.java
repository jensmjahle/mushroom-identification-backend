package ntnu.idi.mushroomidentificationbackend.repository;

import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRequestRepository extends JpaRepository<UserRequest, String> {
}
