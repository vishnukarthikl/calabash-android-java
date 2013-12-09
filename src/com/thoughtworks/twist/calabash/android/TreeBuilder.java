package com.thoughtworks.twist.calabash.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;

import java.util.ArrayList;
import java.util.List;

public class TreeBuilder {

    private final AndroidCalabashWrapper calabashWrapper;
    private List<TreeNode> roots = new ArrayList<TreeNode>();

    public TreeBuilder(AndroidCalabashWrapper calabashWrapper) {
        this.calabashWrapper = calabashWrapper;
    }

    public List<TreeNode> createTree(String baseQuery) throws CalabashException {
        RubyArray allElements = calabashWrapper.query(baseQuery);
        for (int i = 0; i < allElements.size(); i++) {
            String query = String.format("* index:%d", i);
            RubyArray rubyElements = calabashWrapper.query(query);
            RubyHash rubyElement = (RubyHash) rubyElements.get(0);
            UIElement currentElement = new UIElement(rubyElement, query, calabashWrapper);
            List<UIElement> uiElements = new ArrayList<UIElement>();
            uiElements.add(currentElement);
            uiElements.addAll(getAncestors(query));
            merge(uiElements);
        }
        return getRoots();
    }

    private List<UIElement> getAncestors(String query) throws CalabashException {
        query = query + " parent *";
        RubyArray ancestors = calabashWrapper.query(query);
        List<UIElement> elements = new ArrayList<UIElement>();
        for (int i = 0; i < ancestors.size(); i++) {
            elements.add(new UIElement((RubyHash) ancestors.get(i), query + " index:" + i, calabashWrapper));
        }
        return elements;
    }

    public void merge(List<UIElement> elements) {
        sort(elements);
        if (isEmpty()) {
            TreeNode root = new TreeNode(null);
            append(root, elements);
            roots.add(root);
        } else {
            for (TreeNode root : roots) {
                if (tryMerge(root, elements))
                    return;
            }

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
                TreeNode newNode = new TreeNode(null);
                append(newNode, elements.subList(i, elements.size()));
                current.addChild(newNode);
                return true;
            }
            current = current.getChildren().get(matchedChildIndex);
        }
        return true;
    }

    private void append(TreeNode startNode, List<UIElement> elements) {
        TreeNode current = startNode;
        for (int i = 0; i < elements.size(); i++) {
            current.setData(elements.get(i));
            if (i + 1 < elements.size()) {
                TreeNode childNode = new TreeNode(elements.get(i + 1));
                current.addChild(childNode);
                current = childNode;
            }
        }
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
}
