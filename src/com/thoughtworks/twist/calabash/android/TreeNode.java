package com.thoughtworks.twist.calabash.android;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private UIElement data;
    private List<TreeNode> children;

    public TreeNode(UIElement data) {
        this.data = data;
        this.children = new ArrayList<TreeNode>();
    }

    public TreeNode() {
        this(null);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void addChild(TreeNode child) {
        children.add(0, child);
    }

    public UIElement getData() {
        return data;
    }

    public void setData(UIElement data) {
        this.data = data;
    }
}