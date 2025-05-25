package ntnu.idi.mushroomidentificationbackend.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

/**
 * Generates a random reference code consisting of 5 words.
 * The words are selected from a predefined list
 * loaded from a file named "eff_large_wordlist.txt".
 */
@Component
public class ReferenceCodeGenerator {

  private final List<String> wordList;
  private final SecureRandom random = new SecureRandom();

  public ReferenceCodeGenerator() throws IOException {
    this.wordList = loadWordList(); 
  }

  /**
   * Generates a random reference code consisting of 5 words.
   * Each word is capitalized and concatenated together.
   * The words are randomly selected from a predefined list
   * loaded from a file named "eff_large_wordlist.txt".
   *
   * @return A string representing the generated reference code.
   */
  public String generateCode() {
    return IntStream.range(0, 5)
        .mapToObj(i -> {
          String word = wordList.get(random.nextInt(wordList.size()));
          return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        })
        .collect(Collectors.joining());
  }

  /**
   * Loads a list of words from the "eff_large_wordlist.txt" file.
   *
   * @return A list of words loaded from the file.
   * @throws IOException If an error occurs while reading the file.
   */
  public List<String> loadWordList() throws IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("eff_large_wordlist.txt");
    assert inputStream != null;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      return reader.lines()
          .map(line -> line.split("\\s+")[1]) // format: 11111 word
          .collect(Collectors.toList());
    }
  }

}
