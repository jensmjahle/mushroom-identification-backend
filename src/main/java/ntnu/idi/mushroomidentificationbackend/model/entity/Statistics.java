package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

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
