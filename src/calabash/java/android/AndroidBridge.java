package calabash.java.android;

public class AndroidBridge {

    private final Environment environment;

    public AndroidBridge(Environment environment) {
        this.environment = environment;
    }

    public boolean isAppInstalled(String appPackageName, final String serialNo) throws CalabashException {
        String[] cmd = new String[]{environment.getAdb(), "-s", serialNo, "shell", "pm", "path", appPackageName};
        String output = Utils.runCommand(cmd, String.format("could not check if app %s is installed on %s", appPackageName, serialNo));
        return output.contains(appPackageName);
    }

    public String launchEmulator(AndroidConfiguration configuration) throws CalabashException {
        String deviceSerial = configuration.getSerial();
        if (deviceSerial != null) {
            checkDeviceIsRunning(deviceSerial);
            return deviceSerial;
        }

        return deviceSerial;
    }

    private void checkDeviceIsRunning(String serial) throws CalabashException {
        String adb = environment.getAdb();
        String[] deviceListCmd = {adb, "devices"};
        String output = Utils.runCommand(deviceListCmd, "could not list all devices");
        if (!output.contains(serial))
            throw new CalabashException(String.format("%s is not running. Cannot install app", serial));
    }
}
