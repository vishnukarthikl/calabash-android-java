package com.thoughtworks.calabash.android;

import java.io.File;

import static com.thoughtworks.calabash.android.Environment.ENV_ANDROID_HOME;
import static com.thoughtworks.calabash.android.Environment.ENV_JAVA_HOME;

public class EnvironmentInitializer {

    public static final String KEYTOOL = Environment.getPlatformExecutable("keytool");
    public static final String JARSIGNER = Environment.getPlatformExecutable("jarsigner");
    public static final String PROPERTY_JAVA_HOME = "java.home";
    public static final String PATH_SEPARATOR = Environment.getPathSeparator();

    public static Environment initialize(AndroidConfiguration configuration) throws CalabashException {
        String javaHome = null;
        String androidHome = findAndroidHome(configuration);
        String keytool = findExecutableFromPath(KEYTOOL);
        String jarsigner = findExecutableFromPath(JARSIGNER);
        if (keytool == null || jarsigner == null) {
            CalabashLogger.info("Finding executables relative to " + ENV_JAVA_HOME);
            javaHome = findJavaHome(configuration);

            if (javaHome == null) {
                throw new CalabashException(ENV_JAVA_HOME + " is not set");
            }
            File bin = new File(javaHome, "bin");
            keytool = getPathIfExists(new File(bin, KEYTOOL));
            jarsigner = getPathIfExists(new File(bin, JARSIGNER));
        }
        return new Environment(androidHome, javaHome, keytool, jarsigner);
    }

    private static String getPathIfExists(File executable) throws CalabashException {
        if (!executable.exists())
            throw new CalabashException("Could not find " + executable.getName() + ". Ensure JDK is installed");
        return executable.getAbsolutePath();
    }

    private static String findExecutableFromPath(String execName) {
        String path = System.getenv("PATH");
        String[] pathEntries = path.split(PATH_SEPARATOR);
        for (String pathEntry : pathEntries) {
            File executable = new File(pathEntry, execName);
            if (executable.exists()) {
                CalabashLogger.info(String.format(String.format("%s found at %s", execName, executable.getAbsolutePath())));
                return executable.getAbsolutePath();
            }
        }
        return null;
    }

    private static String findJavaHome(AndroidConfiguration configuration) throws CalabashException {
        String javaHomeFromConfig = configuration.getJavaHome();
        if (isNotEmpty(javaHomeFromConfig)) {
            CalabashLogger.info(String.format("%s = %s from configuration", ENV_JAVA_HOME, javaHomeFromConfig));
            return javaHomeFromConfig;
        }
        String javaHomeFromSystemProp = System.getProperty(PROPERTY_JAVA_HOME);

        if (isNotEmpty(javaHomeFromSystemProp)) {

            CalabashLogger.info(String.format("%s = %s from system property", PROPERTY_JAVA_HOME, javaHomeFromSystemProp));
            return javaHomeFromSystemProp;
        }

        String javaHomeFromEnv = System.getenv(ENV_JAVA_HOME);
        if (isNotEmpty(javaHomeFromEnv)) {

            CalabashLogger.info(String.format("%s = %s from environment variable", ENV_JAVA_HOME, javaHomeFromEnv));
            return javaHomeFromEnv;
        }
        return null;
    }

    private static String findAndroidHome(AndroidConfiguration configuration) throws CalabashException {
        String androidHome = null;

        String androidHomeFromConfig = configuration.getAndroidHome();
        if (isNotEmpty(androidHomeFromConfig)) {
            androidHome = androidHomeFromConfig;
            CalabashLogger.info(String.format("%s = %s from configuration", ENV_ANDROID_HOME, androidHome));
        } else {
            String envValue = System.getenv(ENV_ANDROID_HOME);
            if (isNotEmpty(envValue)) {
                androidHome = envValue;
                CalabashLogger.info(String.format("%s = %s from environment", ENV_ANDROID_HOME, androidHome));
            }
        }
        if (androidHome == null) {
            throw new CalabashException("Could not find " + ENV_ANDROID_HOME);
        }
        return androidHome;

    }

    private static boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}
