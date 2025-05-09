package ntnu.idi.mushroomidentificationbackend.security;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "app.secrets")
public class SecretsConfig {

  private String secretKey;
  private String lookupSalt;

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public void setLookupSalt(String lookupSalt) {
    this.lookupSalt = lookupSalt;
  }
}
