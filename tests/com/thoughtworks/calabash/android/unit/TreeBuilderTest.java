package com.thoughtworks.calabash.android.unit;

import com.thoughtworks.calabash.android.*;
import org.codehaus.jackson.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.List;

import static com.thoughtworks.calabash.android.TestUtils.readFileFromResources;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TreeBuilderTest {

    @Mock
    private CalabashWrapper wrapper;
    @Mock
    private CalabashHttpClient httpClient;
    @Mock
    private TreeNodeBuilder treeNodeBuilder;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldGetDumpInfo() throws Exception {
        String dump = readFileFromResources("simple-dump.json");
        final TreeNode root = getMockedTreeNodeWithElement();
        final TreeNode firstLevel = getMockedTreeNodeWithElement();

        when(httpClient.getViewDump()).thenReturn(dump);
        when(treeNodeBuilder.buildFrom(any(JsonNode.class), anyString())).thenReturn(root).thenReturn(firstLevel);
        final TreeBuilder treeBuilder = new TreeBuilder(wrapper, httpClient, treeNodeBuilder);

        List<TreeNode> tree = treeBuilder.createTree();

        assertEquals(1, tree.size());
        verify(root).appendChild(firstLevel);
    }

    @Test
    public void shouldGetDumpInfoForNestedChildren() throws Exception {
        final String dump = readFileFromResources("nested-view-dump.json");
        final TreeNode root = getMockedTreeNodeWithElement();
        final TreeNode firstLevelChild1 = getMockedTreeNodeWithElement();
        final TreeNode firstLevelChild2 = getMockedTreeNodeWithElement();

        when(httpClient.getViewDump()).thenReturn(dump);
        when(treeNodeBuilder.buildFrom(any(JsonNode.class), anyString())).thenReturn(root).thenReturn(firstLevelChild1).thenReturn(firstLevelChild2);
        final TreeBuilder treeBuilder = new TreeBuilder(wrapper, httpClient, treeNodeBuilder);

        List<TreeNode> tree = treeBuilder.createTree();

        assertEquals(1, tree.size());
        ArgumentCaptor<TreeNode> treeNodeCaptor = ArgumentCaptor.forClass(TreeNode.class);
        verify(root, times(2)).appendChild(treeNodeCaptor.capture());
        List<TreeNode> capturedTreeNodes = treeNodeCaptor.getAllValues();
        assertEquals(firstLevelChild1, capturedTreeNodes.get(0));
        assertEquals(firstLevelChild2, capturedTreeNodes.get(1));

        verify(root).appendChild(firstLevelChild1);
    }

    @Test
    public void shouldAddOnlyVisibleNodes() throws Exception {
        final String dump = readFileFromResources("nested-invisible-view-dump.json");
        final TreeNode root = getMockedTreeNodeWithElement();
        final TreeNode visibleChild = mock(TreeNode.class);

        when(httpClient.getViewDump()).thenReturn(dump);
        when(treeNodeBuilder.buildFrom(any(JsonNode.class), anyString())).thenReturn(root).thenReturn(visibleChild);
        final TreeBuilder treeBuilder = new TreeBuilder(wrapper, httpClient, treeNodeBuilder);

        List<TreeNode> tree = treeBuilder.createTree();

        assertEquals(1, tree.size());
        ArgumentCaptor<TreeNode> treeNodeCaptor = ArgumentCaptor.forClass(TreeNode.class);
        verify(root, times(1)).appendChild(treeNodeCaptor.capture());
        List<TreeNode> capturedTreeNodes = treeNodeCaptor.getAllValues();
        assertEquals(visibleChild, capturedTreeNodes.get(0));

        verify(treeNodeBuilder, times(2)).buildFrom(any(JsonNode.class), anyString());

        verify(root).appendChild(visibleChild);
    }

    @Test
    public void shouldAddQueryToElements() throws Exception {
        final String dump = readFileFromResources("nested-view-dump.json");
        final TreeNode root = getMockedTreeNodeWithElementWithQuery("* index:0");
        final TreeNode firstLevelChild1 = getMockedTreeNodeWithElement();
        final TreeNode firstLevelChild2 = getMockedTreeNodeWithElement();

        when(httpClient.getViewDump()).thenReturn(dump);
        when(treeNodeBuilder.buildFrom(any(JsonNode.class), anyString())).thenReturn(root).thenReturn(firstLevelChild1).thenReturn(firstLevelChild2);

        final TreeBuilder treeBuilder = new TreeBuilder(wrapper, httpClient, treeNodeBuilder);
        treeBuilder.createTree();

        final ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(treeNodeBuilder, times(3)).buildFrom(any(JsonNode.class), queryCaptor.capture());

        final List<String> queries = queryCaptor.getAllValues();
        assertEquals("* index:0", queries.get(0));
        assertEquals("* index:0 child * index:0", queries.get(1));
        assertEquals("* index:0 child * index:1", queries.get(2));

    }

    @Test
    public void shouldGenerateQueryOnlyForVisibleNodes() throws Exception {
        final String dump = readFileFromResources("nested-invisible-view-dump.json");
        final TreeNode root = getMockedTreeNodeWithElementWithQuery("* index:0");
        final TreeNode firstLevelChild1 = getMockedTreeNodeWithElement();
        final TreeNode firstLevelChild2 = getMockedTreeNodeWithElement();

        when(httpClient.getViewDump()).thenReturn(dump);
        when(treeNodeBuilder.buildFrom(any(JsonNode.class), anyString())).thenReturn(root).thenReturn(firstLevelChild1).thenReturn(firstLevelChild2);

        final TreeBuilder treeBuilder = new TreeBuilder(wrapper, httpClient, treeNodeBuilder);
        treeBuilder.createTree();

        final ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(treeNodeBuilder, times(2)).buildFrom(any(JsonNode.class), queryCaptor.capture());

        final List<String> queries = queryCaptor.getAllValues();
        assertEquals("* index:0", queries.get(0));
        assertEquals("* index:0 child * index:0", queries.get(1));
    }

    @Test
    public void shouldEmptyTreeNodeForEmptyDump() throws Exception {
        when(httpClient.getViewDump()).thenReturn("{}");
        final TreeBuilder treeBuilder = new TreeBuilder(wrapper, httpClient, treeNodeBuilder);

        List<TreeNode> tree = treeBuilder.createTree();

        assertEquals(0, tree.size());
    }

    private TreeNode getMockedTreeNodeWithElementWithQuery(String query) {
        final TreeNode mockedTreeNodeWithElement = getMockedTreeNodeWithElement();
        when(mockedTreeNodeWithElement.getData().getQuery()).thenReturn(query);
        return mockedTreeNodeWithElement;
    }

    private TreeNode getMockedTreeNodeWithElement() {
        final TreeNode root = mock(TreeNode.class);
        final UIElement rootElement = mock(UIElement.class);
        when(root.getData()).thenReturn(rootElement);
        return root;
    }
}
