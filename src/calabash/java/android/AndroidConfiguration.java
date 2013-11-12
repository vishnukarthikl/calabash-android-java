package calabash.java.android;

public class AndroidConfiguration {
    private String androidHome;
    private String javaHome;

    public boolean isLoggingEnabled() {
        return false;
    }

    public String getLogsDirectory() {
        return null;
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
}
