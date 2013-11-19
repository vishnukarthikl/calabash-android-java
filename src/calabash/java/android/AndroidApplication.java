package calabash.java.android;

import org.jruby.RubyArray;

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
     * @param callback
     *            Callback to be executed for each element
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
}
