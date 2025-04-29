package ntnu.idi.mushroomidentificationbackend.util;

import jakarta.persistence.EntityManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BeanUtil implements ApplicationContextAware {

  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    BeanUtil.context = applicationContext; 
  }

  public static <T> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }


}
