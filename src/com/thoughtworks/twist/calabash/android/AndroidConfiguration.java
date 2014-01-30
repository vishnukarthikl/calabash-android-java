package com.thoughtworks.twist.calabash.android;

import java.io.File;

public class AndroidConfiguration {
    private String androidHome;
    private String javaHome;
    private File logsDirectory;
    private String serial;
    private boolean shouldReinstallApp = false;
    private String deviceName;
    private ScreenshotListener screenshotListener;
    private long pauseTimeInMs = -1;
    private int timeToWaitInSecForEmulatorLaunch = 180;

    /**
     * Gets the boolean value indicating whether logging is enabled
     *
     * @return true if logging is enabled, false otherwise
     */
    public boolean isLoggingEnabled() {
        return getLogsDirectory() != null;
    }

    /**
     * Gets the directory where log files are created
     *
     * @return
     *  The logs directory if set, null otherwise
     */
    public File getLogsDirectory() {
        return logsDirectory;
    }

    /**
     * Sets the directory where log files are created. Setting to null will disable logging.
     * Defaults to null
     *
     * @param logsDirectory
     *      The logs directory
     * @throws CalabashException
     *      If directory does not exist or permissions to write in the directory are not available.
     */
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

    /**
     * Gets the path to the Android SDK directory
     *
     * @return
     *      Path to the android SDK directory if set, null otherwise.
     */
    public String getAndroidHome() {
        return androidHome;
    }

    /**
     * Sets the absolute path to the android sdk directory.
     *
     * @param androidHome
     *     Absolute path to the android sdk directory.
     */
    public void setAndroidHome(String androidHome) {
        this.androidHome = androidHome;
    }

    /**
     * Gets the path to the JRE installation directory
     *
     * @return
     *     path to JRE installation directory if set, null otherwise.
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * Sets the path to JAVA_HOME. Points to the JRE installation directory
     *
     * @param javaHome
     *  Absolute path to the JRE installation directory
     */
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * Gets the serial number of device on which tests execute.
     *
     * @return
     *     serial number if set otherwise null.
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Sets the serial number of the device to execute tests on.
     * eg: emulator-5554
     *
     * Do not set device name if serial number is set.
     *
     * @param serial
     *              Serial number of the device to execute tests on.
     */
    public void setSerial(String serial) {
        this.serial = serial;
    }


    /**
     * Determines if app should be installed on each execution.
     *
     * @return
     *
     * true if should reinstall app on each execution, false otherwise
     */
    public boolean shouldReinstallApp() {
        return this.shouldReinstallApp;
    }

    /**
     * Set true to reinstall the app on the device on every start.
     * Default value false.
     *
     * @param reinstallApp
     */
    public void setShouldReinstallApp(boolean reinstallApp) {
        this.shouldReinstallApp = reinstallApp;
    }

    /**
     * Gets the name of the emulator device set.
     *
     * @return
     *     name of the emulator device if set or null
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Set the name of the emulator device created on the android avd manager to run tests on.
     *  Leave blank if serial is set
     *
     * @param deviceName
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Gets the current screenshot listener
     *
     * @return
     *  ScreenshotListener instance if set or null.
     */
    public ScreenshotListener getScreenshotListener() {
        return screenshotListener;
    }

    /**
     * Sets a screenshot listener which is invoked every time Calabash takes a screenshot.
     *
     * @param screenshotListener
     *                  ScreenshotListener instance
     *
     */
    public void setScreenshotListener(ScreenshotListener screenshotListener) {
        this.screenshotListener = screenshotListener;
    }

    /**
     * Sets the pause time in milliseconds.
     *
     * Calabash will pause for the specified seconds after performing every
     * action.
     *
     * @param pauseTimeInMs
     *            Milliseconds to wait after each actions
     */
    public void setPauseTime(long pauseTimeInMs) {
        this.pauseTimeInMs = pauseTimeInMs;
    }


    /**
     * Gets the pause time in milliseconds
     *
     * @return Pause time in milliseconds
     */
    public long getPauseTimeInMs() {
        return pauseTimeInMs;
    }

    /**
     * specify the time that you want to wait for the emulator to get launched, defaults to 180 seconds.
     * Usually it takes a while to load the emualator, so it is better to enable snapshot through avd
     *
     * @param timeToWaitInSec seconds to wait
     */
    public void setEmulatorLaunchWaitTimeout(int timeToWaitInSec) {
        this.timeToWaitInSecForEmulatorLaunch = timeToWaitInSec;
    }

    /**
     *
     * @return timeToWaitInSecForEmulatorLaunch
     */
    public int getTimeToWaitInSecForEmulatorLaunch() {
        return timeToWaitInSecForEmulatorLaunch;
    }
}
