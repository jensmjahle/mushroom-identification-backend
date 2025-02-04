package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;

@Entity
@Getter
@Setter@NoArgsConstructor
@AllArgsConstructor
public class Admin {
  @Id
  @Column(unique = true)
  private String username;
  private String passwordHash;
  private String email;
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;
  private String firstname;
  private String lastname;
  private AdminRole role;
  @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<UserRequest> requests;

}
