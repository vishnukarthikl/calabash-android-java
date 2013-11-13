package calabash.java.android;

import java.io.File;

import static calabash.java.android.Environment.ENV_ANDROID_HOME;
import static calabash.java.android.Environment.ENV_JAVA_HOME;

public class EnvironmentInitializer {

    public static final String KEYTOOL = "keytool";
    public static final String JARSIGNER = "jarsigner";

    public static Environment initiaize(AndroidConfiguration configuration) throws CalabashException {
        String javaHome = null;
        String androidHome = findAndroidHome(configuration);
        String keytool = findExecutableFromPath("keytool");
        String jarsigner = findExecutableFromPath("jarsigner");
        if (keytool == null || jarsigner == null) {
            javaHome = findJavaHome(configuration);

            if (javaHome == null) {
                throw new CalabashException(ENV_JAVA_HOME + " is not set");
            }
            File bin = new File(javaHome, "bin");
            keytool = new File(bin, getExecutable(KEYTOOL)).getAbsolutePath();
            jarsigner = new File(bin, getExecutable(JARSIGNER)).getAbsolutePath();
        }
        return new Environment(androidHome, javaHome, keytool, jarsigner);
    }

    private static String findExecutableFromPath(String execName) {
        String path = System.getenv("PATH");
        String[] pathEntries = path.split(":");
        for (String pathEntry : pathEntries) {
            File executable = new File(pathEntry, getExecutable(execName));
            if (executable.exists()) {
                return executable.getAbsolutePath();
            }
        }
        return null;
    }

    private static String findJavaHome(AndroidConfiguration configuration) throws CalabashException {
        String javaHomeFromConfig = configuration.getJavaHome();
        if (isNotEmpty(javaHomeFromConfig)) {
            return javaHomeFromConfig;
        }
        String javaHomeFromEnv = System.getenv(ENV_JAVA_HOME);
        if (isNotEmpty(javaHomeFromEnv)) {
            return javaHomeFromEnv;
        }
        return null;
    }

    private static String getExecutable(String executable) {
        return System.getProperty("os.name").toLowerCase().contains("win") ? executable + ".exe" : executable;
    }

    private static String findAndroidHome(AndroidConfiguration configuration) throws CalabashException {
        String androidHome = null;

        String androidHomeFromConfig = configuration.getAndroidHome();
        if (isNotEmpty(androidHomeFromConfig)) {
            androidHome = androidHomeFromConfig;
        } else {
            String envValue = System.getenv(ENV_ANDROID_HOME);
            if (isNotEmpty(envValue)) {
                androidHome = envValue;
            }
        }
        if (androidHome == null) {
            throw new CalabashException("Could not find " + ENV_ANDROID_HOME);
        }
        if (isValidAndroidHome(androidHome)) {
            return androidHome;
        } else {
            throw new CalabashException(String.format("Invalid %s %s ", ENV_ANDROID_HOME, androidHome));
        }

    }

    private static boolean isValidAndroidHome(String androidHome) {
        File adb = new File(androidHome + File.separator + "platform-tools" + File.separator + getExecutable("adb"));
        return adb.exists();
    }

    private static boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}