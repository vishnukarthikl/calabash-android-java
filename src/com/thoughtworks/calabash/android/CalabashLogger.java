package com.thoughtworks.calabash.android;

import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;

class CalabashLogger {

    private static boolean shouldLog = false;
    private static Logger log = null;

    public static void initialize(AndroidConfiguration configuration)
            throws CalabashException {
        if (configuration != null && configuration.isLoggingEnabled()) {
            try {
                BasicConfigurator.configure();

                String logFile = new File(configuration.getLogsDirectory(), "calabash-android-java.log").getAbsolutePath();
                RollingFileAppender fileAppender = new RollingFileAppender(new PatternLayout("%d %-5p - %m%n"), logFile);
                fileAppender.setMaxFileSize("20MB");
                fileAppender.setAppend(true);
                fileAppender.activateOptions();

                log = Logger.getLogger(CalabashLogger.class);
                log.removeAllAppenders();

                log.setAdditivity(false);
                log.addAppender(fileAppender);
                log.setLevel(Level.INFO);
                shouldLog = true;
            } catch (IOException e) {
                throw new CalabashException("Can't setup logging system. " + e.getMessage(), e);
            }
        }
    }

    public static void info(Object message) {
        if (shouldLog && isNotEmpty(message)) {
            log.info(message);
        }
    }

    private static boolean isNotEmpty(Object message) {
        return !(message == null || message.toString().isEmpty());
    }

    public static void info(String message, Object... args) {
        if (shouldLog && isNotEmpty(message))
            log.info(String.format(message, args));
    }

    public static void warn(String message) {
        if (shouldLog && isNotEmpty(message))
            log.warn(String.format(message));
    }

    public static void error(Object message) {
        if (shouldLog && isNotEmpty(message))
            log.error(message);
    }

    public static void error(String message, Object... args) {
        if (shouldLog && isNotEmpty(message))
            log.error(String.format(message, args));
    }

    public static void error(String message, Throwable cause, Object... args) {
        if (shouldLog && isNotEmpty(message))
            log.error(String.format(message, args), cause);
    }

    public static void error(Object message, Throwable cause) {
        if (shouldLog && isNotEmpty(message))
            log.error(message, cause);
    }
}
