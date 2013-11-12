package calabash.java.android;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    public static final String ENV_ANDROID_HOME = "ANDROID_HOME";
    public static final String ENV_JAVA_HOME = "JAVA_HOME";

    private final String keytool;
    private final String jarsigner;
    private final Map<String, String> envVariables = new HashMap<String, String>();

    public Environment(String androidHome, String javaHome, String keytool, String jarsigner) {
        this.keytool = keytool;
        this.jarsigner = jarsigner;
        envVariables.put(ENV_ANDROID_HOME, androidHome);
        if (javaHome != null && !javaHome.isEmpty())
            envVariables.put(ENV_JAVA_HOME, javaHome);
    }

    public String getKeytool() {
        return keytool;
    }

    public String getJarsigner() {
        return jarsigner;
    }

    public Map<String, String> getEnvVariables() {
        return new HashMap<String, String>(envVariables);
    }
}
