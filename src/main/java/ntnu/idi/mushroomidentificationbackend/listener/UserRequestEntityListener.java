package ntnu.idi.mushroomidentificationbackend.listener;

import jakarta.transaction.Transactional;
import ntnu.idi.mushroomidentificationbackend.model.entity.Image;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class UserRequestEntityListener implements PostInsertEventListener, PostUpdateEventListener {

  @Transactional
  @Override
  public void onPostInsert(PostInsertEvent event) {
    handleEntityChange(event.getEntity());
  }

  @Transactional
  @Override
  public void onPostUpdate(PostUpdateEvent event) {
    handleEntityChange(event.getEntity());
  }

  private void handleEntityChange(Object entity) {
    System.out.println("Entity changed: " + entity.getClass().getSimpleName());
    if (entity instanceof Message msg) {
      updateUserRequest(msg.getUserRequest());
    } else if (entity instanceof Mushroom mushroom) {
      updateUserRequest(mushroom.getUserRequest());
    } else if (entity instanceof Image image && image.getMushroom() != null) {
      updateUserRequest(image.getMushroom().getUserRequest());
    }
  }

  private void updateUserRequest(UserRequest userRequest) {
    if (userRequest != null) {
      userRequest.setUpdatedAt(new Date());
      System.out.println("Updated UserRequest: " + userRequest);
    }
  }
  
  @Override
  public boolean requiresPostCommitHandling(EntityPersister entityPersister) {
    return false;
  }
}
