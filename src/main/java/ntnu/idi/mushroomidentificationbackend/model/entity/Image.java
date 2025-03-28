package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Image {
  @Id
  private String imageUrl;
  @ManyToOne
  @JoinColumn(name = "mushroom_id")
  private Mushroom mushroom;
}
