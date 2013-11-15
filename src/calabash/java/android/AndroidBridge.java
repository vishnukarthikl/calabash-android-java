package calabash.java.android;

public class AndroidBridge {

    public void launchEmulator(AndroidConfiguration configuration) {
        String deviceSerial = configuration.getDeviceSerial();
        if (deviceSerial != null)
            launchEmulatorWithSerial(deviceSerial);
    }

    private void launchEmulatorWithSerial(String deviceSerial) {
        new TimedRunner(new Condition() {
            public boolean isSatisfied() {

            }
        })
    }
}
