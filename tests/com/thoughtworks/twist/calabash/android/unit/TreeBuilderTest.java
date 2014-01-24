package com.thoughtworks.twist.calabash.android.unit;

import com.thoughtworks.twist.calabash.android.*;
import org.codehaus.jackson.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.List;

import static com.thoughtworks.twist.calabash.android.TestUtils.readFileFromResources;
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
        final TreeNode root = mock(TreeNode.class);
        final TreeNode firstLevel = mock(TreeNode.class);

        when(httpClient.getViewDump()).thenReturn(dump);
        when(treeNodeBuilder.buildFrom(any(JsonNode.class))).thenReturn(root).thenReturn(firstLevel);
        final TreeBuilder treeBuilder = new TreeBuilder(wrapper, httpClient, treeNodeBuilder);

        List<TreeNode> tree = treeBuilder.createTree();

        assertEquals(1, tree.size());
        verify(root).appendChild(firstLevel);
    }

    @Test
    public void shouldGetDumpInfoForNestedChildren() throws Exception {
        final CalabashWrapper wrapper = mock(CalabashWrapper.class);
        final CalabashHttpClient httpClient = mock(CalabashHttpClient.class);
        final String dump = readFileFromResources("nested-view-dump.json");
        final TreeNode root = mock(TreeNode.class);
        final TreeNode firstLevelChild1 = mock(TreeNode.class);
        final TreeNode firstLevelChild2 = mock(TreeNode.class);

        when(httpClient.getViewDump()).thenReturn(dump);
        when(treeNodeBuilder.buildFrom(any(JsonNode.class))).thenReturn(root).thenReturn(firstLevelChild1).thenReturn(firstLevelChild2);
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
    public void shouldEmptyTreeNodeForEmptyDump() throws Exception {
        final CalabashWrapper wrapper = mock(CalabashWrapper.class);
        final CalabashHttpClient httpClient = mock(CalabashHttpClient.class);
        when(httpClient.getViewDump()).thenReturn("{}");
        final TreeBuilder treeBuilder = new TreeBuilder(wrapper, httpClient, treeNodeBuilder);

        List<TreeNode> tree = treeBuilder.createTree();

        assertEquals(0, tree.size());
    }
}
