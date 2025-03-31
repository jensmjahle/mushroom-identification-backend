package ntnu.idi.mushroomidentificationbackend.repository;

import java.util.List;
import ntnu.idi.mushroomidentificationbackend.model.entity.Image;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {

  List<Image> findAllByMushroom(Mushroom mushroom);
}
