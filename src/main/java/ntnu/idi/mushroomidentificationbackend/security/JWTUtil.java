package ntnu.idi.mushroomidentificationbackend.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import org.springframework.stereotype.Component;

/**
 * Utility class for handling JSON Web Tokens (JWT).
 */
@Component
public class JWTUtil {

  private static final long EXPIRATION_TIME = 86400000; // 1 day
  private static final long IMAGE_URL_EXPIRATION = 86400000; // 1 day
  private final Key key;
  private static final Logger logger = Logger.getLogger(JWTUtil.class.getName());

  public JWTUtil(SecretsConfig secretsConfig) {
    String secretKey = secretsConfig.getSecretKey();
    if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
      logger.severe("SECRET_KEY is too short or missing. Using fallback key. NOTE: This is not secure!");
      secretKey = "defaultSecretKey-super-duper-key-secrets-yes-defaultSecretKey-super-duper-key-secrets-yes";
    }

    key = Keys.hmacShaKeyFor(secretKey.getBytes());
  }
  
  /**
   * Generates a JWT token for the given username and role.
   * The token includes the username as the subject and the role as a claim.
   * The token is signed with the secret key and has an expiration time.
   *
   * @param username the username of the user
   * @param role the role of the user (e.g., ADMIN, USER)
   * @return a signed JWT token
   */
  public String generateToken(String username, String role) {
    return Jwts.builder()
        .setSubject(username)
        .claim("role", role)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Generates a signed image URL for a mushroom image.
   * This method creates a JWT token that includes the user request ID,
   * mushroom ID, and the filename of the image.
   * The token is signed with the secret key and has an expiration time.
   * This URL can be used to securely access the image
   * without exposing sensitive information.
   *
   * @param userRequestId the ID of the user request associated with the mushroom
   * @param mushroomId the ID of the mushroom
   * @param filename the name of the image file
   * @return a signed JWT token representing the image URL
   */
  public String generateSignedImageUrl(String userRequestId, String mushroomId, String filename) {
    return Jwts.builder()
        .setSubject(filename)
        .claim("userRequestId", userRequestId)
        .claim("mushroomId", mushroomId)
        .setExpiration(new Date(System.currentTimeMillis() + IMAGE_URL_EXPIRATION))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Validates a signed image URL and extracts the user request ID and mushroom ID.
   * If the token is invalid or expired, it returns null.
   *
   * @param token the signed JWT token
   * @return the image URL if valid, null otherwise
   */
  public String validateSignedImageUrl(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token)
          .getBody();

      String userRequestId = claims.get("userRequestId", String.class);
      String mushroomId = claims.get("mushroomId", String.class);
      String filename = claims.getSubject();
      return "uploads/" + userRequestId + "/" + mushroomId + "/" + filename;
    } catch (Exception e) {
      logger.warning("Invalid or expired signed image URL: " + e.getMessage());
      return null;
    }
  }
  /**
   * Extracts the username from the JWT token.
   *
   * @param token the JWT token
   * @return the username contained in the token
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the role from the JWT token.
   *
   * @param token the JWT token
   * @return the role contained in the token
   */
  public String extractRole(String token) {
    return extractAllClaims(token).get("role", String.class);
  }

  /**
   * Extracts the user request ID from the JWT token.
   *
   * @param token the JWT token
   * @return the user request ID contained in the token
   */
  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extracts all claims from the JWT token.
   *
   * @param token the JWT token
   * @return the claims contained in the token
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Checks if the token is valid and not expired.
   *
   * @param token the JWT token to validate
   * @return true if the token is valid, false otherwise
   */
  public boolean isTokenValid(String token) {
    try {
      return !isTokenExpired(token);
    } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Checks if the token is expired.
   *
   * @param token the JWT token to check
   * @return true if the token is expired, false otherwise
   */
  private boolean isTokenExpired(String token) {
    return extractAllClaims(token).getExpiration().before(new Date());
  }

  /**
   * Validates the chatroom token for sending messages.
   * This method checks if the user has the required role
   * or if the userRequestId matches the one in the token.
   * If neither condition is met, it throws an UnauthorizedAccessException.
   *
   * @param bearer the Bearer token string containing the JWT
   * @param userRequestId the user request ID to validate against the token
   */
  public void validateChatroomToken(String bearer, String userRequestId) {
    String token = bearer.replace("Bearer ", "");
    String userRequestIdFromToken = extractUsername(token);
    String role = extractRole(token);
    
    if (!role.equals("SUPERUSER") && !role.equals("MODERATOR") && !userRequestIdFromToken.equals(userRequestId)) {
        throw new UnauthorizedAccessException("Unauthorized: You cannot send messages to this chat.");
      }
  }
}
