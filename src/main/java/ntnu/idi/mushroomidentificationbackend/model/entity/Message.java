package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageType;

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
  private MessageType messageType;
  private String content;
  private Date createdAt;
  @OneToOne
  @JoinColumn(name = "user_request_id")
  private UserRequest userRequest;
  

}
