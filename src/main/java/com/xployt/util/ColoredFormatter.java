package com.xployt.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ColoredFormatter extends Formatter {
  // ANSI escape codes for colors
  private static final String RESET = "\u001B[0m";
  private static final String RED = "\u001B[31m";
  private static final String GREEN = "\u001B[32m";
  private static final String YELLOW = "\u001B[33m";
  private static final String BLUE = "\u001B[34m";
  // private static final String PURPLE = "\u001B[35m";
  private static final String CYAN = "\u001B[36m";

  @Override
  public String format(LogRecord record) {
    StringBuilder builder = new StringBuilder();

    // Choose color based on log level
    String color;
    switch (record.getLevel().getName()) {
      case "SEVERE":
        color = RED;
        break;
      case "WARNING":
        color = YELLOW;
        break;
      case "INFO":
        color = BLUE;
        break;
       case "CONFIG":
       color = GREEN;
       break;
       case "FINE":
       case "FINER":
       case "FINEST":
       color = CYAN;
       break;
      default:
        color = RESET;
        break;
    }

    builder.append(color)
        .append("[")
        .append(record.getLevel().getName())
        .append("] ")
        .append(formatMessage(record))
        .append(RESET)
        .append(System.lineSeparator());
    return builder.toString();
  }
}