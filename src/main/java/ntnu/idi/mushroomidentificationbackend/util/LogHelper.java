package ntnu.idi.mushroomidentificationbackend.util;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogHelper {

  public static void log(Logger logger, Level level, String pattern, Object... args) {
    if (logger.isLoggable(level)) {
      logger.log(level, MessageFormat.format(pattern, args));
    }
  }

  public static void info(Logger logger, String pattern, Object... args) {
    log(logger, Level.INFO, pattern, args);
  }

  public static void warning(Logger logger, String pattern, Object... args) {
    log(logger, Level.WARNING, pattern, args);
  }

  public static void severe(Logger logger, String pattern, Object... args) {
    log(logger, Level.SEVERE, pattern, args);
  }
}
