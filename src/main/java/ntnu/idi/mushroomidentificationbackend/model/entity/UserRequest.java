package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;


@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String userRequestId;
  @Column(unique = true)
  private String lookUpKey;
  @Column(unique = true)
  private String passwordHash;
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;
  @Temporal(TemporalType.TIMESTAMP)
  private Date updatedAt;
  private UserRequestStatus status;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "username")
  @Exclude
  private Admin admin;
  
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  @PrePersist
  protected void onCreate() {
    Date now = new Date();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = new Date();
  }
}
