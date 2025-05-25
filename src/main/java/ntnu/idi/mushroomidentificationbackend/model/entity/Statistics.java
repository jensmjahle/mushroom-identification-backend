package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * Entity representing statistics for the system.
 * This entity is used to store various statistics
 * related to user requests, mushroom identifications,
 * and other relevant metrics.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Statistics {
  @Id
  private String monthYear; // Format: YYYY-MM
  @Nullable
  private long totalNewRequests;
  @Nullable
  private long totalRequestsCompleted;
  @Nullable
  private long ftrClicks;
  @Nullable
  private long totalPsilocybinIdentified;
  @Nullable
  private long totalNonPsilocybinIdentified;
  @Nullable
  private long totalToxicIdentified;
  @Nullable
  private long totalUnknownIdentified;
  @Nullable
  private long totalUnidentifiableIdentified;
}
