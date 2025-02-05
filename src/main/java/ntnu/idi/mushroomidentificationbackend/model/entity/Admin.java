package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AdminRole role;
  @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<UserRequest> requests;

  public void setPassword(String rawPassword) {
    this.passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }
  
  public boolean isSuperuser() {
    return this.role == AdminRole.SUPERUSER;
  }
  
}
