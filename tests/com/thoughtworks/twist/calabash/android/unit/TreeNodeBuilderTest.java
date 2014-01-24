package com.thoughtworks.twist.calabash.android.unit;

import com.thoughtworks.twist.calabash.android.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TreeNodeBuilderTest {
    @Test
    public void shouldCreateTreeNodeFromJsonNode() throws Exception {
        CalabashWrapper calabashWrapper = mock(CalabashWrapper.class);
        final String jsonString = TestUtils.readFileFromResources("simple-button.json");
        final JsonNode jsonNode = new ObjectMapper().readTree(jsonString).get(0);

        final TreeNodeBuilder treeNodeBuilder = new TreeNodeBuilder(calabashWrapper);
        final TreeNode actualTreeNode = treeNodeBuilder.buildFrom(jsonNode);

        final UIElement data = actualTreeNode.getData();
        final JsonNode rect = jsonNode.get("rect");
        final Rect expectedRect = new Rect(rect.get("x").getDoubleValue(),
                rect.get("y").getDoubleValue(),
                rect.get("width").getDoubleValue(),
                rect.get("height").getDoubleValue(),
                rect.get("center_x").getDoubleValue(),
                rect.get("center_y").getDoubleValue());


        assertEquals(jsonNode.get("id").getTextValue(), data.getId());
        assertEquals(jsonNode.get("type").getTextValue(), data.getElementClass());
        assertEquals(expectedRect, data.getRect());
        assertEquals(jsonNode.get("value").getTextValue(), data.getText());

    }

    @Test
    public void shouldAddQueryWhileCreatingNode() throws Exception {
        CalabashWrapper calabashWrapper = mock(CalabashWrapper.class);
        final TreeNodeBuilder treeNodeBuilder = new TreeNodeBuilder(calabashWrapper);
        final String jsonString = TestUtils.readFileFromResources("simple-button.json");
        final JsonNode jsonNode = new ObjectMapper().readTree(jsonString);

        final TreeNode treeNode = treeNodeBuilder.buildFrom(jsonNode.get(0));

        assertEquals("* index:0 child * index:1 child * index:0 child * index:4", treeNode.getData().getQuery());
    }

    @Test
    public void shouldAddQueryForRootNodeWhileCreatingNode() throws Exception {
        CalabashWrapper calabashWrapper = mock(CalabashWrapper.class);
        final TreeNodeBuilder treeNodeBuilder = new TreeNodeBuilder(calabashWrapper);
        final String jsonString = TestUtils.readFileFromResources("simple-button.json");
        final JsonNode jsonNode = new ObjectMapper().readTree(jsonString);

        final TreeNode treeNode = treeNodeBuilder.buildFrom(jsonNode.get(1));

        assertEquals("* index:0", treeNode.getData().getQuery());
    }
}
