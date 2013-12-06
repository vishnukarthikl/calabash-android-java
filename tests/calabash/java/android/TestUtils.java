package calabash.java.android;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static calabash.java.android.Utils.runCommand;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestUtils {

    public static final String ACTIVITY_CURRENT_LOCATION = "Current Location";
    public static final String ACTIVITY_SCROLL_LIST = "Scroll List";
    public static final String ACTIVITY_NESTED_VIEWS = "Nested Views";
    public static final String ACTIVITY_SWIPE_PAGE = "Swipe Page";
    public static final String ACTIVITY_SIMPLE_ELEMENTS = "Simple Elements";

    private static HashMap<String, String> activityMap = new HashMap<String, String>() {{
        put(ACTIVITY_SIMPLE_ELEMENTS, "SimpleElementsActivity");
        put(ACTIVITY_SWIPE_PAGE, "SwipePageActivity");
        put(ACTIVITY_SCROLL_LIST, "ScrollListActivity");
        put(ACTIVITY_NESTED_VIEWS, "NestedViewsActivity");
        put(ACTIVITY_CURRENT_LOCATION, "CurrentLocationActivity");
    }};

    public static File createTempDir(String directoryName) throws IOException {
        File tempFile = File.createTempFile("foo", "bar");
        tempFile.delete();
        File tempDir = new File(tempFile.getParentFile(), directoryName);
        tempDir.mkdir();
        return tempDir;
    }

    public static void clearAppDir() throws IOException {
        File tempDir = createTempDir("TestIOSApps");
        FileUtils.deleteDirectory(tempDir);
    }

    public static File createTempDirWithProj(String androidApp, File dir) throws IOException {
        File androidAppPath = new File("tests/resources/" + androidApp);
        File tempAndroidPath = new File(dir, androidApp);
        FileUtils.copyFile(androidAppPath, tempAndroidPath);
        return tempAndroidPath;
    }

    public static void goToActivity(AndroidApplication application, final String activityName) throws CalabashException {
        application.query("* marked:'" + activityName + "'").touch();
        application.waitForActivity(activityMap.get(activityName), 5000);
    }

    public static AndroidApplication installAppOnEmulator(String serial, String packageName, File androidApkPath) throws CalabashException {
        uninstall(packageName, serial);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial(serial);
        configuration.setLogsDirectory(new File("logs"));
        AndroidRunner androidRunner = new AndroidRunner(androidApkPath.getAbsolutePath(), configuration);
        androidRunner.setup();
        AndroidApplication application = androidRunner.start();
        assertTrue(isAppInstalled(packageName, serial));
        assertTrue(isMainActivity(application, "MyActivity"));
        return application;
    }

    public static void uninstall(String packageName, String serial) {
        String[] command = {"adb", "-s", serial, "uninstall", packageName};
        try {
            runCommand(command);
        } catch (CalabashException e) {
            fail(e.getMessage());
        }

    }

    public static boolean isAppInstalled(String appPackageName, final String serialNo) {
        String[] cmd = new String[]{"adb", "-s", serialNo, "shell", "pm", "path", appPackageName};
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
}
