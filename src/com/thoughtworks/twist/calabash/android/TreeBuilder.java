package com.thoughtworks.twist.calabash.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeBuilder {

    public static final String QUERY_ALL = "*";
    private final AndroidCalabashWrapper calabashWrapper;
    private List<TreeNode> roots = new ArrayList<TreeNode>();
    private Set<UIElement> inspectedElements = new HashSet<UIElement>();

    public TreeBuilder(AndroidCalabashWrapper calabashWrapper) {
        this.calabashWrapper = calabashWrapper;
    }

    public List<TreeNode> createTreeFromRoot() throws CalabashException {
        RubyArray allElements = calabashWrapper.query(QUERY_ALL);
        return getTreeNodes(allElements, null, "*");
    }

    public TreeNode createTreeFrom(UIElement root) throws CalabashException {
        String elementQuery = root.getQuery();
        String descendantQuery = elementQuery + " descendant *";
        RubyArray descendants = calabashWrapper.query(descendantQuery);
        return getTreeNodes(descendants, root, descendantQuery).get(0);
    }

    private List<TreeNode> getTreeNodes(RubyArray allElements, UIElement root, String baseQuery) throws CalabashException {
        clearRoot();
        for (int i = allElements.size() - 1; i >= 0; i--) {
            final String query = String.format(baseQuery + " index:%d", i);
            RubyHash rubyElement = (RubyHash) allElements.get(i);
            UIElement currentElement = new UIElement(rubyElement, query, calabashWrapper);
            List<UIElement> uiElements = new ArrayList<UIElement>();
            if (inspectedElements.contains(currentElement))
                continue;

            uiElements.add(currentElement);
            uiElements.addAll(getAncestors(query, root));
            merge(uiElements);
            inspectedElements.addAll(uiElements);
        }
        return getRoots();
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

    public void merge(List<UIElement> elements) {
        sort(elements);
        if (isEmpty()) {
            TreeNode root = createBranch(elements);
            roots.add(root);
        } else {
            for (TreeNode root : roots) {
                if (tryMerge(root, elements))
                    return;
            }
            TreeNode newRoot = createBranch(elements);
            roots.add(newRoot);

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
        TreeNode startNode = new TreeNode(null);
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

    private boolean isEmpty() {
        return roots.size() == 0;
    }

    private void sort(List<UIElement> elements) {
        int length = elements.size();
        for (int i = 0; i < length / 2; i++) {
            int reverseIndex = length - (i + 1);
            UIElement ith = elements.get(i);
            UIElement ithFromReverse = elements.get(reverseIndex);
            elements.set(i, ithFromReverse);
            elements.set(reverseIndex, ith);
        }
    }

    public List<TreeNode> getRoots() {
        return roots;
    }

    private void clearRoot() {
        roots = new ArrayList<TreeNode>();
    }
}
