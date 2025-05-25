package ntnu.idi.mushroomidentificationbackend.security;

import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for managing application secrets.
 * This class retrieves secret keys and salts from application properties
 * and provides fallback values if they are not set.
 */
@Component
@ConfigurationProperties(prefix = "app.secrets")
public class SecretsConfig {

  private String secretKey;
  private String lookupSalt;
  private final Logger logger = Logger.getLogger(SecretsConfig.class.getName());

  /**
   * Sets the secret key used for cryptographic operations.
   *
   * @param secretKey the secret key to be set
   */
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  /**
   * Sets the lookup salt used for hashing operations.
   *
   * @param lookupSalt the lookup salt to be set
   */
  public void setLookupSalt(String lookupSalt) {
    this.lookupSalt = lookupSalt;
  }

  /**
   * Retrieves the secret key used for cryptographic operations.
   * 
   * @return the secret key, or a fallback value if not set
   */
  public String getSecretKey() {
    if (secretKey == null || secretKey.isBlank()) {
      LogHelper.warning(logger, "WARNING: SECRET_KEY not set. Using fallback. NOTE: This is not secure!");
      return "fallback-secret-key-please-set-in-env-fallback-secret-key-please-set-in-env";
    }
    return secretKey;
  }

  /**
   * Retrieves the lookup salt used for hashing operations.
   *
   * @return the lookup salt, or a fallback value if not set
   */
  public String getLookupSalt() {
    if (lookupSalt == null || lookupSalt.isBlank()) {
      LogHelper.warning(logger, "WARNING: LOOKUP_SALT not set. Using fallback. NOTE: This is not secure!");
      return "fallback-lookup-salt";
    }
    return lookupSalt;
  }
}
