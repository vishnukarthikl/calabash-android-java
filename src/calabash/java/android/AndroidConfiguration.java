package calabash.java.android;

import java.io.File;

public class AndroidConfiguration {
    private String androidHome;
    private String javaHome;
    private File logsDirectory;

    public boolean isLoggingEnabled() {
        return getLogsDirectory() != null;
    }

    public File getLogsDirectory() {
        return logsDirectory;
    }

    public String getAndroidHome() {
        return androidHome;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setAndroidHome(String androidHome) {
        this.androidHome = androidHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public void setLogsDirectory(File logsDirectory) throws CalabashException {
        if (logsDirectory == null) {
            this.logsDirectory = null;
            return;
        }

        if (!logsDirectory.isDirectory())
            throw new CalabashException(logsDirectory.getAbsolutePath()
                    + " is not a directory");

        if (!logsDirectory.canWrite())
            throw new CalabashException(logsDirectory.getAbsolutePath()
                    + " is not writable");

        this.logsDirectory = logsDirectory;
    }
}
