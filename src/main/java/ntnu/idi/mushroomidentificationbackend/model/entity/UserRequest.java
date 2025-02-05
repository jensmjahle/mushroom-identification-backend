package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String userRequestId;
  @Column(unique = true)
  private String referenceCode;
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;
  @Temporal(TemporalType.TIMESTAMP)
  private Date updatedAt;
  private UserRequestStatus status;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "username")
  private Admin admin;
  
  public void setReferenceCode(String referenceCode) {
    this.referenceCode = referenceCode;
  }
}
