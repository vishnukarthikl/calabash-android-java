package calabash.java.android;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestUtils {

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
}
