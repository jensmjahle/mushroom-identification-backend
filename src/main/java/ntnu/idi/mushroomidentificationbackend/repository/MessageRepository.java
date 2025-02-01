package ntnu.idi.mushroomidentificationbackend.repository;

import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
}
