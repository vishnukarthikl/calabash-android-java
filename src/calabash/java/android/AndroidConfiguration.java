package calabash.java.android;

import java.io.File;

public class AndroidConfiguration {
    private String androidHome;
    private String javaHome;
    private File logsDirectory;
    private String serial;
    private boolean shouldReinstallApp;
    private String deviceName;

    public boolean isLoggingEnabled() {
        return getLogsDirectory() != null;
    }

    public File getLogsDirectory() {
        return logsDirectory;
    }

    public void setLogsDirectory(File logsDirectory) throws CalabashException {
        if (logsDirectory == null) {
            this.logsDirectory = null;
            return;
        }

        if (!logsDirectory.isDirectory())
            throw new CalabashException(logsDirectory.getAbsolutePath() + " is not a directory");

        if (!logsDirectory.canWrite())
            throw new CalabashException(logsDirectory.getAbsolutePath() + " is not writable");

        this.logsDirectory = logsDirectory;
    }

    public String getAndroidHome() {
        return androidHome;
    }

    public void setAndroidHome(String androidHome) {
        this.androidHome = androidHome;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public boolean shouldReinstallApp() {
        return this.shouldReinstallApp;
    }

    public void setShouldReinstallApp(boolean reinstallApp) {
        this.shouldReinstallApp = reinstallApp;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
