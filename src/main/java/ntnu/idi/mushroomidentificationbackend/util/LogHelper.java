package ntnu.idi.mushroomidentificationbackend.util;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for logging messages with a specific pattern and arguments.
 * This class provides methods to log messages at different levels (INFO, WARNING, SEVERE)
 *
 */
public final class LogHelper {

  /**
   * Logs a message at the specified level with a formatted pattern and arguments.
   *
   * @param logger the logger to use for logging
   * @param level the logging level
   * @param pattern the message pattern to log
   * @param args the arguments to format the message pattern
   */
  public static void log(Logger logger, Level level, String pattern, Object... args) {
    if (logger.isLoggable(level)) {
      logger.log(level, MessageFormat.format(pattern, args));
    }
  }

  /**
   * Logs a message at the INFO level.
   *
   * @param logger the logger to use for logging
   * @param pattern the message pattern to log
   * @param args the arguments to format the message pattern
   */
  public static void info(Logger logger, String pattern, Object... args) {
    log(logger, Level.INFO, pattern, args);
  }

  /**
   * Logs a message at the WARNING level.
   *
   * @param logger the logger to use for logging
   * @param pattern the message pattern to log
   * @param args the arguments to format the message pattern
   */
  public static void warning(Logger logger, String pattern, Object... args) {
    log(logger, Level.WARNING, pattern, args);
  }
  
  /**
   * Logs a message at the SEVERE level.
   *
   * @param logger the logger to use for logging
   * @param pattern the message pattern to log
   * @param args the arguments to format the message pattern
   */
  public static void severe(Logger logger, String pattern, Object... args) {
    log(logger, Level.SEVERE, pattern, args);
  }
  /**
   * Private constructor to prevent instantiation of this utility class.
   * @throws UnsupportedOperationException if an attempt is made to instantiate this class 
   */
  private LogHelper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
