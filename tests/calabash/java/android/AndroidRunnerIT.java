package calabash.java.android;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static calabash.java.android.TestUtils.createTempDir;
import static org.junit.Assert.*;

public class AndroidRunnerIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private File tempDir;
    private File tempAndroidPath;

    @Before
    public void setUp() throws IOException {
        createTempDirWithProj("MyAndroidApp.apk");
    }

    private void createTempDirWithProj(String androidApp) throws IOException {
        tempDir = createTempDir("TestAndroidApps");
        File androidAppPath = new File("tests/resources/" + androidApp);
        tempAndroidPath = new File(tempDir, androidApp);
        FileUtils.copyFile(androidAppPath, tempAndroidPath);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void shouldCreateTestServerApk() throws CalabashException, IOException {
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath());
        androidRunner.setup();
        File testServersDir = new File(tempDir, "test_servers");

        assertTrue(testServersDir.exists());
        File[] testServerApk = testServersDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".apk");
            }
        });

        assertEquals(1, testServerApk.length);
    }

    @Test
    public void shouldThrowExceptionIfSerialIsGivenWhenNotStarted() throws CalabashException {
        String serial = "emulator-x";
        expectedException.expect(CalabashException.class);
        expectedException.expectMessage(String.format("%s is not running. Cannot install app", serial));

        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial(serial);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);
        androidRunner.setup();
        androidRunner.start();

    }

    @Test
    public void shouldInstallApplicationIfSerialIsProvided() throws CalabashException {
        //note: emulator should be launched
        String packageName = "com.foo.android";
        uninstall(packageName);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial("emulator-5554");
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);
        androidRunner.setup();
        androidRunner.start();

        assertTrue(isAppInstalled(packageName, "emulator-5554"));
        assertTrue(isMainActivity(packageName));
    }

    private void uninstall(String packageName) {
        String[] command = {"adb", "uninstall", packageName};
        try {
            Utils.runCommand(command, "failed");
        } catch (CalabashException e) {
            fail();
        }

    }

    private boolean isMainActivity(String packageName) {
        String[] command = {"adb", "shell", "dumpsys", "activity"};
        try {
            String output = Utils.runCommand(command, "failed");
            int beginIndex = output.indexOf("Main stack");
            return output.substring(beginIndex, beginIndex + 100).contains(packageName);
        } catch (CalabashException e) {
            fail();
        }
        return false;
    }

    private boolean isAppInstalled(String appPackageName, final String serialNo) {
        String[] cmd = new String[]{"adb", "-s", serialNo, "shell", "pm", "path", appPackageName};
        try {
            String output = Utils.runCommand(cmd, "failed");
            return output.contains(appPackageName);
        } catch (CalabashException e) {
            fail("failed to see if app is installed");
        }
        return false;
    }
}
