package com.thoughtworks.twist.calabash.android;

import org.jruby.RubyArray;

import java.io.File;
import java.util.List;
import java.util.Map;

public class AndroidApplication {
    private String installedOn;
    private AndroidCalabashWrapper calabashWrapper;

    public AndroidApplication(AndroidCalabashWrapper calabashWrapper, String serial) {
        this.calabashWrapper = calabashWrapper;
        this.installedOn = serial;
    }

    public String getInstalledOnSerial() {
        return installedOn;
    }

    public void setInstalledOn(String installedOn) {
        this.installedOn = installedOn;
    }

    public UIElements query(String query) throws CalabashException {
        RubyArray array = calabashWrapper.query(query);
        return new UIElements(array, query, calabashWrapper);
    }

    /**
     * Fetches all elements in this application and executes callback for each
     * of them
     *
     * @param callback Callback to be executed for each element
     * @throws CalabashException
     */
    public void inspect(InspectCallback callback) throws CalabashException {
        List<TreeNode> tree = new TreeBuilder(calabashWrapper).createTreeFromRoot();
        if (tree.isEmpty()) return;

        for (TreeNode treeNode : tree) {
            Utils.inspectElement(treeNode, 0, callback);
        }
    }

    /**
     * @param dir      Existing directory where the screenshot is saved
     * @param fileName the name of the screenshot
     * @throws CalabashException
     */
    public void takeScreenshot(File dir, String fileName) throws CalabashException {
        if (dir == null)
            throw new CalabashException("Empty directory name");
        if (fileName == null)
            throw new CalabashException("Empty file name");

        if (!dir.isDirectory())
            throw new CalabashException(dir.getAbsolutePath() + " is not a directory");
        if (!dir.canWrite())
            throw new CalabashException(dir.getAbsolutePath() + " is not writeable");

        calabashWrapper.takeScreenShot(dir, fileName);
    }

    /**
     * Read the preferences inside the shared preference denoted by <code>preferenceName</code>
     *
     * @param preferenceName name of the shared preference
     * @return a map of preferences in the shared preference
     * @throws CalabashException
     */
    public Map<String, String> getSharedPreferences(String preferenceName) throws CalabashException {
        if (preferenceName == null || preferenceName.isEmpty()) {
            throw new CalabashException("Invalid preference name");
        }
        return calabashWrapper.getPreferences(preferenceName);
    }

    /**
     * waits for specified condition for the given timeoutInSec
     *
     * @param condition    Condition to wait for
     * @param timeoutInSec timeout in seconds
     * @throws CalabashException
     * @throws OperationTimedoutException
     */
    public void waitFor(ICondition condition, int timeoutInSec) throws CalabashException, OperationTimedoutException {
        calabashWrapper.waitFor(condition, new WaitOptions(timeoutInSec));
    }

    /**
     * Waits for the specified condition with the options specified
     *
     * @param condition Condition to wait for
     * @param options   Wait options
     * @throws CalabashException          When any calabash operations fails
     * @throws OperationTimedoutException When the operation elapsed the timeout period
     */
    public void waitFor(ICondition condition, WaitOptions options) throws CalabashException, OperationTimedoutException {
        calabashWrapper.waitFor(condition, options);
    }

    /**
     * Gets the name of the current activity on the application.
     *
     * @return the name of the activity on the screen
     */
    public String getCurrentActivity() throws CalabashException {
        return calabashWrapper.getCurrentActivity();
    }

    /**
     * simulates the press of 'back' button
     *
     * @throws CalabashException
     */
    public void goBack() throws CalabashException {
        calabashWrapper.performGoBack();

    }

    /**
     * Wait for an activity to come on the screen
     *
     * @param activityName the activity name which you want to wait for
     * @param timeout      in seconds to timeout when condition fails resulting <code>CalabashException</code>
     * @throws CalabashException
     */
    public void waitForActivity(final String activityName, int timeout) throws CalabashException, OperationTimedoutException {
        waitFor(new ICondition() {
            @Override
            public boolean test() throws CalabashException {
                return getCurrentActivity().contains(activityName);
            }
        }, timeout);
    }

    /**
     * Scroll Down by one page
     */
    public void scrollDown() throws CalabashException {
        calabashWrapper.scrollDown();
    }

    /**
     * Scroll Up by one page
     */
    public void scrollUp() throws CalabashException {
        calabashWrapper.scrollUp();
    }

    /**
     * Selects a menu item from the menu
     *
     * @param menuItem The name of the menu item to be selected
     * @throws CalabashException
     */
    public void selectMenuItem(String menuItem) throws CalabashException {
        calabashWrapper.selectMenuItem(menuItem);
    }

    /**
     * Performs a swipe action on the screen
     *
     * @param direction the direction to swipe
     * @throws CalabashException
     */
    public void swipe(Direction direction) throws CalabashException {
        switch (direction) {
            case LEFT:
                calabashWrapper.drag(1, 99, 50, 50, 5);
                break;
            case RIGHT:
                calabashWrapper.drag(99, 1, 50, 50, 5);
                break;
        }
    }

    /**
     * Modify the gps co-ordinates of your emulator
     * <pre>
     * Note: * you have to turn on 'Allow mock location' on the emulator
     *       * manifest should have android.permission.ACCESS_MOCK_LOCATION permission to change the location
     * </pre>
     *
     * @param latitude
     * @param longitude
     * @throws CalabashException
     */
    public void setGPSCoordinates(double latitude, double longitude) throws CalabashException {
        calabashWrapper.setGPSCoordinates(latitude, longitude);
    }

    /**
     * Modify the gps co-ordinates of your emulator.
     * You can specify the location like 'Bangalore, India' and set the co-ordinates with the best match using <a href="http://www.rubygeocoder.com"/>GeoCoder</a>
     * <pre>
     * Note: * you have to turn on 'Allow mock location' on the emulator
     *       * manifest should have android.permission.ACCESS_MOCK_LOCATION permission to change the location
     * </pre>
     *
     * @param location
     * @throws CalabashException
     */
    public void setGPSLocation(String location) throws CalabashException {
        calabashWrapper.setGPSLocation(location);
    }
}
