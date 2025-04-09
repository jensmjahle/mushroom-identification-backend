package ntnu.idi.mushroomidentificationbackend.repository;

import java.util.List;
import java.util.Optional;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MushroomRepository extends JpaRepository<Mushroom, String> {


  List<Mushroom> findByUserRequest(Optional<UserRequest> userRequest);

  @Query("SELECT m.mushroomStatus, COUNT(m) FROM Mushroom m GROUP BY m.mushroomStatus")
  List<Object[]> countMushroomsByStatus();
}
