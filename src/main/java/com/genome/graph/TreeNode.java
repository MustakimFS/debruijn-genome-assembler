package com.genome.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree node used in bubble detection algorithm.
 * 
 * When exploring paths from a branching vertex, we build a tree structure
 * where each node represents a vertex visited during the exploration.
 * This allows us to:
 * 1. Track the path taken to reach each vertex
 * 2. Find common ancestors when two paths converge
 * 3. Determine which path to remove based on coverage
 */
public class TreeNode {
    
    private final int vertexId;
    private TreeNode parent;
    private final List<TreeNode> children;
    
    /**
     * Creates a tree node for the given vertex.
     * 
     * @param vertexId The ID of the vertex this node represents
     */
    public TreeNode(int vertexId) {
        this.vertexId = vertexId;
        this.parent = null;
        this.children = new ArrayList<>();
    }
    
    public int getVertexId() { return vertexId; }
    public TreeNode getParent() { return parent; }
    public List<TreeNode> getChildren() { return children; }
    
    public void setParent(TreeNode parent) { this.parent = parent; }
    
    /**
     * Adds a child node to this node.
     */
    public void addChild(TreeNode child) {
        children.add(child);
        child.setParent(this);
    }
    
    /**
     * Removes a child node from this node.
     */
    public void removeChild(TreeNode child) {
        children.remove(child);
        if (child.getParent() == this) {
            child.setParent(null);
        }
    }
    
    /**
     * Checks if this node is the root (no parent).
     */
    public boolean isRoot() {
        return parent == null;
    }
    
    /**
     * Checks if this node is a leaf (no children).
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    /**
     * Gets the depth of this node (distance from root).
     */
    public int getDepth() {
        int depth = 0;
        TreeNode current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }
    
    /**
     * Gets all ancestors of this node (path from this node to root).
     */
    public List<TreeNode> getAncestors() {
        List<TreeNode> ancestors = new ArrayList<>();
        TreeNode current = this.parent;
        while (current != null) {
            ancestors.add(current);
            current = current.parent;
        }
        return ancestors;
    }
    
    @Override
    public String toString() {
        return String.format("TreeNode[vertex=%d, depth=%d, children=%d]", 
            vertexId, getDepth(), children.size());
    }
}
