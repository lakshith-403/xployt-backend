package com.xployt.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.*;

public class CustomLogger {
    private static final Logger logger = Logger.getLogger(CustomLogger.class.getName());

    public static void setup() throws IOException {
        // Clear existing handlers
        LogManager.getLogManager().reset();

        // // Load the logging properties file from resources
        // LogManager.getLogManager().readConfiguration(
        // CustomLogger.class.getClassLoader().getResourceAsStream("logging.properties"));

        // Create console handler with color formatter
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new ColoredFormatter());
        // consoleHandler.setLevel(Level.ALL);

        // Set up file handler
        String logFilePath = "app.log";
        File logFile = new File(logFilePath);
        if (logFile.exists()) {
            // Clear the file by writing an empty string
            Files.write(logFile.toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            // Create the file if it does not exist
            logFile.createNewFile();
        }

        FileHandler fileHandler = new FileHandler(logFilePath, false); // false to overwrite
        fileHandler.setFormatter(new ColoredFormatter());
        // fileHandler.setLevel(Level.ALL);

        // Add handlers to the root logger
        Logger rootLogger = Logger.getLogger("");
        // rootLogger.setLevel(Level.ALL); // Set the root logger level to ALL
        rootLogger.addHandler(consoleHandler);
        rootLogger.addHandler(fileHandler);
    }

    public static Logger getLogger() {
        return logger;
    }
}