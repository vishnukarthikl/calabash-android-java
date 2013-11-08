package calabash.java.android;

import java.io.File;

public class AndroidCalabashWrapper {
    private final File gemPath;
    private final File apk;
    private final AndroidConfiguration configuration;

    public AndroidCalabashWrapper(File gemPath, File apk, AndroidConfiguration configuration) {
        this.gemPath = gemPath;
        this.apk = apk;
        this.configuration = configuration;
    }

    public void setup() {


    }
}
