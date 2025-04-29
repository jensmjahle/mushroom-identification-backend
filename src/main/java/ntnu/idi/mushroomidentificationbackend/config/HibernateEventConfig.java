package ntnu.idi.mushroomidentificationbackend.config;

import ntnu.idi.mushroomidentificationbackend.listener.UserRequestEntityListener;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

@Configuration
public class HibernateEventConfig {

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  @Autowired
  private UserRequestEntityListener userRequestEntityListener;

  @PostConstruct
  public void registerListeners() {
    SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
    EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

    assert registry != null;
    registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(userRequestEntityListener);
    registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(userRequestEntityListener);
  }
}
