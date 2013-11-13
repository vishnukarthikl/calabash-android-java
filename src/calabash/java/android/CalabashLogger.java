package calabash.java.android;

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

				String logFile = new File(configuration.getLogsDirectory(),"calabash-android-java.log").getAbsolutePath();
				RollingFileAppender fileAppender = new RollingFileAppender(new PatternLayout("%d %-5p - %m%n"), logFile);
				fileAppender.setMaxFileSize("20MB");
				fileAppender.setAppend(true);
				fileAppender.activateOptions();

				Logger.getRootLogger().removeAllAppenders();
				Logger.getRootLogger().addAppender(fileAppender);
				log = Logger.getLogger(CalabashLogger.class);
				log.setLevel(Level.INFO);
				shouldLog = true;
			} catch (IOException e) {
				throw new CalabashException("Can't setup logging system. "+ e.getMessage(), e);
			}
		}
	}

	public static void info(Object message) {
		if (shouldLog)
			log.info(message);
	}

	public static void info(String message, Object... args) {
		if (shouldLog)
			log.info(String.format(message, args));
	}

	public static void error(Object message) {
		if (shouldLog)
			log.error(message);
	}

	public static void error(String message, Object... args) {
		if (shouldLog)
			log.error(String.format(message, args));
	}

	public static void error(String message, Throwable cause, Object... args) {
		if (shouldLog)
			log.error(String.format(message, args), cause);
	}

	public static void error(Object message, Throwable cause) {
		if (shouldLog)
			log.error(message, cause);
	}
}
