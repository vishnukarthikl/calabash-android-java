package com.thoughtworks.twist.calabash.android;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jruby.RubyArray;
import org.jruby.RubyHash;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

import static com.thoughtworks.twist.calabash.android.CalabashLogger.error;
import static com.thoughtworks.twist.calabash.android.CalabashLogger.info;

public class TreeBuilder {

    private final CalabashWrapper calabashWrapper;
    private final CalabashHttpClient calabashHttpClient;
    private final TreeNodeBuilder treeNodeBuilder;
    private ObjectMapper mapper = new ObjectMapper();

    public TreeBuilder(CalabashWrapper calabashWrapper) {
        this.calabashWrapper = calabashWrapper;
        this.calabashHttpClient = new CalabashHttpClient();
        this.treeNodeBuilder = new TreeNodeBuilder(calabashWrapper);
    }

    public TreeBuilder(CalabashWrapper calabashWrapper, CalabashHttpClient calabashHttpClient, TreeNodeBuilder treeNodeBuilder) {
        this.calabashWrapper = calabashWrapper;
        this.calabashHttpClient = calabashHttpClient;
        this.treeNodeBuilder = treeNodeBuilder;
    }

    public TreeNode createTreeFrom(UIElement root) throws CalabashException {
        Set<UIElement> inspectedElements = new HashSet<UIElement>();
        String elementQuery = root.getQuery();
        String descendantQuery = elementQuery + " descendant *";
        RubyArray descendants = calabashWrapper.query(descendantQuery);
        return getTreeNodes(descendants, root, descendantQuery, inspectedElements).get(0);
    }

    private List<TreeNode> getTreeNodes(RubyArray allElements, UIElement root, String baseQuery, Set<UIElement> inspectedElements) throws CalabashException {
        List<TreeNode> roots = new ArrayList<TreeNode>();
        for (int i = allElements.size() - 1; i >= 0; i--) {
            final String query = String.format(baseQuery + " index:%d", i);
            RubyHash rubyElement = (RubyHash) allElements.get(i);
            UIElement currentElement = new UIElement(rubyElement, query, calabashWrapper);
            List<UIElement> uiElements = new ArrayList<UIElement>();
            if (inspectedElements.contains(currentElement))
                continue;

            uiElements.add(currentElement);
            uiElements.addAll(getAncestors(query, root));
            merge(uiElements, roots);
            inspectedElements.addAll(uiElements);
        }
        return roots;
    }

    private List<UIElement> getAncestors(String query, UIElement root) throws CalabashException {
        return root == null ? getAllAncestors(query) : getAncestorsWithRoot(query, root);
    }

    private List<UIElement> getAllAncestors(String query) throws CalabashException {
        String parentQuery = query + " parent *";
        RubyArray ancestors = calabashWrapper.query(parentQuery);
        return convertToList(ancestors, parentQuery);
    }

    private List<UIElement> getAncestorsWithRoot(String query, UIElement root) throws CalabashException {
        List<UIElement> ancestorElements = getAllAncestors(query);

        List<UIElement> finalElements = new ArrayList<UIElement>();
        if (!ancestorElements.contains(root)) {
            // when query's element is the root it won't be there in ancestor list
            return finalElements;
        }

        for (UIElement ancestor : ancestorElements) {
            finalElements.add(ancestor);
            if (ancestor.equals(root)) {
                return finalElements;
            }
        }
        return finalElements;
    }

    private List<UIElement> convertToList(RubyArray ancestors, String baseQuery) {
        List<UIElement> uiElements = new ArrayList<UIElement>();
        for (int i = 0; i < ancestors.size(); i++) {
            UIElement uiElement = new UIElement((RubyHash) ancestors.get(i), baseQuery + " index:" + i, calabashWrapper);
            uiElements.add(uiElement);
        }
        return uiElements;
    }

    public void merge(List<UIElement> elements, List<TreeNode> roots) {
        Collections.reverse(elements);
        if (roots.isEmpty()) {
            TreeNode root = createBranch(elements);
            roots.add(root);
        } else {
            for (TreeNode root : roots) {
                if (tryMerge(root, elements))
                    return;
            }
            TreeNode newRoot = createBranch(elements);
            roots.add(0, newRoot);

        }
    }

    private boolean tryMerge(TreeNode root, List<UIElement> elements) {
        TreeNode current = root;
        if (!current.getData().equals(elements.get(0))) {
            return false;
        }

        int i;
        int matchedChildIndex;
        for (i = 1; i < elements.size(); i++) {
            matchedChildIndex = matchWith(current.getChildren(), elements.get(i));
            if (matchedChildIndex == -1) {
                TreeNode newNode = createBranch(elements.subList(i, elements.size()));
                current.addChild(newNode);
                return true;
            }
            current = current.getChildren().get(matchedChildIndex);
        }
        return true;
    }

    private TreeNode createBranch(List<UIElement> elements) {
        TreeNode startNode = new TreeNode();
        TreeNode current = startNode;
        for (int i = 0; i < elements.size(); i++) {
            current.setData(elements.get(i));
            if (i + 1 < elements.size()) {
                TreeNode childNode = new TreeNode(elements.get(i + 1));
                current.addChild(childNode);
                current = childNode;
            }
        }
        return startNode;
    }

    private int matchWith(List<TreeNode> elements, UIElement elementToMatch) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).getData().equals(elementToMatch))
                return i;

        }
        return -1;
    }

    public List<TreeNode> createTree() {
        List<TreeNode> treeNodes = null;
        try {
            info("Fetching view hierarchy");
            treeNodes = new ArrayList<TreeNode>();
            final JsonNode jsonNode = mapper.readTree(calabashHttpClient.getViewDump());
            final JsonNode childNodes = jsonNode.get("children");
            if (childNodes == null) {
                return treeNodes;
            }
            Iterator<JsonNode> rootJsonNodes = childNodes.getElements();
            while (rootJsonNodes.hasNext()) {
                JsonNode rootJsonNode = rootJsonNodes.next();
                final TreeNode rootTreeNode = treeNodeBuilder.buildFrom(rootJsonNode);
                addChildren(rootTreeNode, rootJsonNode);

                treeNodes.add(rootTreeNode);
            }

        } catch (MalformedURLException e) {
            error("malformed url", e);
        } catch (IOException e) {
            error("exception while fetching view hierarchy", e);
        }
        info("Done fetching view hierarchy");
        return treeNodes;
    }

    private void addChildren(TreeNode treeNode, JsonNode jsonNode) throws IOException {
        final Iterator<JsonNode> children = jsonNode.get("children").getElements();
        while (children.hasNext()) {
            final JsonNode childJsonNode = children.next();
            if (childJsonNode.get("visible").getBooleanValue()) {
                final TreeNode childTreeNode = treeNodeBuilder.buildFrom(childJsonNode);
                addChildren(childTreeNode, childJsonNode);

                treeNode.appendChild(childTreeNode);
            }
        }

    }
}
