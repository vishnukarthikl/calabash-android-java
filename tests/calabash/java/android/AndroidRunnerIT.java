package calabash.java.android;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;

import static calabash.java.android.TestUtils.createTempDir;
import static calabash.java.android.Utils.runCommand;
import static org.junit.Assert.*;

//Just run all the test. Can't help with emulator state dependency.
public class AndroidRunnerIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private File tempDir;
    private File tempAndroidPath;
    private String packageName;

    @Before
    public void setUp() throws IOException {
        packageName = "com.example.AndroidTestApplication";
        createTempDirWithProj("AndroidTestApplication.apk");
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
        final AndroidApplication application = installAppOnEmulator("emulator-5554");
        goToActivity(application, "Simple Elements");
        application.waitFor(new ICondition() {
            @Override
            public boolean test() throws CalabashException {
                return application.getCurrentActivity().contains("SimpleElementsActivity");
            }
        }, 5000);

        UIElements elements = application.query("textview marked:'Hello world!'");

        assertEquals(1, elements.size());
        assertEquals("Hello world!", elements.first().getText());
    }

    @Test
    @Ignore("Need to speed up inspect..taking too long")
    public void shouldInspectApplicationElements() throws CalabashException {
        final AndroidApplication application = installAppOnEmulator("emulator-5554");
        goToActivity(application, "Nested Views");
        application.waitFor(new ICondition() {
            @Override
            public boolean test() throws CalabashException {
                return application.getCurrentActivity().contains("NestedViewsActivity");
            }
        }, 5000);
        String expectedElementCollection = "";
        final StringBuilder actualElementCollection = new StringBuilder();

        application.inspect(new InspectCallback() {
            public void onEachElement(UIElement element, int nestingLevel) {
                actualElementCollection.append(String.format("Element : %s , Nesting : %d\n", element.getElementClass(), nestingLevel));
            }
        });

        assertEquals(expectedElementCollection, actualElementCollection.toString());
    }

    @Test
    public void shouldTouchElements() throws CalabashException {
        final AndroidApplication application = installAppOnEmulator("emulator-5554");

        goToActivity(application, "Simple Elements");
        application.waitFor(new ICondition() {
            @Override
            public boolean test() throws CalabashException {
                return application.getCurrentActivity().contains("SimpleElementsActivity");
            }
        }, 5000);
        UIElement button = application.query("button").first();
        UIElement radioButton = application.query("radioButton").first();
        UIElement imageButton = application.query("imageButton").first();

        button.touch();
        UIElement textView = application.query("textView index:1").first();
        assertEquals("normal button was clicked", textView.getText());

        radioButton.touch();
        textView = application.query("textView index:1").first();
        assertEquals("radio button was clicked", textView.getText());

        imageButton.touch();
        textView = application.query("textView index:1").first();
        assertEquals("image button was clicked", textView.getText());
    }

    @Test
    public void shouldSetText() throws CalabashException {
        final AndroidApplication application = installAppOnEmulator("emulator-5554");
        goToActivity(application, "Simple Elements");
        application.waitFor(new ICondition() {
            @Override
            public boolean test() throws CalabashException {
                return application.getCurrentActivity().contains("SimpleElementsActivity");
            }
        }, 5000);
        UIElement editText = application.query("editText").first();

        editText.setText("foo bar");

        UIElement textView = application.query("textView index:1").first();
        assertEquals("foo bar was entered", textView.getText());
    }

    @Test
    public void shouldTakeScreenshot() throws CalabashException {
        AndroidApplication application = installAppOnEmulator("emulator-5554");
        File screenshotsDir = new File(tempDir, "screenshots");
        screenshotsDir.mkdirs();

        application.takeScreenshot(screenshotsDir, "first");
        File screenshot = new File(screenshotsDir, "first_0.png");

        assertTrue(screenshot.exists());
    }

    @Test
    public void shouldGetSharedPreferences() throws CalabashException {
        AndroidApplication application = installAppOnEmulator("emulator-5554");
        Map<String, String> preferences = application.getSharedPreferences("my_preferences");

        assertEquals("true", preferences.get("a boolean"));
        assertEquals("my string", preferences.get("a string"));
        assertEquals("1.5", preferences.get("a float"));
        assertEquals("123", preferences.get("an int"));

    }

    private void goToActivity(AndroidApplication application, final String activityName) throws CalabashException {
        application.query("* marked:'" + activityName + "'").touch();
    }

    private AndroidApplication installAppOnEmulator(String serial) throws CalabashException {
        uninstall(packageName);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial(serial);
        configuration.setLogsDirectory(new File("logs"));
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);
        androidRunner.setup();
        AndroidApplication application = androidRunner.start();
        assertTrue(isAppInstalled(packageName, serial));
        assertTrue(isMainActivity(packageName, serial));
        return application;
    }

    private void uninstall(String packageName) {
        String[] command = {"adb", "uninstall", packageName};
        try {
            runCommand(command);
        } catch (CalabashException e) {
            fail();
        }

    }

    private boolean isMainActivity(String packageName, String serial) {
        String[] command = {"adb", "-s", serial, "shell", "dumpsys", "window", "windows"};
        try {
            String output = runCommand(command, "failed");
            int beginIndex = output.indexOf("mCurrentFocus"); {
                if (beginIndex == -1)
                    beginIndex =  output.indexOf("mFocusedApp");
            }
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
