package com.thoughtworks.calabash.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;

import java.io.File;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class AndroidApplication {
    private String installedOn;
    private CalabashWrapper calabashWrapper;

    public AndroidApplication(CalabashWrapper calabashWrapper, String serial) {
        this.calabashWrapper = calabashWrapper;
        this.installedOn = serial;
    }

    public String getInstalledOnSerial() {
        return installedOn;
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
        List<TreeNode> tree = new TreeBuilder(calabashWrapper).createTree();
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
     * press the 'enter' key on the keypad
     *
     * @throws CalabashException
     */
    public void pressEnterKey() throws CalabashException {
        calabashWrapper.pressEnterKey();
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
     * Wait till an element with id appears
     *
     * @param id           id of the element
     * @param timeoutInSec wait time in seconds
     * @throws OperationTimedoutException
     * @throws CalabashException
     */
    public void waitForElementWithId(final String id, int timeoutInSec) throws OperationTimedoutException, CalabashException {
        waitFor(new ICondition() {
            @Override
            public boolean test() throws CalabashException {
                return calabashWrapper.query(format("* id:'%s'", id)).size() > 0;
            }
        }, timeoutInSec);
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

    /**
     * Gets all the root elements available This can be used to make a tree view
     * of all the elements available in the view currently
     *
     * @return list of root elements if available, null otherwise
     * @throws CalabashException
     */
    public List<TreeNode> getRootElements() throws CalabashException {
        final CalabashHttpClient calabashHttpClient = new CalabashHttpClient(calabashWrapper);
        final TreeNodeBuilder treeNodeBuilder = new TreeNodeBuilder(calabashWrapper);
        final TreeBuilder treeBuilder = new TreeBuilder(calabashWrapper, calabashHttpClient, treeNodeBuilder);
        return treeBuilder.createTree();
    }

    /**
     * click and drag from (fromX, fromY) to (toX, toY) where X and Y axis start at top left corner
     *
     * @param fromX source x-coordinate normalized to screen width
     * @param toX   destination x-coordinate normalized to screen width
     * @param fromY source y-coordinate normalized to screen height
     * @param toY   destination y-coordinate normalized to screen height
     * @param steps no.of steps that it takes between the two points
     * @throws CalabashException
     */
    public void drag(int fromX, int toX, int fromY, int toY, int steps) throws CalabashException {
        calabashWrapper.drag(fromX, toX, fromY, toY, steps);
    }

    /**
     * call calabash's performAction function with action and its corresponding args
     * eg:
     * performCalabashAction("enter_text_into_numbered_field","text to be entered","1");
     *
     * @param action action to be performed
     * @param args   list of arguments for the action
     * @throws CalabashException
     */
    public ActionResult performCalabashAction(String action, String... args) throws CalabashException {
        final RubyHash rubyResult = calabashWrapper.performAction(action, args);
        final RubyArray bonusInformationArray = (RubyArray) rubyResult.get("bonusInformation");
        final String message = rubyResult.get("message").toString();
        final boolean success = Boolean.parseBoolean(rubyResult.get("success").toString());
        final Object[] bonusInformation = Utils.toJavaArray(bonusInformationArray);
        return new ActionResult(bonusInformation, message, success);
    }
}
