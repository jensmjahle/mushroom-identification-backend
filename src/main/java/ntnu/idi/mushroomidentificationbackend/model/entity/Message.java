package ntnu.idi.mushroomidentificationbackend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
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
import lombok.ToString;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;

/**
 * Entity representing a message in the system.
 * This entity is used to store messages sent by users or administrators
 * in the context of user requests.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String messageId;
  private MessageSenderType senderType;
  private String content;
  private Date createdAt;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_request_id", nullable = false)
  private UserRequest userRequest;
  

}
