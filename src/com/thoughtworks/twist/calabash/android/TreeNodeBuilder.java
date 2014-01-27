package com.thoughtworks.twist.calabash.android;

import org.codehaus.jackson.JsonNode;

import java.util.HashMap;

public class TreeNodeBuilder {
    private final CalabashWrapper calabashWrapper;

    public TreeNodeBuilder(CalabashWrapper calabashWrapper) {
        this.calabashWrapper = calabashWrapper;
    }

    public TreeNode buildFrom(JsonNode jsonNode, String query) {
        final HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("class", getProperty(jsonNode, "type"));
        map.put("id", getProperty(jsonNode, "id"));
        map.put("text", getProperty(jsonNode, "value"));
        map.put("enabled", getBooleanProperty(jsonNode, "enabled"));
        createRect(jsonNode, map);

        return new TreeNode(new UIElement(map, query, calabashWrapper));
    }

    private void createRect(JsonNode jsonNode, HashMap<Object, Object> map) {
        final HashMap<String, String> rectMap = new HashMap<String, String>();
        final JsonNode rectNode = jsonNode.get("rect");
        if (rectNode == null) {
            return;
        }
        populateMap(rectMap, rectNode, "x");
        populateMap(rectMap, rectNode, "y");
        populateMap(rectMap, rectNode, "width");
        populateMap(rectMap, rectNode, "height");
        populateMap(rectMap, rectNode, "center_x");
        populateMap(rectMap, rectNode, "center_y");
        map.put("rect", rectMap);
    }

    private void populateMap(HashMap<String, String> rectMap, JsonNode node, String property) {
        rectMap.put(property, getPropertyAsDouble(node, property).toString());
    }

    private String getProperty(JsonNode jsonNode, String property) {
        final JsonNode propertyNode = jsonNode.get(property);
        return propertyNode == null ? "null" : propertyNode.getTextValue();
    }

    private Double getPropertyAsDouble(JsonNode jsonNode, String property) {
        final JsonNode propertyNode = jsonNode.get(property);
        return propertyNode == null ? null : propertyNode.getDoubleValue();
    }

    private boolean getBooleanProperty(JsonNode jsonNode, String property) {
        final JsonNode propertyNode = jsonNode.get(property);
        return propertyNode == null ? false : propertyNode.getBooleanValue();
    }
}
