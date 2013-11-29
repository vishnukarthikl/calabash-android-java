package calabash.java.android;

import org.jruby.RubyArray;

import java.io.File;
import java.util.Map;

public class AndroidApplication {
    private String installedOn;
    private AndroidCalabashWrapper calabashWrapper;

    public AndroidApplication(AndroidCalabashWrapper calabashWrapper, String serial) {
        this.calabashWrapper = calabashWrapper;
        this.installedOn = serial;
    }

    public String getInstalledOn() {
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
        UIElements rootElements = getRootElements();
        if (rootElements == null)
            return;

        for (UIElement root : rootElements) {
            Utils.inspectElement(root, 0, callback);
        }
    }

    /**
     * Gets all the root elements available This can be used to make a tree view
     * of all the elements available in the view currently
     *
     * @return list of root elements if available, null otherwise
     * @throws CalabashException
     */
    public UIElements getRootElements() throws CalabashException {
        RubyArray allElements = calabashWrapper.query("*");
        if (allElements.size() == 0)
            return null;

        UIElements rootElements = new UIElements();
        for (int i = 0; i < allElements.size(); i++) {
            String query = String.format("* index:%d", i);
            UIElement rootElement = getRootElement(query);
            if (rootElement != null && !rootElements.contains(rootElement))
                rootElements.add(rootElement);
        }

        return rootElements;
    }

    private UIElement getRootElement(String query) throws CalabashException {
        UIElement rootElement = null;
        RubyArray result = calabashWrapper.query(query);
        if (result.size() == 0)
            return null;
        else {
            rootElement = new UIElements(result, query, calabashWrapper).get(0);
            UIElement element = getRootElement(query + " parent * index:0");
            if (element != null)
                rootElement = element;
        }

        return rootElement;
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

    public Map<String, String> getSharedPreferences(String preferenceName) throws CalabashException {
        if (preferenceName == null || preferenceName.isEmpty()) {
            throw new CalabashException("Invalid preference name");
        }
        return calabashWrapper.getPreferences(preferenceName);
    }

    public void waitFor(ICondition condition, int timeout) throws CalabashException {
        ConditionalWaiter conditionalWaiter = new ConditionalWaiter(condition);
        conditionalWaiter.run(timeout);
    }

    /**
     * Gets the name of the current activity on the application.
     *
     * @return
     */
    public String getCurrentActivity() throws CalabashException {
        return calabashWrapper.getCurrentActivity();
    }

    public void goBack() throws CalabashException {
        calabashWrapper.performGoBack();

    }

    public void waitForActivity(final String activityName, int timeoutMillis) throws CalabashException {
        waitFor(new ICondition() {
            @Override
            public boolean test() throws CalabashException {
                return getCurrentActivity().contains(activityName);
            }
        }, timeoutMillis);

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
}
