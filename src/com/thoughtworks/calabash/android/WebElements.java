package com.thoughtworks.calabash.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;

import java.util.ArrayList;

import static com.thoughtworks.calabash.android.CalabashLogger.warn;

public class WebElements extends ArrayList<UIElement> {
    public WebElements(RubyArray elementsArray, String query, CalabashWrapper calabashWrapper) {
        query = query.trim();
        for (Object element : elementsArray) {
            RubyHash object = (RubyHash) element;
            this.add(new UIElement(object, query, calabashWrapper));
        }
    }

    /**
     * touch the webview element, if there are multiple elements, only the first one will be touched
     *
     * @throws CalabashException
     */
    public void touch() throws CalabashException {
        getFirstElement().touch();
    }

    private UIElement getFirstElement() throws CalabashException {
        ensureCollectionIsNotEmpty();
        if (this.size() > 1) {
            warn("There are multiple webview elements, so considering only the first element");
        }
        return this.get(0);
    }

    /**
     * set text to the webview element, if there are multiple elements, only the first one will be considered
     *
     * @param text
     * @throws CalabashException
     */
    public void setText(String text) throws CalabashException {
        getFirstElement().setText(text);
    }

    /**
     * fetch the text content of the weblement
     *
     * @return inner text of the element
     */
    public String getText() throws CalabashException {
        return getFirstElement().getTextContent();
    }

    /**
     * fetch the getValue of the webelement
     *
     * @return getValue of the element
     * @throws CalabashException
     */
    public String getValue() throws CalabashException {
        return getFirstElement().getValue();
    }

    /**
     * @return bounds of the webelement
     */
    public Rect getRect() throws CalabashException {
        return getFirstElement().getRect();
    }

    /**
     * @return the css class of the webelement
     */
    public String getElementClass() throws CalabashException {
        return getFirstElement().getElementClass();
    }

    /**
     * @return the css id of the webelement
     * @throws CalabashException
     */
    public String getId() throws CalabashException {
        return getFirstElement().getId();
    }

    private void ensureCollectionIsNotEmpty() throws CalabashException {
        if (this.size() == 0) {
            throw new CalabashException("Cannot perform action on an empty list");
        }
    }
}
