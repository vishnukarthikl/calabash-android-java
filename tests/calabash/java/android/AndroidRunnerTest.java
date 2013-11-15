package calabash.java.android;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;

public class AndroidRunnerTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowCalabashExceptionIfApkNotFound() throws Exception {
        expectedException.expect(CalabashException.class);
        expectedException.expectMessage("invalid path to apk file");
        File tempFile = File.createTempFile("foo", "bar");
        new AndroidRunner(tempFile.getPath());
    }

    @Test
    public void shouldFailIfAndroidHomeIsNotSet() throws IOException, CalabashException {
        if (System.getenv("ANDROID_HOME") == null) {
            expectedException.expect(CalabashException.class);
            expectedException.expectMessage("ANDROID_HOME is not set");

            File apk = new File("tests/resources/MyAndroidApp.apk");
            AndroidRunner androidRunner = new AndroidRunner(apk.getAbsolutePath());
            androidRunner.setup();
        }
    }

    @Test
    public void shouldFailForAndroidHome() throws IOException, CalabashException {
        String androidHome = "/user/android/sdk";

        expectedException.expect(CalabashException.class);
        expectedException.expectMessage("Invalid ANDROID_HOME : " + androidHome);

        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setAndroidHome(androidHome);
        File apk = new File("tests/resources/MyAndroidApp.apk");
        AndroidRunner androidRunner = new AndroidRunner(apk.getAbsolutePath(), configuration);
        androidRunner.setup();
    }
}
