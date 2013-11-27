package calabash.java.android;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static calabash.java.android.Utils.isWindows;

public class Environment {
    public static final String ENV_ANDROID_HOME = "ANDROID_HOME";
    public static final String ENV_JAVA_HOME = "JAVA_HOME";
    private final String keytool;
    private final String jarsigner;
    private final Map<String, String> envVariables = new HashMap<String, String>();
    private final String androidHome;

    public Environment(String androidHome, String javaHome, String keytool, String jarsigner) throws CalabashException {
        this.keytool = keytool;
        this.jarsigner = jarsigner;
        if (!isValidAndroidHome(androidHome))
            throw new CalabashException(String.format("Invalid %s : %s", ENV_ANDROID_HOME, androidHome));
        this.androidHome = androidHome;
        envVariables.put(ENV_ANDROID_HOME, androidHome);
        if (javaHome != null && !javaHome.isEmpty())
            envVariables.put(ENV_JAVA_HOME, javaHome);
    }

    private static String getPlatformExecutable(String executable) {
        return isWindows() ? executable + ".exe" : executable;
    }

    private boolean isValidAndroidHome(String androidHome) {
        return getAdbFile(androidHome).exists();
    }

    public Map<String, String> getEnvVariables() {
        return new HashMap<String, String>(envVariables);
    }

    public String getKeytool() {
        String keytool = getPlatformExecutable(this.keytool);
        return quotify(keytool);
    }

    public String getJarsigner() {
        String jarsigner = getPlatformExecutable(this.jarsigner);
        return quotify(jarsigner);
    }

    public String getAdb() {
        return quotify(getAdbFile(androidHome).getAbsolutePath());
    }

    private File getAdbFile(String androidHome) {
        return new File(androidHome + File.separator + "platform-tools" + File.separator + getPlatformExecutable("adb"));
    }

    private File getEmulatorFile(String androidHome) {
        return new File(androidHome + File.separator + "tools" + File.separator + getPlatformExecutable("emulator"));
    }

    public String getEmulator() {
        return quotify(getEmulatorFile(androidHome).getAbsolutePath());
    }

    private String quotify(String executable) {
        return  "\"" + executable + "\"";
    }
}
