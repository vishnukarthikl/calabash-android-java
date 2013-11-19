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
import static calabash.java.android.Utils.runCommand;
import static org.junit.Assert.*;

//Just run all the test. Can't help with emulator state dependency.
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
    public void shouldInstallAppOnDeviceWithName() throws CalabashException {
        String packageName = "com.foo.android";
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setDeviceName("device");
        configuration.setShouldReinstallApp(true);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        AndroidApplication application = androidRunner.start();

        assertTrue(isAppInstalled(packageName, application.getInstalledOn()));
        assertTrue(isMainActivity(packageName, application.getInstalledOn()));
    }

    @Test
    public void shouldInstallApplicationIfSerialIsProvided() throws CalabashException {
        //note: emulator should be launched with serial 'emulator-5554
        String packageName = "com.foo.android";
        String serial = "emulator-5554";
        uninstall(packageName);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial(serial);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        androidRunner.start();

        assertTrue(isAppInstalled(packageName, serial));
        assertTrue(isMainActivity(packageName, serial));
    }

    @Test
    public void shouldInstallApplicationAlreadyRunningDevice() throws CalabashException {
        //note: emulator with name 'device' should be launched with serial 'emulator-5554'

        String packageName = "com.foo.android";
        String device = "device";
        String serial = "emulator-5554";
        uninstall(packageName);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setDeviceName(device);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        androidRunner.start();

        assertTrue(isAppInstalled(packageName, serial));
        assertTrue(isMainActivity(packageName, serial));
    }

    @Test
    public void shouldQueryForElements() throws CalabashException {
        String packageName = "com.foo.android";
        String serial = "emulator-5554";
        uninstall(packageName);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial(serial);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        AndroidApplication application = androidRunner.start();
        assertTrue(isAppInstalled(packageName, serial));
        assertTrue(isMainActivity(packageName, serial));

        UIElements elements = application.query("textview marked:'Hello world!'");
        assertEquals(1, elements.size());
        assertEquals("Hello world!", elements.first().getText());
    }

    @Test
    public void shouldInspectApplicationElements() throws CalabashException {
        String packageName = "com.foo.android";
        String serial = "emulator-5554";
        uninstall(packageName);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial(serial);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        AndroidApplication application = androidRunner.start();
        assertTrue(isAppInstalled(packageName, serial));
        assertTrue(isMainActivity(packageName, serial));

        String expectedElementCollection = "Element : com.android.internal.policy.impl.PhoneWindow$DecorView , Nesting : 0\n" +
                          "Element : android.widget.LinearLayout , Nesting : 1\n" +
                          "Element : com.android.internal.widget.ActionBarContainer , Nesting : 2\n" +
                          "Element : com.android.internal.widget.ActionBarView , Nesting : 3\n" +
                          "Element : android.widget.LinearLayout , Nesting : 4\n" +
                          "Element : android.widget.LinearLayout , Nesting : 5\n" +
                          "Element : android.widget.TextView , Nesting : 6\n" +
                          "Element : com.android.internal.widget.ActionBarView$HomeView , Nesting : 4\n" +
                          "Element : android.widget.ImageView , Nesting : 5\n" +
                          "Element : android.widget.FrameLayout , Nesting : 2\n" +
                          "Element : android.widget.RelativeLayout , Nesting : 3\n" +
                          "Element : android.widget.TextView , Nesting : 4\n";
        final StringBuilder actualElementCollection = new StringBuilder();
        application.inspect(new InspectCallback() {
            public void onEachElement(UIElement element, int nestingLevel) {
                actualElementCollection.append(String.format("Element : %s , Nesting : %d\n", element.getElementClass(), nestingLevel));
            }
        });
        assertEquals(expectedElementCollection, actualElementCollection.toString());
    }

    private void uninstall(String packageName) {
        String[] command = {"adb", "uninstall", packageName};
        try {
            runCommand(command, "failed");
        } catch (CalabashException e) {
            fail();
        }

    }

    private boolean isMainActivity(String packageName, String serial) {
        String[] command = {"adb", "-s", serial, "shell", "dumpsys", "activity"};
        try {
            String output = runCommand(command, "failed");
            int beginIndex = output.indexOf("Main stack");
            if (beginIndex == -1) fail("Main activity not found");
            return output.substring(beginIndex, beginIndex + 100).contains(packageName);
        } catch (CalabashException e) {
            fail("Main activity not found");
        }
        return false;
    }

    private boolean isAppInstalled(String appPackageName, final String serialNo) {
        String[] cmd = new String[]{"adb", "-s", serialNo, "shell", "pm", "path", appPackageName};
        try {
            String output = runCommand(cmd, "failed");
            return output.contains(appPackageName);
        } catch (CalabashException e) {
            fail("failed to see if app is installed");
        }
        return false;
    }
}
