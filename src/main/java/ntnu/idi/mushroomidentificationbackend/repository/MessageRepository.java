package ntnu.idi.mushroomidentificationbackend.repository;

import java.util.Date;
import java.util.List;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
  int deleteByCreatedAtBefore(Date dateThreshold);
  List<Message> findByUserRequestOrderByCreatedAtAsc(UserRequest userRequest);
}
