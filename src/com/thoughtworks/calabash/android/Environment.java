package com.thoughtworks.calabash.android;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    public static final String ENV_ANDROID_HOME = "ANDROID_HOME";
    public static final String ENV_JAVA_HOME = "JAVA_HOME";
    private final String keytool;
    private final String jarsigner;
    private final Map<String, String> envVariables = new HashMap<String, String>();
    private final String androidHome;
    private String jrubyHome;

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

    public static String getPlatformExecutable(String executable) {
        return Utils.isWindows() ? executable + ".exe" : executable;
    }

    public static String getPathSeparator() {
        return Utils.isWindows() ? ";" : ":";
    }

    public String getJrubyHome() {
        return jrubyHome;
    }

    public void setJrubyHome(String jrubyHome) {
        this.jrubyHome = jrubyHome;
    }

    private boolean isValidAndroidHome(String androidHome) {
        return getAdbFile(androidHome).exists();
    }

    public Map<String, String> getEnvVariables() {
        return new HashMap<String, String>(envVariables);
    }

    public String getKeytool() {
        String keytool = getPlatformExecutable(this.keytool);
        return quoteIfWindows(keytool);
    }

    public String getJarsigner() {
        String jarsigner = getPlatformExecutable(this.jarsigner);
        return quoteIfWindows(jarsigner);
    }

    public String getAdb() {
        return quoteIfWindows(getAdbFile(androidHome).getAbsolutePath());
    }

    private File getAdbFile(String androidHome) {
        return new File(androidHome + File.separator + "platform-tools" + File.separator + getPlatformExecutable("adb"));
    }

    public String getEmulator() {
        return quoteIfWindows(getEmulatorFile(androidHome).getAbsolutePath());
    }

    private File getEmulatorFile(String androidHome) {
        return new File(androidHome + File.separator + "tools" + File.separator + getPlatformExecutable("emulator"));
    }

    private String quoteIfWindows(String executable) {
        return Utils.isWindows() ? "\"" + executable + "\"" : executable;
    }
}
