package com.xployt.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class SQLLoader {
  private static final Logger logger = Logger.getLogger(SQLLoader.class.getName());

  public static String loadSQL(String filename) throws Exception {
    logger.info("Loading SQL file: " + filename);
    Path path = Paths.get(ClassLoader.getSystemResource("sql/" + filename).toURI());
    logger.info("Loading SQL file: " + path);
    return Files.readString(path);
  }
}
