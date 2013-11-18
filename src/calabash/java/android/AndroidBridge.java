package calabash.java.android;

import static calabash.java.android.Utils.runCommand;
import static java.lang.String.format;

public class AndroidBridge {

    public static final String BOOT_ANIM_STOPPED = "stopped";
    private final Environment environment;

    public AndroidBridge(Environment environment) {
        this.environment = environment;
    }

    public boolean isAppInstalled(String appPackageName, final String serialNo) throws CalabashException {
        String[] cmd = new String[]{environment.getAdb(), "-s", serialNo, "shell", "pm", "path", appPackageName};
        String output = runCommand(cmd, format("could not check if app %s is installed on %s", appPackageName, serialNo));
        return output.contains(appPackageName);
    }

    public String launchEmulator(AndroidConfiguration configuration) throws CalabashException {
        String deviceSerial = configuration.getSerial();
        if (deviceSerial != null) {
            checkDeviceIsRunning(deviceSerial);
            return deviceSerial;
        }
        String deviceName = configuration.getDeviceName();
        if (deviceName != null) {
            final String newSerial = launchEmulatorWithName(deviceName);
            if (newSerial == null) {
                CalabashLogger.error("Could not find launched emulator's serial from device list");
                throw new CalabashException("Emulator launch Failed");
            }

            ConditionalWaiter waitForBootAnim = new ConditionalWaiter(new ICondition(String.format("Device %s is not ready", newSerial)) {
                @Override
                public boolean test() throws CalabashException {
                    return isBootAnimationOver(newSerial);
                }
            });
            ConditionalWaiter waitForPackageManager = new ConditionalWaiter(new ICondition(String.format("Package manager is not available on %s", newSerial)) {
                @Override
                public boolean test() throws CalabashException {
                    return isPackageManagerAvailable(newSerial);
                }
            });
            waitForBootAnim.run(20, 5);
            waitForPackageManager.run(5, 5);
            return newSerial;
        }
        return deviceSerial;
    }

    private String launchEmulatorWithName(String deviceName) throws CalabashException {
        String[] launchCommand = getLaunchCommand(deviceName);
        final DeviceList deviceList = getDeviceList();
        Process process = Utils.runCommandInBackGround(launchCommand, format("failed to launch the emulator %s", deviceName));
        ConditionalWaiter conditionalWaiter = new ConditionalWaiter(new ICondition(String.format("Unable to launch emulator: %s", deviceName)) {
            public boolean test() throws CalabashException {
                DeviceList newDeviceList = getDeviceList();
                return deviceList.size() < newDeviceList.size();
            }
        });
        conditionalWaiter.run(5, 5);

        DeviceList newDeviceList = getDeviceList();
        return getNewSerial(deviceList, newDeviceList);
    }

    private String getNewSerial(DeviceList oldDeviceList, DeviceList newDeviceList) {
        for (Device device : newDeviceList.devices) {
            if (!oldDeviceList.contains(device))
                return device.getSerial();
        }
        return null;
    }

    private DeviceList getDeviceList() throws CalabashException {
        String listDeviceOutput = runCommand(getDeviceListCommand(), "could not list all devices");
        return new DeviceList(listDeviceOutput);
    }

    private boolean isBootAnimationOver(String serial) throws CalabashException {
        String[] bootAnimationCommand = getBootAnimationCommand(serial);
        String result = runCommand(bootAnimationCommand);
        return result.equals(BOOT_ANIM_STOPPED);
    }

    private void checkDeviceIsRunning(String serial) throws CalabashException {
        String[] deviceListCmd = getDeviceListCommand();
        String output = runCommand(deviceListCmd, "could not list all devices");
        if (!output.contains(serial))
            throw new CalabashException(format("%s is not running. Cannot install app", serial));
    }

    public void unlockKeyguard(String serial) throws CalabashException {
        String[] unlockCommand = {environment.getAdb(), "-s", serial, "shell", "input", "keyevent", "82"};
        runCommand(unlockCommand, "failed to unlock the keyguard");
    }

    private boolean isPackageManagerAvailable(String serial) throws CalabashException {
        String[] deviceReadyCommand = getPackageManagerAvailableCommand(serial);
        String output = runCommand(deviceReadyCommand);
        return output.contains("package");
    }

    private String[] getLaunchCommand(String deviceName) {
        return new String[]{environment.getEmulator(), "-avd", deviceName};
    }

    private String[] getDeviceListCommand() {
        return new String[]{environment.getAdb(), "devices"};
    }

    private String[] getBootAnimationCommand(String serial) {
        return new String[]{environment.getAdb(), "-s", serial, "shell", "getprop", "init.svc.bootanim"};
    }

    private String[] getPackageManagerAvailableCommand(String serial) {
        return new String[]{environment.getAdb(), "-s", serial, "shell", "pm", "path", "android"};
    }
}
