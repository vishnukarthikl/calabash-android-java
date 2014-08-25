package com.thoughtworks.calabash.android;

import org.joda.time.DateTime;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.FileFilter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.calabash.android.CalabashLogger.error;
import static com.thoughtworks.calabash.android.CalabashLogger.info;
import static java.io.File.separator;
import static java.lang.String.format;

public class CalabashWrapper {
    public static final String QUERY_STRING = "cajQueryString";
    public static final String QUERY_ARGS = "cajQueryArgs";
    public static final String SCREENSHOT_PREFIX = "cajPrefix";
    public static final String SCREENSHOT_FILENAME = "cajFileName";
    public static final String PREFERENCE_NAME = "cajPreferenceName";
    public static final String MENU_ITEM = "cajMenuItem";
    public static final String WAIT_CONDITION = "cajWaitCondition";
    public static final String WAIT_TIMEOUT = "cajWaitTimeout";
    public static final String WAIT_RETRY_FREQ = "cajWaitRetryFreq";
    public static final String WAIT_POST_TIMEOUT = "cajWaitPostTimeout";
    public static final String WAIT_TIMEOUT_MESSAGE = "cajWaitTimeoutMessage";
    public static final String WAIT_SHOULD_TAKE_SCREENSHOT = "cajWaitShouldTakeScreenshot";
    public static final String ENVIRONMENT_VAR_PLACEHOLDER = "cajEnv";
    public static final String ARGV = "ARGV";
    private static final String ADB_DEVICE_ARG = "ADB_DEVICE_ARG";
    private static final String APP_PATH = "APP_PATH";
    private static final String TEST_SERVER_PATH = "TEST_APP_PATH";
    private static final String ACTION = "cajAction";
    private static final String ACTION_ARGS = "cajActionArgs";
    private final ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
    private final File rbScriptsPath;
    private final File apk;
    private final AndroidConfiguration configuration;
    private final Environment environment;
    private File gemsDir;
    private AndroidBridge androidBridge;
    private boolean disposed = false;
    private long pauseTimeInMilliSec = 500;

    public CalabashWrapper(File rbScriptsPath, File apk, AndroidConfiguration configuration, Environment environment) throws CalabashException {
        this.rbScriptsPath = rbScriptsPath;
        this.gemsDir = new File(rbScriptsPath, "gems");
        this.apk = apk;
        this.configuration = configuration;
        this.environment = environment;
        this.androidBridge = new AndroidBridge(environment);
        this.initializeScriptingContainer();
        if (configuration != null && configuration.getPauseTimeInMs() >= 0)
            pauseTimeInMilliSec = configuration.getPauseTimeInMs();

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


    public void setup() throws CalabashException {
        try {
            addSystemCommandHack();
            createDebugCertificateIfMissing();
            String jrubyClasspath = getClasspathFor("jruby");
            addContainerEnv("CLASSPATH", jrubyClasspath);
            container.runScriptlet(format("Dir.chdir '%s'", apk.getParent()));

            container.put(ARGV, new String[]{"resign", apk.getAbsolutePath()});
            String calabashAndroid = new File(getCalabashGemDirectory(), "calabash-android").getAbsolutePath();
            container.runScriptlet(PathType.ABSOLUTE, calabashAndroid);
            info("Done signing the app");

            container.put(ARGV, new String[]{"build", apk.getAbsolutePath()});
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
            addSystemCommandHack();
            container.runScriptlet(format("Dir.chdir '%s'", apk.getParentFile().getAbsolutePath()));
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
            error("Error starting the app: ", e);
            throw new CalabashException("Error starting the app:" + e.getMessage(), e);
        }
    }

    //HACK - Jruby system call fails crashing the JVM on attempting to start test server command which redirects error stream to input stream.
    //Overriding kernel system call to execute command via backtick for the particular edge case. Rest of the calls will be executed via the regular
    //kernel system call. Bug has been reported on jruby - https://github.com/jruby/jruby/issues/1500
    //TODO: Remove this once the bug is fixed on jruby and the new jruby jar is added.
    private void addSystemCommandHack() {
        StringBuilder script = new StringBuilder();

        script.append(" def system(cmd)\n" +
                "  `#{cmd}`\n" +
                "  return $?.success?\n" +
                " end\n");

        container.runScriptlet(script.toString());
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

    private String getClasspathFor(String resource) throws CalabashException, UnsupportedEncodingException {
        if (environment.getJrubyHome() != null) {
            return environment.getJrubyHome();
        }

        ClassLoader classLoader = container.getClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            //eclipse doesn't give URLClassLoader
            error("Could not get %s path", resource);
            throw new CalabashException(String.format("Could not get %s path", resource));
        }
        URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
        URL[] urls = urlClassLoader.getURLs();
        for (URL url : urls) {
            if (url.toString().contains(resource)) {
                String jrubyPath = URLDecoder.decode(url.getFile(), "UTF-8");
                info("Found %s in classpath at : %s", resource, jrubyPath);
                return jrubyPath;
            }
        }
        error("Could not find %s in classpath", resource);
        throw new CalabashException(String.format("Could not find %s in classpath", resource));
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

    public RubyArray query(String query, String... args) throws CalabashException {
        ensureNotDisposed();
        try {
            info("Executing query - %s", query);
            container.put(QUERY_STRING, query);
            container.put(QUERY_ARGS, args);

            RubyArray queryResults = null;
            if (args != null && args.length > 0)
                queryResults = (RubyArray) container.runScriptlet(String.format("query(%s, *%s)", QUERY_STRING, QUERY_ARGS));
            else
                queryResults = (RubyArray) container.runScriptlet(String.format("query(%s)", QUERY_STRING));

            return queryResults;
        } catch (Exception e) {
            error("Execution of query: %s, failed", e, query);
            throw new CalabashException(String.format("Failed to execute '%s'. %s", query, e.getMessage()));
        }
    }

    public void touch(String query) throws CalabashException {
        try {
            info("Touching - %s", query);
            container.put(QUERY_STRING, query);
            container.runScriptlet(String.format("touch(%s)", QUERY_STRING));
            pause();
        } catch (Exception e) {
            error("Failed to touch on: %s", e, query);
            throw new CalabashException(String.format("Failed to touch on: %s. %s", query, e.getMessage()));
        }

    }

    public void enterText(String text, String query) throws CalabashException {
        try {
            info("Entering text %s into %s", text, query);
            container.put(QUERY_STRING, query);
            container.put("TEXT", text);
            container.runScriptlet(String.format("enter_text(%s,%s)", QUERY_STRING, "TEXT"));
            pause();
        } catch (Exception e) {
            error("Failed to enter text %s into %s", e, text, query);
            throw new CalabashException(String.format("Failed to enter text %s into %s :%s", text, query, e.getMessage()));
        }
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
            container.put(SCREENSHOT_PREFIX, dir.getAbsolutePath() + "/");
            container.put(SCREENSHOT_FILENAME, fileName);
            container.runScriptlet(String.format("screenshot(options={:prefix => %s, :name => %s})", SCREENSHOT_PREFIX, SCREENSHOT_FILENAME));
        } catch (Exception e) {
            error("Failed to take screenshot.", e);
            throw new CalabashException(String.format("Failed to take screenshot. %s", e.getMessage()));
        }
    }

    public Map<String, String> getPreferences(String preferenceName) throws CalabashException {
        try {
            info("Finding preferences: %s", preferenceName);
            container.put(PREFERENCE_NAME, preferenceName);
            RubyHash preferenceHash = (RubyHash) container.runScriptlet(String.format("get_preferences(%s)", PREFERENCE_NAME));
            return (Map<String, String>) Utils.toJavaHash(preferenceHash);
        } catch (Exception e) {
            error("Failed to get preferences: %s", preferenceName);
            throw new CalabashException(String.format("Failed to find preferences: %s", preferenceName));
        }
    }

    public String getCurrentActivity() throws CalabashException {
        try {
            info("Getting current activity");
            RubyHash activityInfoMap = (RubyHash) container.runScriptlet("perform_action('get_activity_name')");
            String activityName = (String) Utils.toJavaHash(activityInfoMap).get("message");
            info("Current activity: %s", activityName);
            return activityName;
        } catch (Exception e) {
            String message = "Failed to get Current Activity";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public DateTime getDate(String query) throws CalabashException {
        try {
            info("Getting date");
            container.put(QUERY_STRING, query);
            RubyArray rubyArray = (RubyArray) container.runScriptlet(String.format("query(%s, :getYear)", QUERY_STRING));
            int year = Utils.getFirstIntValue(rubyArray);

            rubyArray = (RubyArray) container.runScriptlet(String.format("query(%s, :getMonth)", QUERY_STRING));
            int month = Utils.getFirstIntValue(rubyArray);

            rubyArray = (RubyArray) container.runScriptlet(String.format("query(%s, :getDayOfMonth)", QUERY_STRING));
            int day = Utils.getFirstIntValue(rubyArray);

            return new DateTime(year, month + 1, day, 0, 0);
        } catch (Exception e) {
            String message = "Error getting date";
            throw new CalabashException(message, e);
        }
    }

    public void setChecked(String query, boolean checked) throws CalabashException {
        try {
            info("Setting checked to : %s", checked);
            container.put(QUERY_STRING, query);
            container.runScriptlet(String.format("query(%s, {:method_name => :setChecked, :arguments => [%s] })", QUERY_STRING, checked));
        } catch (Exception e) {
            String message = String.format("Failed to set checked property to: %s", checked);
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void performGoBack() throws CalabashException {
        try {
            info("Pressing back button");
            container.runScriptlet("press_back_button");
            pause();
        } catch (Exception e) {
            String message = "Failed to go back";
            error(message, e);
            throw new CalabashException(message, e);
        }

    }

    public void pressEnterKey() throws CalabashException {
        try {
            info("Pressing enter key");
            container.runScriptlet("perform_action('send_key_enter')");
            pause();
        } catch (Exception e) {
            String message = "Failed to press enter key";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void scrollDown() throws CalabashException {
        try {
            info("Scrolling down");
            container.runScriptlet("scroll_down");
        } catch (Exception e) {
            String message = "Failed to scroll down";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void scrollUp() throws CalabashException {
        try {
            info("Scrolling up");
            container.runScriptlet("scroll_up");
        } catch (Exception e) {
            String message = "Failed to scroll up";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void selectMenuItem(String menuItem) throws CalabashException {
        info("Selecting menu item %s", menuItem);
        try {
            touch(String.format("com.android.internal.view.menu.ActionMenuItemView marked:\"%s\"", menuItem));
        } catch (CalabashException ce) {
            selectOptionsMenuItem(menuItem);
        }
    }

    private void selectOptionsMenuItem(String menuItem) throws CalabashException {
        try {
            container.put(MENU_ITEM, menuItem);
            container.runScriptlet(String.format("select_options_menu_item %s", MENU_ITEM));
            pause();
        } catch (Exception e) {
            throw new CalabashException(String.format("Failed to select menu item '%s'", menuItem));
        }
    }

    public void drag(Integer fromX, Integer toX, Integer fromY, Integer toY, Integer steps) throws CalabashException {
        try {
            info("Performing drag from: (%s,%s) to: (%s,%s) in %s steps", fromX, fromY, toX, toY, steps);
            container.runScriptlet(String.format("perform_action('drag', '%d', '%d', '%d', '%d', '%d')", fromX, toX, fromY, toY, steps));
        } catch (Exception e) {
            String message = "Error performing drag";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void longPress(String query) throws CalabashException {
        try {
            info("Long pressing element: %s", query);
            container.runScriptlet(String.format("long_press_when_element_exists(\"%s\")", query));
            pause();
        } catch (Exception e) {
            String message = "Failed to long press";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void setGPSCoordinates(double latitude, double longitude) throws CalabashException {
        try {
            info("Setting gps coordinates %f : %f", latitude, longitude);
            container.runScriptlet(String.format("set_gps_coordinates(%f, %f)", latitude, longitude));

        } catch (Exception e) {
            String message = String.format("Failed to set coordinates %f : %f", latitude, longitude);
            error(message, e);
            throw new CalabashException(message, e);
        }

    }

    public void setGPSLocation(String location) throws CalabashException {
        try {
            info("Setting GPS location to : %s", location);
            container.runScriptlet(String.format("set_gps_coordinates_from_location('%s')", location));
        } catch (Exception e) {
            String message = "Failed to set gps location to : " + location;
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void setDate(String query, int year, int month, int day) throws CalabashException {
        try {
            info("Setting date: %d-%d-%d - format yyyy-mm-dd", year, month, day);
            container.put(QUERY_STRING, query);
            container.runScriptlet(String.format("query(%s, {:method_name => :updateDate, :arguments => [%d,%d,%d]})", QUERY_STRING, year, month - 1, day));
        } catch (Exception e) {
            String message = String.format("Failed to set date : %d-%d-%d", year, month, day);
            error(message, e);
            throw new CalabashException(message, e);
        }

    }

    public RubyHash performAction(String action, String[] args) throws CalabashException {
        try {
            info("performing action %s with args %s", action, Utils.getStringFromArray(args));
            container.put(ACTION, action);
            container.put(ACTION_ARGS, args);
            return (RubyHash) container.runScriptlet(String.format("perform_action(%s,*%s)", ACTION, ACTION_ARGS));
        } catch (Exception e) {
            String message = String.format("Failed to perform action %s with args %s", action, Utils.getStringFromArray(args));
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void waitFor(ICondition condition, WaitOptions options) throws CalabashException, OperationTimedoutException {
        try {
            info("Waiting for condition");
            addRequiresAndIncludes("Calabash::Android::WaitHelpers");
            container.put(WAIT_CONDITION, condition);
            String waitOptionsHash = getWaitOptionsHash(options);
            if (waitOptionsHash == null)
                container.runScriptlet(String.format("wait_for { %s.test }", WAIT_CONDITION));
            else {
                container.runScriptlet(String.format("wait_for(%s) { %s.test }", waitOptionsHash, WAIT_CONDITION));
            }
        } catch (Exception e) {
            handleWaitException(e, options);
        }
    }

    private void ensureNotDisposed() throws CalabashException {
        if (disposed)
            throw new CalabashException("Object is disposed.");
    }

    private void handleWaitException(Exception e, WaitOptions options) throws OperationTimedoutException, CalabashException {
        if (e.getMessage().contains("WaitError")) {
            String message = null;
            if (options != null)
                message = options.getTimeoutMessage();

            error("Wait Timed-out");
            throw new OperationTimedoutException(message == null ? "Timed out waiting..." : message);
        } else {
            error("Failed to wait for condition. %s", e, e.getMessage());
            throw new CalabashException(String.format("Failed to wait for condition. %s", e.getMessage()));
        }
    }

    private String getWaitOptionsHash(WaitOptions options) {
        if (options == null)
            return null;
        else {
            container.put(WAIT_TIMEOUT, options.getTimeoutInSec());
            container.put(WAIT_RETRY_FREQ, options.getRetryFreqInSec());
            container.put(WAIT_POST_TIMEOUT, options.getPostTimeoutInSec());
            container.put(WAIT_TIMEOUT_MESSAGE, options.getTimeoutMessage());
            container.put(WAIT_SHOULD_TAKE_SCREENSHOT,
                    options.shouldScreenshotOnError());
            return String.format("{:timeout => %s, " +
                            ":retry_frequency => %s, " +
                            ":post_timeout => %s, " +
                            ":timeout_message => %s, " +
                            ":screenshot_on_error => %s}",
                    WAIT_TIMEOUT,
                    WAIT_RETRY_FREQ,
                    WAIT_POST_TIMEOUT,
                    WAIT_TIMEOUT_MESSAGE,
                    WAIT_SHOULD_TAKE_SCREENSHOT);
        }
    }

    private void pause() {
        try {
            Thread.sleep(pauseTimeInMilliSec);
        } catch (InterruptedException ignored) {
        }
    }

    private void addContainerEnv(String envName, String envValue) {
        String cajEnv = ENVIRONMENT_VAR_PLACEHOLDER;
        container.put(cajEnv, envValue);
        container.runScriptlet(format("ENV['%s'] = %s", envName, cajEnv));
    }

    public String getTestServerPort() throws CalabashException {
        addRequiresAndIncludes("Calabash::Android::Operations");
        final Object serverPort = container.runScriptlet("default_device.default_server_port");
        return serverPort.toString();
    }

    public boolean elementExistsById(String id) throws CalabashException {
        try {
            info("Checking for element's existence");
            Boolean existsAsUIElement = (Boolean) container.runScriptlet(String.format("element_exists(\"%s\")", id));
            Boolean existsAsWebView = (Boolean) container.runScriptlet("element_exists(\"webView css:'#" + id + "'\")");
            return existsAsUIElement || existsAsWebView;
        } catch (Exception e) {
            String message = "Failed to check for element's existence";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void hideKeyboard() throws CalabashException {
        try {
            info("hiding keyboard");
            container.runScriptlet("hide_soft_keyboard");
        } catch (Exception e) {
            String message = "Failed hide keyboard";
            error(message, e);
            throw new CalabashException(message, e);
        }
    }

    public void waitForActivity(String activityName, int timeout) throws OperationTimedoutException {
        try {
            info("waiting for activity %s for %d seconds", activityName, timeout);
            container.runScriptlet(String.format("wait_for_activity('%s',%d)", activityName, timeout));
        } catch (Exception e) {
            String message = String.format("Activity '%s' did not appear within %d seconds", activityName, timeout);
            error(message, e);
            throw new OperationTimedoutException(message);
        }
    }
}
