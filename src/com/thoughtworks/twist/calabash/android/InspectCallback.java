/**
 * 
 */
package com.thoughtworks.twist.calabash.android;

/**
 * Provides callback while inspecting elements
 * 
 */
public interface InspectCallback {

	/**
	 * This function gets called for each UIElement
	 * 
	 * @param element
	 *            Current element
	 * @param nestingLevel
	 *            Nesting level. This indicates how deep the element in the tree
	 */
	void onEachElement(UIElement element, int nestingLevel);

}
