package ntnu.idi.mushroomidentificationbackend.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ntnu.idi.mushroomidentificationbackend.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

/**
 * Tracks failed admin login attempts per client/username combination and applies a temporary
 * lockout once too many failures occur in a row, to defend against brute-force and credential
 * stuffing attacks against the admin login endpoint.
 */
@Component
public class LoginAttemptService {

  private static final int MAX_ATTEMPTS = 5;
  private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

  private final ConcurrentMap<String, Attempts> attemptsByKey = new ConcurrentHashMap<>();

  /**
   * Checks whether the given key is currently locked out due to too many recent failed attempts.
   *
   * @param key an identifier for the caller, e.g. "{clientIp}:{username}"
   * @throws TooManyRequestsException if the key is currently locked out
   */
  public void checkAllowed(String key) {
    Attempts attempts = attemptsByKey.get(key);
    if (attempts != null && attempts.isLockedOut()) {
      throw new TooManyRequestsException(
          "Too many failed login attempts. Please try again later.");
    }
  }

  /**
   * Records a failed login attempt for the given key, locking it out once the failure count
   * reaches the configured threshold.
   *
   * @param key an identifier for the caller, e.g. "{clientIp}:{username}"
   */
  public void recordFailure(String key) {
    attemptsByKey.compute(key, (k, existing) -> {
      Attempts attempts = (existing == null || !existing.isWithinWindow()) ? new Attempts() : existing;
      attempts.failureCount++;
      if (attempts.failureCount >= MAX_ATTEMPTS) {
        attempts.lockedUntil = Instant.now().plus(LOCKOUT_DURATION);
      }
      return attempts;
    });
  }

  /**
   * Clears any recorded failures for the given key after a successful login.
   *
   * @param key an identifier for the caller, e.g. "{clientIp}:{username}"
   */
  public void recordSuccess(String key) {
    attemptsByKey.remove(key);
  }

  private static final class Attempts {
    private int failureCount;
    private Instant lockedUntil;
    private final Instant windowStart = Instant.now();

    private boolean isLockedOut() {
      return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    private boolean isWithinWindow() {
      return isLockedOut() || Instant.now().isBefore(windowStart.plus(LOCKOUT_DURATION));
    }
  }
}
