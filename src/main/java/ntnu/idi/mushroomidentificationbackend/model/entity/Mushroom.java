package ntnu.idi.mushroomidentificationbackend.model.entity;

import com.fasterxml.jackson.databind.DatabindException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;

@AllArgsConstructor
@Getter
@Setter
@Entity
@NoArgsConstructor
public class Mushroom {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private long mushroomId;
  private Date createdAt;
  private Date updatedAt;
  private MushroomStatus mushroomStatus;
  @ManyToOne
  @JoinColumn(name = "user_request_id")
  private UserRequest userRequest;
}
