package ntnu.idi.mushroomidentificationbackend.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
  public static void main(String[] args) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String rawPassword = "SuperSecurePassword123"; // Change this to your desired password
    String hashedPassword = encoder.encode(rawPassword);
    System.out.println("Hashed Password: " + hashedPassword);
  }
}
