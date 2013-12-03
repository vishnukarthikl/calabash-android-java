package calabash.java.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;
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
import java.util.Map;

import static calabash.java.android.CalabashLogger.error;
import static calabash.java.android.CalabashLogger.info;
import static java.io.File.separator;
import static java.lang.String.format;

public class AndroidCalabashWrapper {
    private static final String ADB_DEVICE_ARG = "ADB_DEVICE_ARG";
    private static final String APP_PATH = "APP_PATH";
    private static final String TEST_SERVER_PATH = "TEST_APP_PATH";
    private final ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
    private final File rbScriptsPath;
    private final File apk;
    private final AndroidConfiguration configuration;
    private final Environment environment;
    private File gemsDir;
    private AndroidBridge androidBridge;
    private boolean disposed = false;

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
            addContainerEnv("CLASSPATH", jrubyClasspath);
            container.runScriptlet(format("Dir.chdir '%s'", apk.getParent()));
            container.put("ARGV", new String[]{"resign", apk.getAbsolutePath()});
            String calabashAndroid = new File(getCalabashGemDirectory(), "calabash-android").getAbsolutePath();
            container.runScriptlet(PathType.ABSOLUTE, calabashAndroid);

            info("Done signing the app");
            container.put("ARGV", new String[]{"build", apk.getAbsolutePath()});
            container.runScriptlet(PathType.ABSOLUTE, calabashAndroid);

            info("App build complete");

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
            String testServerPath = container.runScriptlet("test_server_path(ENV['APP_PATH'])").toString();
            addContainerEnv(TEST_SERVER_PATH, testServerPath);

            String packageName = container.runScriptlet("package_name(ENV['APP_PATH'])").toString();
            if (configuration.shouldReinstallApp() || !androidBridge.isAppInstalled(packageName, serial)) {
                info("Reinstalling app %s and test server on %s", packageName, serial);
                container.runScriptlet("reinstall_apps");
            } else {
                info("Reinstalling test server on %s", serial);
                container.runScriptlet("reinstall_test_server");
            }

            container.runScriptlet("start_test_server_in_background");
            info("Started the app");
        } catch (Exception e) {
            throw new CalabashException("Error starting the app", e);
        }
    }

    private void addContainerEnv(String envName, String envValue) {
        String cajEnv = "cajEnv";
        container.put(cajEnv, envValue);
        container.runScriptlet(format("ENV['%s'] = %s", envName, cajEnv));
    }

    private void addRequiresAndIncludes(String... modules) throws CalabashException {
        StringBuilder script = new StringBuilder("require 'calabash-android'\n");
        for (String module : modules) {
            script.append(String.format("extend %s\n", module));
        }

        // HACK - Calabash ruby calls embed method when there is a error.
        // This is from cucumber and won't be available in the Jruby
        // environment. So just defining a function to suppress the error
        if (configuration != null && configuration.getScreenshotListener() != null) {
            container.put("@cajScreenshotCallback", configuration.getScreenshotListener());
            script.append("def embed(path,image_type,file_name)\n @cajScreenshotCallback.screenshotTaken(path, image_type, file_name)\n end\n");
        } else {
            script.append("def embed(path,image_type,file_name)\nend\n");
        }

        container.runScriptlet(script.toString());
    }

    private void createDebugCertificateIfMissing() throws CalabashException {
        List<File> keystoreLocation = getKeystoreLocation();
        for (File file : keystoreLocation) {
            if (file.exists()) {
                info("Debug Keystore found at %s", file.getAbsolutePath());
                return;
            }
            info("Could not find debug keystore at %s", file.getAbsolutePath());
        }
        generateDefaultAndroidKeyStore();
    }

    private void generateDefaultAndroidKeyStore() throws CalabashException {
        File destinationKeystoreLocation = new File(apk.getParentFile(), "debug.keystore");
        String[] keygenCommand = getKeygenCommand(destinationKeystoreLocation.getAbsolutePath());
        info("Generating keystore at %s", destinationKeystoreLocation.getAbsolutePath());
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
                info("Found %s in classpath at : %s", resource, url.getFile());
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

    public RubyArray query(String query, String... args) throws CalabashException {
        ensureNotDisposed();
        try {
            info("Executing query - %s", query);
            container.clear();
            container.put("cajQueryString", query);
            container.put("cajQueryArgs", args);

            RubyArray queryResults = null;
            if (args != null && args.length > 0)
                queryResults = (RubyArray) container.runScriptlet("query(cajQueryString, *cajQueryArgs)");
            else
                queryResults = (RubyArray) container.runScriptlet("query(cajQueryString)");

            return queryResults;
        } catch (Exception e) {
            error("Execution of query: %s, failed", e, query);
            throw new CalabashException(String.format("Failed to execute '%s'. %s", query, e.getMessage()));
        }
    }

    public void touch(String query) throws CalabashException {
        try {
            info("Touching - %s", query);
            container.clear();
            container.put("cajQueryString", query);
            container.runScriptlet("touch(cajQueryString)");
        } catch (Exception e) {
            error("Failed to touch on: %s", e, query);
            throw new CalabashException(String.format("Failed to touch on: %s. %s", query, e.getMessage()));
        }

    }

    public void enterText(String text, String query) throws CalabashException {
        try {
            info("Entering text %s into %s", text, query);
            container.clear();
            container.put("cajQueryString", query);
            String setText = String.format("{:setText => '%s'}", text);
            container.runScriptlet(String.format("query(cajQueryString, %s)", setText));
        } catch (Exception e) {
            error("Failed to enter text %s into %s", e, text, query);
            throw new CalabashException(String.format("Failed to enter text %s into %s :%s", text, query, e.getMessage()));
        }
    }

    private void ensureNotDisposed() throws CalabashException {
        if (disposed)
            throw new CalabashException("Object is disposed.");
    }

    public void dispose() throws CalabashException {
        try {
            container.clear();
            container.getProvider().getRuntime().tearDown(true);
            container.terminate();
            disposed = true;
        } catch (Throwable e) {
            error("Failed to dispose container. ", e);
            throw new CalabashException("Failed to dispose container. " + e.getMessage());
        }
    }

    public void takeScreenShot(File dir, String fileName) throws CalabashException {
        try {
            info("Taking screenshot");
            container.clear();
            container.put("cajPrefix", dir.getAbsolutePath() + "/");
            container.put("cajFileName", fileName);
            container.runScriptlet("screenshot(options={:prefix => cajPrefix, :name => cajFileName})");
        } catch (Exception e) {
            error("Failed to take screenshot.", e);
            throw new CalabashException(String.format("Failed to take screenshot. %s", e.getMessage()));
        }
    }

    public Map<String, String> getPreferences(String preferenceName) throws CalabashException {
        try {
            info("Finding preferences: %s", preferenceName);
            container.clear();
            container.put("cajPreferenceName", preferenceName);
            RubyHash preferenceHash = (RubyHash) container.runScriptlet("get_preferences(cajPreferenceName)");
            return (Map<String, String>) Utils.toJavaHash(preferenceHash);

        } catch (Exception e) {
            error("Failed to get preferences: %s", preferenceName);
            throw new CalabashException(String.format("Failed to find preferences: %s", preferenceName));
        }
    }

    public String getCurrentActivity() throws CalabashException {
        try {
            info("Getting current activity");
            container.clear();
            RubyHash activityInfoMap = (RubyHash) container.runScriptlet("performAction('get_activity_name')");
            return (String) Utils.toJavaHash(activityInfoMap).get("message");
        } catch (Exception e) {
            String message = "Failed to get Current Activity";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public boolean isChecked(String query) throws CalabashException {
        try {
            info("Getting isChecked property");
            container.clear();
            container.put("cajQueryString", query);
            RubyArray rubyArray = (RubyArray) container.runScriptlet("query(cajQueryString, :isChecked)");
            Object[] javaArray = Utils.toJavaArray(rubyArray);
            return Boolean.parseBoolean(javaArray[0].toString());
        } catch (Exception e) {
            String message = "Failed to get isChecked property";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void setChecked(String query, boolean checked) throws CalabashException {
        try {
            info("Setting checked to : %s", checked);
            container.clear();
            container.put("cajQueryString", query);
            container.runScriptlet(String.format("query(cajQueryString, {:method_name => :setChecked, :arguments => [%s] })", checked));
        } catch (Exception e) {
            String message = String.format("Failed to set checked property to: %s", checked);
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void performGoBack() throws CalabashException {
        try {
            info("Pressing back button");
            container.clear();
            container.runScriptlet("performAction('go_back')");
        } catch (Exception e) {
            String message = "Failed to go back";
            error(message, e);
            throw new CalabashException(message, e);
        }

    }

    public void scrollDown() throws CalabashException {
        try {
            info("Scrolling down");
            container.clear();
            container.runScriptlet("performAction('scroll_down')");
        } catch (Exception e) {
            String message = "Failed to scroll down";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void scrollUp() throws CalabashException {
        try {
            info("Scrolling up");
            container.clear();
            container.runScriptlet("performAction('scroll_up')");
        } catch (Exception e) {
            String message = "Failed to scroll up";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void selectMenuItem(String menuItem) throws CalabashException {
        try {
            info("Selecting menu item %s", menuItem);
            container.clear();
            container.put("cajMenuItem", menuItem);
            container.runScriptlet("performAction('select_from_menu', cajMenuItem)");
        } catch (Exception e) {
            String message = "Failed to Select menu item" + menuItem;
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void drag(Integer fromX, Integer toX, Integer fromY, Integer toY, Integer steps) throws CalabashException {
        try {
            info("Performing drag from: (%s,%s) to: (%s,%s) in %s steps", fromX, toX, fromY, toY, steps);
            container.clear();
            container.runScriptlet(String.format("performAction('drag', '%d', '%d', '%d', '%d', '%d')", fromX, toX, fromY, toY, steps));
        } catch (Exception e) {
            String message = "Error performing drag";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void longPress(PropertyType withProperty, String property) throws CalabashException {
        String actionName = null;
        try {
            container.clear();
            switch (withProperty) {
                case id:
                    actionName = "long_press_on_view_by_id";
                    break;
                case text:
                    actionName = "press_long_on_text";
            }
            container.runScriptlet(String.format("performAction('%s', '%s')", actionName, property));
        } catch (Exception e) {
            String message = "Failed to long press";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }
}
