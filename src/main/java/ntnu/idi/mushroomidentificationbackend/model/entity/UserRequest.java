package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String requestID;
  private String imageURL;
  private String status;
  private Date sentDate;
  private Date lastUpdatedDate;
  
}
