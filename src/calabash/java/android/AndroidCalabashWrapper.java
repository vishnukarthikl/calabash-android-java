package calabash.java.android;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.FileFilter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static calabash.java.android.CalabashLogger.error;
import static java.io.File.separator;
import static java.lang.String.format;

public class AndroidCalabashWrapper {
    private static final String ADB_DEVICE_ARG = "ADB_DEVICE_ARG";
    private static final String APP_PATH = "APP_PATH";
    private static final String TEST_SERVER_PATH = "TEST_APP_PATH";
    private final ScriptingContainer container = new ScriptingContainer(
            LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
    private final File rbScriptsPath;
    private final File apk;
    private final AndroidConfiguration configuration;
    private final Environment environment;
    private File gemsDir;
    private AndroidBridge androidBridge;

    public AndroidCalabashWrapper(File rbScriptsPath, File apk, AndroidConfiguration configuration, Environment environment) throws CalabashException {
        this.rbScriptsPath = rbScriptsPath;
        this.gemsDir = new File(rbScriptsPath, "gems");
        this.apk = apk;
        this.configuration = configuration;
        this.environment = environment;
        this.androidBridge = new AndroidBridge(environment);
        this.initializeScriptingContainer();
    }

    public void setup() throws CalabashException {
        try {
            createDebugCertificateIfMissing();

            //Todo: check if it works on eclipse
            String jrubyClasspath = getClasspathFor("jruby");
            container.runScriptlet(format("ENV['CLASSPATH'] = \"%s\"", jrubyClasspath));
            container.runScriptlet(format("Dir.chdir '%s'", apk.getParent()));
            container.put("ARGV", new String[]{"resign", apk.getAbsolutePath()});
            String calabashAndroid = new File(getCalabashGemDirectory(), "calabash-android").getAbsolutePath();
            container.runScriptlet(PathType.ABSOLUTE, calabashAndroid);

            CalabashLogger.info("Done signing the app");
            container.put("ARGV", new String[]{"build", apk.getAbsolutePath()});
            container.runScriptlet(PathType.ABSOLUTE, calabashAndroid);

            CalabashLogger.info("App build complete");

        } catch (Exception e) {
            error("Failed to setup calabash for project: %s", e, apk.getAbsolutePath());
            throw new CalabashException(format("Failed to setup calabash. %s", e.getMessage()));
        }
    }

    public void start(String serial) throws CalabashException {
        try {
            addRequiresAndIncludes("Calabash::Android::Operations");
            container.runScriptlet(format("Dir.chdir '%s'", apk.getParent()));
            addContainerEnv(ADB_DEVICE_ARG, serial);
            addContainerEnv(APP_PATH, apk.getAbsolutePath());
            String testServerPath = container.runScriptlet(format("test_server_path(\"%s\")", apk.getAbsolutePath())).toString();
            addContainerEnv(TEST_SERVER_PATH, testServerPath);

            String packageName = container.runScriptlet(format("package_name(\"%s\")", apk.getAbsolutePath())).toString();
            if (configuration.shouldReinstallApp() || !androidBridge.isAppInstalled(packageName, serial)) {
                CalabashLogger.info("Reinstalling app %s and test server on %s", packageName, serial);
                container.runScriptlet("reinstall_apps");
            } else {
                CalabashLogger.info("Reinstalling test server on %s", serial);
                container.runScriptlet("reinstall_test_server");
            }
            container.runScriptlet("start_test_server_in_background");
            CalabashLogger.info("Started the app");
        } catch (Exception e) {
            throw new CalabashException("Error starting the app", e);
        }
    }

    private void addContainerEnv(String envName, String envValue) {
        container.runScriptlet(format("ENV['%s'] = \"%s\"", envName, envValue));
    }

    private void addRequiresAndIncludes(String... modules) throws CalabashException {
        StringBuilder script = new StringBuilder("require 'calabash-android'\n");
        for (String module : modules) {
            script.append(String.format("extend %s\n", module));
        }

        container.runScriptlet(script.toString());
    }

    private void createDebugCertificateIfMissing() throws CalabashException {
        List<File> keystoreLocation = getKeystoreLocation();
        for (File file : keystoreLocation) {
            if (file.exists()) {
                CalabashLogger.info("Debug Keystore found at %s", file.getAbsolutePath());
                return;
            }
            CalabashLogger.info("Could not find debug keystore at %s", file.getAbsolutePath());
        }
        generateDefaultAndroidKeyStore();
    }

    private void generateDefaultAndroidKeyStore() throws CalabashException {
        File destinationKeystoreLocation = new File(apk.getParentFile(), "debug.keystore");
        String[] keygenCommand = getKeygenCommand(destinationKeystoreLocation.getAbsolutePath());
        CalabashLogger.info("Generating keystore at %s", destinationKeystoreLocation.getAbsolutePath());
        Utils.runCommand(keygenCommand, "could not generate debug.keystore");
    }

    private String[] getKeygenCommand(final String keystore) {
        return new String[]{environment.getKeytool(), "-genkey", "-v",
                "-keystore", keystore,
                "-alias", "androiddebugkey",
                "-storepass", "android",
                "-keypass", "android",
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "10000",
                "-dname", "CN=AndroidDebug,O=Android,C=US"};
    }

    private List<File> getKeystoreLocation() {
        return new ArrayList<File>() {{
            add(new File(System.getProperty("user.home") + separator + ".android" + separator + "debug.keystore"));
            add(new File(System.getProperty("user.home") + separator + ".local" + separator + "share" + separator + "Xamarin" + separator + "Mono for Android" + separator + "debug.keystore"));
            add(new File(apk.getParentFile(), "debug.keystore"));
            add(new File("AppData" + separator + "Local" + separator + "Xamarin" + separator + "Mono for Android" + separator + "debug.keystore"));
        }};
    }

    private String getClasspathFor(String resource) {
        URLClassLoader classLoader = (URLClassLoader) container.getClassLoader();
        URL[] urls = classLoader.getURLs();
        for (URL url : urls) {
            if (url.toString().contains(resource)) {
                CalabashLogger.info("Found %s in classpath at : %s", resource, url.getFile());
                return url.getFile();
            }
        }
        CalabashLogger.error("Could not find %s in classpath", resource);
        return null;
    }

    private File getCalabashGemDirectory() throws CalabashException {
        File[] calabashGemPath = gemsDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith("calabash-android");
            }
        });

        if (calabashGemPath.length == 0)
            throw new CalabashException(format("Error finding 'calabash-android' in the gempath : %s", gemsDir.getAbsolutePath()));
        if (calabashGemPath.length > 1)
            throw new CalabashException(format("Multiple matches for 'calabash-android' in the gempath : %s", gemsDir.getAbsolutePath()));
        return new File(calabashGemPath[0], "bin");
    }

    private void initializeScriptingContainer() throws CalabashException {
        container.setHomeDirectory(new File(rbScriptsPath, "jruby.home").getAbsolutePath());

        HashMap<String, String> environmentVariables = new HashMap<String, String>();
        environmentVariables.putAll(System.getenv());
        environmentVariables.putAll(environment.getEnvVariables());
        container.setEnvironment(environmentVariables);

        container.getLoadPaths().addAll(getLoadPaths());
        container.setErrorWriter(new StringWriter());
    }

    private List<String> getLoadPaths() throws CalabashException {
        ArrayList<String> loadPaths = new ArrayList<String>();
        File[] gems = gemsDir.listFiles(new FileFilter() {

            public boolean accept(File arg0) {
                return arg0.isDirectory();
            }
        });

        if (gems == null || gems.length == 0)
            throw new CalabashException("Couldn't find any gems inside " + gemsDir.getAbsolutePath());

        for (File gem : gems) {
            File libPath = new File(gem, "lib");
            loadPaths.add(libPath.getAbsolutePath());
        }

        return loadPaths;
    }
}
