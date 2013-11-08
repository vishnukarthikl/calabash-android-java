package calabash.java.android;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

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
    public void shouldNotThrowCalabashExceptionIfFound() throws Exception {
        File tempFile = File.createTempFile("foo", ".apk");
        new AndroidRunner(tempFile.getPath());
    }
}
