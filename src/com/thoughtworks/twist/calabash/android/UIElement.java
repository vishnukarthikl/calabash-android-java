/**
 *
 */
package com.thoughtworks.twist.calabash.android;

import org.joda.time.DateTime;
import org.jruby.RubyArray;
import org.jruby.RubyHash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.twist.calabash.android.CalabashLogger.error;
import static java.lang.Double.parseDouble;

/**
 * Represents an UI element.
 */
public class UIElement implements AndroidElementAction {

    private final Map<Object, Object> data;
    private final String query;
    private final CalabashWrapper calabashWrapper;

    public UIElement(RubyHash data, String query, CalabashWrapper calabashWrapper) {
        this.data = (Map<Object, Object>) Utils.toJavaHash(data);
        this.query = query;
        this.calabashWrapper = calabashWrapper;
    }

    public UIElement(HashMap<Object, Object> data, String query, CalabashWrapper calabashWrapper) {
        this.data = data;
        this.query = query;
        this.calabashWrapper = calabashWrapper;
    }

    /**
     * Get element's class
     *
     * @return the class property
     */
    public String getElementClass() {
        return Utils.toString(data.get("class"));
    }

    /**
     * Gets the element id
     *
     * @return the id property
     */
    public String getId() {
        return Utils.toString(data.get("id"));
    }

    /**
     * Gets the label
     *
     * @return the text property
     */
    public String getText() {
        return Utils.toString(data.get("text"));
    }

    /**
     * Set the text property of the element
     *
     * @param text
     * @throws CalabashException
     */
    public void setText(String text) throws CalabashException {
        calabashWrapper.enterText(text, this.getQuery());

    }

    /**
     * Get description about this element
     *
     * @return the description property
     */
    public String getDescription() throws CalabashException {
        return getElementProperty("description");
    }

    /**
     * is the element enabled
     *
     * @return the isEnabled property
     */
    public boolean isEnabled() {
        return Boolean.parseBoolean(Utils.toString(data.get("enabled")));
    }

    /**
     * Get the content description of this element
     *
     * @return the contentDescription property
     */
    public String getContentDescription() throws CalabashException {
        return getElementProperty("contentDescription");
    }

    private String getElementProperty(String property) throws CalabashException {
        Object description = data.get(property);
        if (description == null) {
            description = getProperty(property);
        }
        return Utils.toString(description);
    }

    /**
     * Gets the underlying query used to locate this element
     *
     * @return query
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * Gets the rectangle
     *
     * @return the rectangle
     */
    public Rect getRect() {
        Map<String, String> rect;
        try {
            rect = (Map<String, String>) data.get("rect");
            if (rect == null)
                return null;
        } catch (Exception e) {
            return null;
        }

        return new Rect(parseDouble(rect.get("x")),
                parseDouble(rect.get("y")),
                parseDouble(rect.get("width")),
                parseDouble(rect.get("height")),
                parseDouble(rect.get("center_x")),
                parseDouble(rect.get("center_y")));
    }

    /**
     * Gets all the child elements for this element
     *
     * @return List of UIElement
     * @throws CalabashException
     */
    public UIElements children() throws CalabashException {
        String q = query + " child *";
        RubyArray result = calabashWrapper.query(q);
        return new UIElements(result, q, calabashWrapper);
    }

    /**
     * Inspects the current element and it's child elements and call callback
     * for each element
     *
     * @param callback Callback to be invoked
     * @throws CalabashException
     */
    public void inspect(InspectCallback callback) throws CalabashException {
        TreeBuilder treeBuilder = new TreeBuilder(calabashWrapper);
        TreeNode tree = treeBuilder.createTreeFrom(this);
        Utils.inspectElement(tree, 0, callback);
    }

    /**
     * set the checked property of the element. It is advisable to do it only on a check box
     *
     * @param checked
     * @throws CalabashException
     */
    public void setChecked(boolean checked) throws CalabashException {
        calabashWrapper.setChecked(this.getQuery(), checked);
    }

    /**
     * get the <code>selector</code> property of the element
     *
     * @param selector the property of the element
     * @return the value of the selector property
     * @throws CalabashException
     */
    public Object getProperty(String selector) throws CalabashException {
        RubyArray rubyArray = calabashWrapper.query(this.getQuery(), selector);
        Object value = Utils.toJavaObject(rubyArray.get(0));
        if (value != null && value.toString().toLowerCase().contains("no accessor")) {
            return null;
        }
        return value;
    }

    public void touch() throws CalabashException {
        calabashWrapper.touch(query);
    }

    public void longPress() throws CalabashException {
        String elementId = getId();
        String text = getText();
        if (elementId != null)
            calabashWrapper.longPress(PropertyType.id, elementId);
        else if (text != null && !text.isEmpty()) {
            calabashWrapper.longPress(PropertyType.text, text);
        } else {
            throw new CalabashException("Failed to long press - The element doesn't have an id or text property");
        }
    }

    /**
     * @return the date value represented by the UI element if it is a date picker
     * @throws CalabashException
     */
    public DateTime getDate() throws CalabashException {
        return calabashWrapper.getDate(getQuery());
    }

    /**
     * set the date if it is a date picker
     *
     * @param date date to be set
     * @throws CalabashException
     */
    public void setDate(DateTime date) throws CalabashException {
        calabashWrapper.setDate(getQuery(), date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }

    /**
     * get the tree with the current element as the root
     *
     * @return treeNode
     * @throws CalabashException
     */
    public TreeNode getTree() throws CalabashException {
        return new TreeBuilder(calabashWrapper).createTreeFrom(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UIElement uiElement = (UIElement) o;

        if (getId() != null ? !getId().equals(uiElement.getId()) : uiElement.getId() != null) return false;
        if (getElementClass() != null ? !getElementClass().equals(uiElement.getElementClass()) : uiElement.getElementClass() != null)
            return false;
        if (getRect() != null ? !getRect().equals(uiElement.getRect()) : uiElement.getRect() != null) return false;
        if (getText() != null ? !getText().equals(uiElement.getText()) : uiElement.getText() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        List<Object> objects = new ArrayList<Object>();
        objects.add(getId());
        objects.add(getElementClass());
        objects.add(getRect());
        objects.add(getText());
        int result = 0;
        for (Object object : objects) {
            result = 31 * result + (object != null ? object.hashCode() : 0);
        }
        return result;
    }

    public String toString() {
        try {
            return String.format("id: %s, class: %s, text: %s, description: %s, content description: %s, enabled: %s, rect: %s",
                    getId(), getElementClass(), getText(), getDescription(), getContentDescription(), isEnabled(), getRect());
        } catch (CalabashException e) {
            error("Unable to get string value of element", e);
        }
        return "";
    }

}
