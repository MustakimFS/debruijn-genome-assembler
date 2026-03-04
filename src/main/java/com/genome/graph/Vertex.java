package com.genome.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a vertex (node) in the de Bruijn graph.
 * Each vertex corresponds to a (k-1)-mer sequence.
 * 
 * In genome assembly, vertices are created from k-mers:
 * - For k-mer "ACGT", we create vertices for prefix "ACG" and suffix "CGT"
 * - Edges connect overlapping k-mers
 */
public class Vertex {
    
    // Unique identifier for this vertex
    private final int id;
    
    // The (k-1)-mer sequence this vertex represents
    private final String sequence;
    
    // Outgoing edges: vertices reachable from this vertex
    private final List<Integer> outgoingEdges;
    
    // Incoming edges: vertices that can reach this vertex
    private final List<Integer> incomingEdges;
    
    // Edge list for Eulerian cycle finding
    private final List<Integer> edgeIndices;
    
    // Flags for graph algorithms
    private boolean removed;      // Marked for removal (tip removal, bubble detection)
    private boolean visited;      // DFS/BFS visited flag
    private boolean found;        // Bubble detection flag
    
    // Temporary node reference for tree-based bubble detection
    private TreeNode tempNode;
    
    /**
     * Creates a new vertex in the de Bruijn graph.
     * 
     * @param id Unique identifier for this vertex
     * @param sequence The (k-1)-mer sequence this vertex represents
     * @param outgoingEdges List of vertex IDs reachable from this vertex
     * @param incomingEdges List of vertex IDs that can reach this vertex
     */
    public Vertex(int id, String sequence, List<Integer> outgoingEdges, List<Integer> incomingEdges) {
        this.id = id;
        this.sequence = sequence;
        this.outgoingEdges = new ArrayList<>(outgoingEdges);
        this.incomingEdges = new ArrayList<>(incomingEdges);
        this.edgeIndices = new ArrayList<>();
        this.removed = false;
        this.visited = false;
        this.found = false;
        this.tempNode = null;
    }
    
    // Getters
    public int getId() { return id; }
    public String getSequence() { return sequence; }
    public List<Integer> getOutgoingEdges() { return outgoingEdges; }
    public List<Integer> getIncomingEdges() { return incomingEdges; }
    public List<Integer> getEdgeIndices() { return edgeIndices; }
    
    // State getters/setters
    public boolean isRemoved() { return removed; }
    public void setRemoved(boolean removed) { this.removed = removed; }
    
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    
    public boolean isFound() { return found; }
    public void setFound(boolean found) { this.found = found; }
    
    public TreeNode getTempNode() { return tempNode; }
    public void setTempNode(TreeNode tempNode) { this.tempNode = tempNode; }
    
    // Degree calculations
    public int getOutDegree() { return outgoingEdges.size(); }
    public int getInDegree() { return incomingEdges.size(); }
    
    /**
     * Checks if this vertex is balanced (in-degree equals out-degree).
     * Balanced vertices are required for Eulerian cycles.
     */
    public boolean isBalanced() {
        return getInDegree() == getOutDegree();
    }
    
    /**
     * Checks if this vertex is a source (no incoming edges).
     * Sources are candidates for tip removal.
     */
    public boolean isSource() {
        return incomingEdges.isEmpty();
    }
    
    /**
     * Checks if this vertex is a sink (no outgoing edges).
     * Sinks are candidates for tip removal.
     */
    public boolean isSink() {
        return outgoingEdges.isEmpty();
    }
    
    /**
     * Checks if this vertex has multiple outgoing edges (branching point).
     * Branching vertices are starting points for bubble detection.
     */
    public boolean isBranchingOut() {
        return outgoingEdges.size() > 1;
    }
    
    /**
     * Checks if this vertex has multiple incoming edges (merging point).
     * Merging vertices are ending points for bubble detection.
     */
    public boolean isBranchingIn() {
        return incomingEdges.size() > 1;
    }
    
    /**
     * Removes an outgoing edge to the specified vertex.
     */
    public void removeOutgoingEdge(int targetVertexId) {
        outgoingEdges.remove(Integer.valueOf(targetVertexId));
    }
    
    /**
     * Removes an incoming edge from the specified vertex.
     */
    public void removeIncomingEdge(int sourceVertexId) {
        incomingEdges.remove(Integer.valueOf(sourceVertexId));
    }
    
    /**
     * Removes all outgoing edges.
     */
    public void clearOutgoingEdges() {
        outgoingEdges.clear();
    }
    
    /**
     * Removes all incoming edges.
     */
    public void clearIncomingEdges() {
        incomingEdges.clear();
    }
    
    @Override
    public String toString() {
        return String.format("Vertex[id=%d, seq=%s, in=%d, out=%d]", 
            id, sequence, getInDegree(), getOutDegree());
    }
}
