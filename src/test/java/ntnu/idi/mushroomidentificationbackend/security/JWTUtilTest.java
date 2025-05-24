package ntnu.idi.mushroomidentificationbackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTUtilTest {

  private JWTUtil jwtUtil;
  private Key testKey;

  @BeforeEach
  void setUp() throws Exception {
    SecretsConfig secretsConfig = mock(SecretsConfig.class);
    when(secretsConfig.getSecretKey()).thenReturn("super-secret-key-for-testing-super-secret-key-for-testing");
    jwtUtil = new JWTUtil(secretsConfig);

    Field keyField = JWTUtil.class.getDeclaredField("key");
    keyField.setAccessible(true);
    testKey = (Key) keyField.get(jwtUtil);
  }

  @Test
  void generateToken_and_extractClaims() {
    String token = jwtUtil.generateToken("testuser", "ADMIN");

    assertTrue(jwtUtil.isTokenValid(token));
    assertEquals("testuser", jwtUtil.extractUsername(token));
    assertEquals("ADMIN", jwtUtil.extractRole(token));
  }

  @Test
  void generateSignedImageUrl_and_validate() {
    String token = jwtUtil.generateSignedImageUrl("user123", "mush123", "img.jpg");
    String path = jwtUtil.validateSignedImageUrl(token);
    assertEquals("uploads/user123/mush123/img.jpg", path);
  }

  @Test
  void validateSignedImageUrl_invalidToken_returnsNull() {
    String result = jwtUtil.validateSignedImageUrl("invalid.token.structure");
    assertNull(result);
  }

  @Test
  void validateChatroomToken_withProperRole_succeeds() {
    String token = jwtUtil.generateToken("req123", "SUPERUSER");
    assertDoesNotThrow(() -> jwtUtil.validateChatroomToken("Bearer " + token, "req123"));
  }

  @Test
  void validateChatroomToken_withWrongUser_throwsUnauthorized() {
    String token = jwtUtil.generateToken("someoneElse", "USER");
    assertThrows(UnauthorizedAccessException.class, () ->
        jwtUtil.validateChatroomToken("Bearer " + token, "req123")
    );
  }

  @Test
  void isTokenValid_withExpiredToken_returnsFalse() {
    Date now = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24); 
    Date expiry = new Date(now.getTime() - 1000);

    String expiredToken = Jwts.builder()
        .setSubject("user")
        .claim("role", "ADMIN")
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(testKey, SignatureAlgorithm.HS256)
        .compact();

    assertFalse(jwtUtil.isTokenValid(expiredToken));
  }
}
