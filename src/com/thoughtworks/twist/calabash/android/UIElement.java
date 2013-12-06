/**
 *
 */
package com.thoughtworks.twist.calabash.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;

import static com.thoughtworks.twist.calabash.android.Utils.getIntFromHash;
import static com.thoughtworks.twist.calabash.android.Utils.getStringFromHash;

/**
 * Represents an UI element.
 */
public class UIElement implements AndroidElementAction {

    private final RubyHash data;
    private final String query;
    private final AndroidCalabashWrapper calabashWrapper;

    public UIElement(RubyHash data, String query,
                     AndroidCalabashWrapper calabashWrapper) {
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
        return Utils.getStringFromHash(data, "class");
    }

    /**
     * Gets the element id
     *
     * @return the id property
     */
    public String getId() {
        return Utils.getStringFromHash(data, "id");
    }

    /**
     * Gets the label
     *
     * @return the text property
     */
    public String getText() {
        return Utils.getStringFromHash(data, "text");
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
    public String getDescription() {
        return Utils.getStringFromHash(data, "description");
    }

    /**
     * is the element enabled
     *
     * @return the isEnabled property
     */
    public String isEnabled() {
        return Utils.getStringFromHash(data, "enabled");
    }

    /**
     * Get the content description of this element
     *
     * @return the contentDescription property
     */
    public String getContentDescription() {
        return Utils.getStringFromHash(data, "contentDescription");
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
        RubyHash rect;
        try {
            rect = (RubyHash) data.get("rect");
            if (rect == null)
                return null;
        } catch (Exception e) {
            return null;
        }

        return new Rect(Utils.getIntFromHash(rect, "x"), Utils.getIntFromHash(rect, "y"), Utils.getIntFromHash(rect, "width"), Utils.getIntFromHash(rect, "height"),
                Utils.getIntFromHash(rect, "center_x"), Utils.getIntFromHash(rect, "center_y"));
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
        Utils.inspectElement(this, 0, callback);
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
        return Utils.toJavaObject(rubyArray.get(0));
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

    public boolean equals(Object obj) {
        if (obj instanceof UIElement) {
            UIElement that = (UIElement) obj;
            boolean equal = false;

            if (this.getRect() != null && that.getRect() != null)
                equal = this.getRect().equals(that.getRect());

            if (equal && this.getId() != null && that.getId() != null)
                equal = this.getId().equals(that.getId());

            if (equal && this.getText() != null && that.getText() != null)
                equal = this.getText().equals(that.getText());

            if (equal && this.getElementClass() != null && that.getElementClass() != null)
                equal = this.getElementClass().equals(that.getElementClass());

            return equal;
        }

        return super.equals(obj);
    }

    public String toString() {
        return String.format("id: %s, class: %s, text: %s, description: %s, content description: %s, enabled: %s, rect: %s",
                getId(), getElementClass(), getText(), getDescription(), getContentDescription(), isEnabled(), getRect());
    }
}
