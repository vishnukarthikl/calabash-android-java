package calabash.java.android;

public class AndroidApplication {
    private String installedOn;

    public AndroidApplication(String serial) {
        this.installedOn = serial;
    }


    public String getInstalledOn() {
        return installedOn;
    }

    public void setInstalledOn(String installedOn) {
        this.installedOn = installedOn;
    }
}
