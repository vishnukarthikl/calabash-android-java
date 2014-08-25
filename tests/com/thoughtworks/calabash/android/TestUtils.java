package com.thoughtworks.calabash.android;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.thoughtworks.calabash.android.Utils.runCommand;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestUtils {

    public static final String ACTIVITY_MAIN = "Main Activity";
    public static final String ACTIVITY_CURRENT_LOCATION = "Current Location";
    public static final String ACTIVITY_SCROLL_LIST = "Scroll List";
    public static final String ACTIVITY_NESTED_VIEWS = "Nested Views";
    public static final String ACTIVITY_SWIPE_PAGE = "Swipe Page";
    public static final String ACTIVITY_SIMPLE_ELEMENTS = "Simple Elements";
    public static final String ACTIVITY_DATE_TIME_ELEMENTS = "DateTime Elements";
    public static final String ACTIVITY_WEB_VIEW = "Web view";
    public static HashMap<String, String> activityMap = new HashMap<String, String>() {{
        put(ACTIVITY_SIMPLE_ELEMENTS, "SimpleElementsActivity");
        put(ACTIVITY_SWIPE_PAGE, "SwipePageActivity");
        put(ACTIVITY_SCROLL_LIST, "ScrollListActivity");
        put(ACTIVITY_NESTED_VIEWS, "NestedViewsActivity");
        put(ACTIVITY_CURRENT_LOCATION, "CurrentLocationActivity");
        put(ACTIVITY_MAIN, "MyActivity");
        put(ACTIVITY_DATE_TIME_ELEMENTS, "DateTimeElementsActivity");
        put(ACTIVITY_WEB_VIEW, "WebViewActivity");
    }};

    public static File createTempDir(String directoryName) throws IOException {
        File tempFile = File.createTempFile("foo", "bar");
        tempFile.delete();
        File tempDir = new File(tempFile.getParentFile(), directoryName);
        tempDir.mkdir();
        return tempDir;
    }

    public static File createTempDirWithProj(String androidApp, File dir) throws IOException {
        File androidAppPath = new File("tests/resources/" + androidApp);
        File tempAndroidPath = new File(dir, androidApp);
        FileUtils.copyFile(androidAppPath, tempAndroidPath);
        return tempAndroidPath;
    }

    public static void goToActivity(AndroidApplication application, final String activityName) throws CalabashException, OperationTimedoutException {
        application.query("* marked:'" + activityName + "'").touch();
        application.waitForActivity(activityMap.get(activityName), 6);
    }

    public static AndroidApplication installAppOnEmulator(String serial, String packageName, File androidApkPath) throws CalabashException {
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setLogsDirectory(new File("logs"));
        configuration.setSerial(serial);
        return installAppOnEmulator(serial, packageName, androidApkPath, configuration);
    }

    public static AndroidApplication installAppOnEmulator(String serial, String packageName, File androidApkPath, AndroidConfiguration configuration) throws CalabashException {
        uninstall(packageName, serial);
        configuration.setPauseTime(4000);
        AndroidRunner androidRunner = new AndroidRunner(androidApkPath.getAbsolutePath(), configuration);
        androidRunner.setup();
        AndroidApplication application = androidRunner.start();
        assertTrue(isAppInstalled(packageName, serial));
        assertTrue(isMainActivity(application, activityMap.get(ACTIVITY_MAIN)));
        return application;
    }

    public static void uninstall(String packageName, String serial) throws CalabashException {

        String[] command = {TestUtils.getAdbPath(), "-s", serial, "uninstall", packageName};
        try {
            runCommand(command);
        } catch (CalabashException e) {
            fail(e.getMessage());
        }

    }

    private static String getAdbPath() throws CalabashException {
        return EnvironmentInitializer.initialize(new AndroidConfiguration()).getAdb();
    }

    public static boolean isAppInstalled(String appPackageName, final String serialNo) throws CalabashException {
        String[] cmd = new String[]{TestUtils.getAdbPath(), "-s", serialNo, "shell", "pm", "path", appPackageName};
        try {
            String output = runCommand(cmd, "failed");
            return output.contains(appPackageName);
        } catch (CalabashException e) {
            fail("failed to see if app is installed");
        }
        return false;
    }

    public static boolean isMainActivity(AndroidApplication application, String mainActivity) {
        try {
            return application.getCurrentActivity().toLowerCase().contains(mainActivity.toLowerCase());
        } catch (CalabashException e) {
            fail(mainActivity + " wasn't the main activity");
        }
        return false;
    }

    public static String readFileFromResources(final String fileName) throws IOException {
        final File file = new File("tests/resources/" + fileName);
        return FileUtils.readFileToString(file, "UTF-8");
    }
}
