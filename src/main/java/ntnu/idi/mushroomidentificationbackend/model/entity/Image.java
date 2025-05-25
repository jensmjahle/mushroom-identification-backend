package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity representing an image associated with a mushroom.
 * This entity is used to store the URL of the image
 * and its association with a specific mushroom.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@ToString
public class Image {
  @Id
  private String imageUrl;
  @ManyToOne
  @JoinColumn(name = "mushroom_id", nullable = false)
  private Mushroom mushroom;
}
