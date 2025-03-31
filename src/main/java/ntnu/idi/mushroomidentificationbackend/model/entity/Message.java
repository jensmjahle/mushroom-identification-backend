package ntnu.idi.mushroomidentificationbackend.model.entity;

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
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String messageId;
  private MessageSenderType senderType;
  private String content;
  private Date createdAt;
  @ManyToOne
  @JoinColumn(name = "user_request_id")
  private UserRequest userRequest;
  

}
