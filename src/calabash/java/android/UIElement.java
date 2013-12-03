/**
 *
 */
package calabash.java.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;

import static calabash.java.android.Utils.getIntFromHash;
import static calabash.java.android.Utils.getStringFromHash;

/**
 * Represents an UI element.
 */
public class UIElement {

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
     * @return
     */
    public String getElementClass() {
        return getStringFromHash(data, "class");
    }

    /**
     * Gets the element id
     *
     * @return
     */
    public String getId() {
        return getStringFromHash(data, "id");
    }

    /**
     * Gets the label
     *
     * @return
     */
    public String getText() {
        return getStringFromHash(data, "text");
    }

    public void setText(String text) throws CalabashException {
        calabashWrapper.enterText(text, this.getQuery());

    }

    /**
     * Get description about this element
     *
     * @return
     */
    public String getDescription() {
        return getStringFromHash(data, "description");
    }

    /**
     * is the element enabled
     *
     * @return
     */
    public String isEnabled() {
        return getStringFromHash(data, "enabled");
    }

    /**
     * Get the content description of this element
     *
     * @return
     */
    public String getContentDescription() {
        return getStringFromHash(data, "contentDescription");
    }

    /**
     * Gets the underlying query used to locate this element
     *
     * @return Query
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * Gets the rectangle
     *
     * @return
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

        return new Rect(getIntFromHash(rect, "x"), getIntFromHash(rect, "y"), getIntFromHash(rect, "width"), getIntFromHash(rect, "height"),
                getIntFromHash(rect, "center_x"), getIntFromHash(rect, "center_y"));
    }

    public String toString() {
        return String.format("id: %s, class: %s, text: %s, description: %s, content description: %s, enabled: %s, rect: %s",
                getId(), getElementClass(), getText(), getDescription(), getContentDescription(), isEnabled(), getRect());
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
     * performs touch operation on the particular element
     *
     * @throws CalabashException
     */
    public void touch() throws CalabashException {
        calabashWrapper.touch(query);
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

    public boolean isChecked() throws CalabashException {
        return calabashWrapper.isChecked(this.getQuery());
    }

    public void setChecked(boolean checked) throws CalabashException {
        calabashWrapper.setChecked(this.getQuery(), checked);
    }

    public Object getProperty(String selector) throws CalabashException {
        RubyArray rubyArray = calabashWrapper.query(this.getQuery(), selector);
        return Utils.toJavaObject(rubyArray.get(0));
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
}
