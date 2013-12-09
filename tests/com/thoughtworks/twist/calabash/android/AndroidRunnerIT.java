package com.thoughtworks.twist.calabash.android;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//Just run all the test. Can't help with emulator state dependency.
public class AndroidRunnerIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private File tempDir;
    private File tempAndroidApkPath;
    private String packageName;

    @Before
    public void setUp() throws IOException {
        packageName = "com.example.AndroidTestApplication";
        tempDir = TestUtils.createTempDir("TestAndroidApps");
        tempAndroidApkPath = createTempDirWithProj("AndroidTestApplication.apk", tempDir);
    }

    private File createTempDirWithProj(String androidApp, File dir) throws IOException {
        File androidAppPath = new File("tests/resources/" + androidApp);
        File tempAndroidPath = new File(dir, androidApp);
        FileUtils.copyFile(androidAppPath, tempAndroidPath);
        return tempAndroidPath;
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void shouldCreateTestServerApk() throws CalabashException, IOException {
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidApkPath.getAbsolutePath());
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
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidApkPath.getAbsolutePath(), configuration);
        androidRunner.setup();
        androidRunner.start();
    }

    @Test
    public void shouldInstallAppOnDeviceWithName() throws CalabashException {
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setDeviceName("device");
        configuration.setShouldReinstallApp(true);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidApkPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        AndroidApplication application = androidRunner.start();

        assertTrue(TestUtils.isAppInstalled(packageName, application.getInstalledOn()));
        assertTrue(TestUtils.isMainActivity(application, "MyActivity"));
    }

    @Test
    public void shouldInstallApplicationIfSerialIsProvided() throws CalabashException {
        //note: emulator should be launched with serial 'emulator-5554
        String serial = "emulator-5554";
        TestUtils.uninstall(packageName, serial);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial(serial);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidApkPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        AndroidApplication application = androidRunner.start();

        assertTrue(TestUtils.isAppInstalled(packageName, serial));
        assertTrue(TestUtils.isMainActivity(application, "MyActivity"));
    }

    @Test
    public void shouldInstallApplicationAlreadyRunningDevice() throws CalabashException {
        //note: emulator with name 'device' should be launched with serial 'emulator-5554'
        String device = "device";
        String serial = "emulator-5554";
        TestUtils.uninstall(packageName, serial);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setDeviceName(device);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidApkPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        AndroidApplication application = androidRunner.start();

        assertTrue(TestUtils.isAppInstalled(packageName, serial));
        assertTrue(TestUtils.isMainActivity(application, TestUtils.activityMap.get(TestUtils.ACTIVITY_MAIN)));
    }

    @Test
    public void shouldTestGoBack() throws CalabashException, OperationTimedoutException {
        final AndroidApplication application = TestUtils.installAppOnEmulator("emulator-5554", packageName, tempAndroidApkPath);

        TestUtils.goToActivity(application, "Nested Views");
        application.goBack();

        application.waitForActivity("MyActivity", 2);
        assertEquals("MyActivity", application.getCurrentActivity());
    }

    @Test
    public void shouldTakeScreenshotOnFailure() throws CalabashException {
        final StringBuffer screenshotPath = new StringBuffer();
        AndroidConfiguration androidConfiguration = new AndroidConfiguration();
        androidConfiguration.setSerial("emulator-5554");
        androidConfiguration.setScreenshotListener(new ScreenshotListener() {
            public void screenshotTaken(String path, String imageType, String fileName) {
                screenshotPath.append(path);
            }
        });
        final AndroidApplication application;
        try {
            application = TestUtils.installAppOnEmulator("emulator-5554", packageName, tempAndroidApkPath, androidConfiguration);
            application.waitFor(new ICondition() {
                @Override
                public boolean test() throws CalabashException {
                    return false;
                }
            }, 1);
        } catch (OperationTimedoutException e) {
        }

        assertTrue(new File(tempDir, screenshotPath.toString()).exists());
    }
}
