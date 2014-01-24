package com.thoughtworks.twist.calabash.android.unit;

import com.thoughtworks.twist.calabash.android.AndroidConfiguration;
import com.thoughtworks.twist.calabash.android.AndroidRunner;
import com.thoughtworks.twist.calabash.android.CalabashException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;

public class AndroidRunnerTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AndroidConfiguration configuration = new AndroidConfiguration();

    @Test
    public void shouldThrowCalabashExceptionIfApkNotFound() throws Exception {
        expectedException.expect(CalabashException.class);
        expectedException.expectMessage("invalid path to apk file");
        File tempFile = File.createTempFile("foo", "bar");
        new AndroidRunner(tempFile.getAbsolutePath(), configuration);
    }

    @Test
    public void shouldFailIfAndroidHomeIsNotSet() throws IOException, CalabashException {
        if (System.getenv("ANDROID_HOME") == null) {
            expectedException.expect(CalabashException.class);
            expectedException.expectMessage("Could not find ANDROID_HOME");

            File apk = new File("tests/resources/AndroidTestApplication.apk");
            AndroidRunner androidRunner = new AndroidRunner(apk.getAbsolutePath(), configuration);
            androidRunner.setup();
        }
    }

    @Test
    public void shouldFailForAndroidHome() throws IOException, CalabashException {
        String androidHome = "/user/android/sdk";

        expectedException.expect(CalabashException.class);
        expectedException.expectMessage("Invalid ANDROID_HOME : " + androidHome);

        configuration.setAndroidHome(androidHome);
        File apk = new File("tests/resources/AndroidTestApplication.apk");
        AndroidRunner androidRunner = new AndroidRunner(apk.getAbsolutePath(), configuration);
        androidRunner.setup();
    }

}
