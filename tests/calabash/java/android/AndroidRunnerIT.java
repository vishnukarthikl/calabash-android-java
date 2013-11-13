package calabash.java.android;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static calabash.java.android.TestUtils.createTempDir;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AndroidRunnerIT {

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
        File testServersDir = new File(tempDir, "test_servers");

        assertTrue(testServersDir.exists());
        File[] testServerApk = testServersDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".apk");
            }
        });

        assertEquals(1, testServerApk.length);
    }
}
