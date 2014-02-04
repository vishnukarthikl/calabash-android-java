package com.thoughtworks.calabash.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static com.thoughtworks.calabash.android.CalabashLogger.info;
import static java.lang.String.format;

public class AndroidBridge {

    public static final String BOOT_ANIM_STOPPED = "stopped";
    public static final String EMULATOR_PREFIX = "emulator-";
    private final Environment environment;
    private DeviceList deviceList;
    private DeviceList newDeviceList;
    public static final int POLL_RATE_IN_SECONDS = 5;

    public AndroidBridge(Environment environment) {
        this.environment = environment;
    }

    public boolean isAppInstalled(String appPackageName, final String serialNo) throws CalabashException {
        String[] cmd = new String[]{environment.getAdb(), "-s", serialNo, "shell", "pm", "path", appPackageName};
        String output = Utils.runCommand(cmd, format("could not check if app %s is installed on %s", appPackageName, serialNo));
        return output.contains(appPackageName);
    }

    public String launchEmulator(AndroidConfiguration configuration) throws CalabashException {
        deviceList = getDeviceList();
        String deviceSerial = configuration.getSerial();
        if (deviceSerial != null) {
            checkDeviceIsRunning(deviceList, deviceSerial);
            return deviceSerial;
        }
        String deviceName = configuration.getDeviceName();
        if (deviceName != null) {
            final String newSerial = launchEmulatorWithName(deviceName);
            if (newSerial == null) {
                CalabashLogger.error("Could not find launched emulator's serial from device list");
                throw new CalabashException("Emulator launch Failed");
            }

            ConditionalWaiter waitForBootAnim = new ConditionalWaiter(new ICondition(format("Wait for Device %s to be ready", newSerial)) {
                @Override
                public boolean test() throws CalabashException {
                    return isBootAnimationOver(newSerial);
                }
            });
            ConditionalWaiter waitForPackageManager = new ConditionalWaiter(new ICondition(format("Wait for Package manager to be available on %s", newSerial)) {
                @Override
                public boolean test() throws CalabashException {
                    return isPackageManagerAvailable(newSerial);
                }
            });
            waitForBootAnim.run(configuration.getTimeToWaitInSecForEmulatorLaunch() / POLL_RATE_IN_SECONDS, POLL_RATE_IN_SECONDS);
            waitForPackageManager.run(5, POLL_RATE_IN_SECONDS);
            unlockKeyguard(newSerial);
            return newSerial;
        }
        if (deviceList.size() == 1) {
            CalabashLogger.info("Only one emualtor/device is connected");
            return deviceList.get(0).getSerial();
        }
        throw new CalabashException("Could not get the device serial, set the serial or devicename in the AndroidConfiguration");
    }

    private String launchEmulatorWithName(String deviceName) throws CalabashException {
        String[] launchCommand = getLaunchCommand(deviceName);
        String launchedDeviceSerial = getSerialIfDeviceAlreadyLaunched(deviceList, deviceName);
        if (launchedDeviceSerial != null) {
            return launchedDeviceSerial;
        }
        Process process = Utils.runCommandInBackGround(launchCommand, format("failed to launch the emulator %s", deviceName));
        ConditionalWaiter waitForNewEmulatorLaunch = new ConditionalWaiter(new ICondition(format("waiting for emulator with name %s to launch", deviceName)) {
            public boolean test() throws CalabashException {
                newDeviceList = getDeviceList();
                return deviceList.size() < newDeviceList.size();
            }
        });
        waitForNewEmulatorLaunch.run(5, 5);

        return getNewSerial(deviceList, newDeviceList);
    }

    private String getSerialIfDeviceAlreadyLaunched(DeviceList deviceList, String deviceName) throws CalabashException {
        CalabashLogger.info("Checking if %s is already launched", deviceName);
        for (Device device : deviceList.devices) {
            String serial = device.getSerial();
            try {
                if (isDeviceRunningWithSerial(deviceName, serial)) {
                    CalabashLogger.info("%s is running with serial %s", deviceName, serial);
                    return serial;
                }
            } catch (IOException e) {
                CalabashLogger.error(e);
                throw new CalabashException("could not get the device name from serial " + deviceName, e);
            }
        }
        return null;
    }

    private boolean isDeviceRunningWithSerial(String deviceName, String serial) throws IOException, CalabashException {
        int index = serial.indexOf(EMULATOR_PREFIX);
        if (index != -1) {
            String port = serial.substring(EMULATOR_PREFIX.length());
            Socket emulatorSocket = new Socket("localhost", Integer.parseInt(port));
            BufferedReader in = new BufferedReader(new InputStreamReader(emulatorSocket.getInputStream()));
            PrintWriter out = new PrintWriter(emulatorSocket.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try {
                in.readLine();
                in.readLine();
                out.println("avd name");
                String runningDeviceName = in.readLine();
                if (deviceName.equals(runningDeviceName.trim())) {
                    return true;
                }
            } catch (Exception e) {
                throw new CalabashException(String.format("could not get the device name from serial %s", serial), e);
            } finally {
                emulatorSocket.close();
                in.close();
                out.close();
                br.close();
            }
        }
        return false;
    }

    private String getNewSerial(DeviceList oldDeviceList, DeviceList newDeviceList) {
        for (Device device : newDeviceList.devices) {
            if (!oldDeviceList.contains(device))
                return device.getSerial();
        }
        return null;
    }

    private DeviceList getDeviceList() throws CalabashException {
        String listDeviceOutput = Utils.runCommand(getDeviceListCommand(), "could not list all devices");
        return new DeviceList(listDeviceOutput);
    }

    private boolean isBootAnimationOver(String serial) throws CalabashException {
        String[] bootAnimationCommand = getBootAnimationCommand(serial);
        String result = Utils.runCommand(bootAnimationCommand);
        return result.equals(BOOT_ANIM_STOPPED);
    }

    private void checkDeviceIsRunning(DeviceList deviceList, String serial) throws CalabashException {
        for (Device device : deviceList.devices) {
            if (device.getSerial().equals(serial)) {
                if (!device.getState().equals("device")) {
                    throw new CalabashException(format("%s's state: %s. Cannot install app", serial, device.getState()));
                }
                return;
            }
        }
        throw new CalabashException(format("%s not found in the device list, installation failed", serial));
    }

    public void unlockKeyguard(String serial) throws CalabashException {
        String[] unlockCommand = {environment.getAdb(), "-s", serial, "shell", "input", "keyevent", "82"};
        Utils.runCommand(unlockCommand, "failed to unlock the keyguard");
    }

    private boolean isPackageManagerAvailable(String serial) throws CalabashException {
        String[] deviceReadyCommand = getPackageManagerAvailableCommand(serial);
        String output = Utils.runCommand(deviceReadyCommand);
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
