package com.genome.correction;

import com.genome.graph.DeBruijnGraph;
import com.genome.graph.TreeNode;
import com.genome.graph.Vertex;

import java.util.*;

/**
 * Detects and resolves bubbles in de Bruijn graphs.
 * 
 * Bubbles are alternative paths between two vertices caused by sequencing errors.
 * They occur when:
 * - A vertex has multiple outgoing edges (branching)
 * - Two paths from that vertex converge at another vertex (merging)
 * - The paths are short (within threshold length)
 * - The paths are disjoint (don't share internal vertices)
 * 
 * Resolution strategy:
 * - Calculate average k-mer coverage for each path
 * - Remove the path with lower coverage (likely contains errors)
 */
public class BubbleDetector {
    
    private final DeBruijnGraph graph;
    private final int pathLengthThreshold;
    private int bubblesDetected;
    private int bubblesResolved;
    
    /**
     * Creates a bubble detector for the given graph.
     * 
     * @param graph The de Bruijn graph to analyze
     * @param pathLengthThreshold Maximum path length for bubbles (typically k-mer size + 1)
     */
    public BubbleDetector(DeBruijnGraph graph, int pathLengthThreshold) {
        this.graph = graph;
        this.pathLengthThreshold = pathLengthThreshold;
        this.bubblesDetected = 0;
        this.bubblesResolved = 0;
    }
    
    /**
     * Detects and resolves all bubbles in the graph.
     * 
     * @return Number of bubbles resolved
     */
    public int detectAndResolveBubbles() {
        bubblesDetected = 0;
        bubblesResolved = 0;
        Vertex[] vertices = graph.getVertices();
        
        // Find all branching vertices
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].isRemoved() || vertices[i].getOutDegree() < 2) {
                continue;
            }
            
            // Explore paths from this branching vertex
            exploreBubblesFrom(vertices, i);
        }
        
        return bubblesResolved;
    }
    
    /**
     * Explores potential bubbles starting from a branching vertex.
     */
    private void exploreBubblesFrom(Vertex[] vertices, int branchingVertexId) {
        // Reset graph state for exploration
        for (Vertex vertex : vertices) {
            vertex.setFound(false);
            vertex.setTempNode(null);
        }
        
        // Build exploration tree
        TreeNode root = new TreeNode(branchingVertexId);
        Set<Integer> visited = new HashSet<>();
        
        exploreForBubbles(vertices, root, visited);
    }
    
    /**
     * Recursively explores paths from a vertex to find bubbles.
     */
    private void exploreForBubbles(Vertex[] vertices, TreeNode node, Set<Integer> visited) {
        int vertexId = node.getVertexId();
        visited.add(vertexId);
        Vertex vertex = vertices[vertexId];
        
        // Check if we've seen this vertex before (potential bubble!)
        if (vertex.isFound()) {
            bubblesDetected++;
            
            // Find common ancestor (bubble start)
            TreeNode commonAncestor = findCommonAncestor(vertices, node);
            
            if (commonAncestor != null) {
                // Resolve bubble by removing lower-coverage path
                resolveBubble(vertices, node, commonAncestor, visited);
            }
            
            if (!visited.contains(vertexId)) {
                return;
            }
        }
        
        // Mark vertex as found
        vertex.setFound(true);
        vertex.setTempNode(node);
        
        // Stop if we've reached maximum depth
        if (visited.size() >= pathLengthThreshold + 1) {
            visited.remove(vertexId);
            return;
        }
        
        // Explore all outgoing edges
        List<Integer> outgoingEdges = new ArrayList<>(vertex.getOutgoingEdges());
        for (int nextVertexId : outgoingEdges) {
            if (visited.contains(nextVertexId)) {
                continue;
            }
            
            TreeNode child = new TreeNode(nextVertexId);
            node.addChild(child);
            
            exploreForBubbles(vertices, child, visited);
            
            // Early termination if vertex was removed
            if (!visited.contains(vertexId)) {
                return;
            }
        }
        
        visited.remove(vertexId);
    }
    
    /**
     * Finds the common ancestor of two nodes (bubble starting point).
     */
    private TreeNode findCommonAncestor(Vertex[] vertices, TreeNode node) {
        TreeNode firstPath = vertices[node.getVertexId()].getTempNode();
        
        // Get all ancestors of first path
        List<TreeNode> firstPathAncestors = new ArrayList<>();
        TreeNode current = firstPath;
        while (current != null) {
            firstPathAncestors.add(current);
            current = current.getParent();
        }
        
        // Find first common ancestor with second path
        current = node;
        while (current != null) {
            for (int i = firstPathAncestors.size() - 1; i >= 0; i--) {
                if (current == firstPathAncestors.get(i)) {
                    return current;
                }
            }
            current = current.getParent();
        }
        
        return null;
    }
    
    /**
     * Resolves a bubble by removing the path with lower coverage.
     */
    private void resolveBubble(Vertex[] vertices, TreeNode convergenceNode, 
                              TreeNode commonAncestor, Set<Integer> visited) {
        // Get the two paths
        TreeNode path1 = convergenceNode;
        TreeNode path2 = vertices[convergenceNode.getVertexId()].getTempNode();
        
        // Calculate average coverage for each path
        double coverage1 = calculatePathCoverage(vertices, path1, commonAncestor);
        double coverage2 = calculatePathCoverage(vertices, path2, commonAncestor);
        
        // Remove the path with lower coverage
        if (coverage1 <= coverage2) {
            List<Integer> removedVertices = new ArrayList<>();
            TreeNode removedPath = removePath(vertices, path1, commonAncestor, removedVertices);
            resetTreeNodeFlags(vertices, removedPath);
            
            // Update the found status
            vertices[convergenceNode.getVertexId()].setFound(true);
            vertices[convergenceNode.getVertexId()].setTempNode(path2);
            
            // Remove from visited set
            for (int vertexId : removedVertices) {
                visited.remove(vertexId);
            }
            
            // Remove from parent's children
            commonAncestor.removeChild(removedPath);
            bubblesResolved++;
        } else {
            List<Integer> removedVertices = new ArrayList<>();
            TreeNode removedPath = removePath(vertices, path2, commonAncestor, removedVertices);
            resetTreeNodeFlags(vertices, removedPath);
            
            // Update the found status
            vertices[convergenceNode.getVertexId()].setFound(true);
            vertices[convergenceNode.getVertexId()].setTempNode(path1);
            
            // Remove from parent's children
            commonAncestor.removeChild(removedPath);
            bubblesResolved++;
        }
    }
    
    /**
     * Calculates the average k-mer coverage along a path.
     */
    private double calculatePathCoverage(Vertex[] vertices, TreeNode pathEnd, TreeNode pathStart) {
        double totalCoverage = 0;
        int edgeCount = 0;
        
        TreeNode current = pathEnd;
        while (current != pathStart) {
            String currentSeq = vertices[current.getVertexId()].getSequence();
            String parentSeq = vertices[current.getParent().getVertexId()].getSequence();
            
            // Reconstruct k-mer from parent -> current
            String kmer = parentSeq + currentSeq.charAt(currentSeq.length() - 1);
            
            totalCoverage += graph.getKmerCoverage(kmer);
            edgeCount++;
            
            current = current.getParent();
        }
        
        return edgeCount > 0 ? totalCoverage / edgeCount : 0;
    }
    
    /**
     * Removes a path from the graph.
     */
    private TreeNode removePath(Vertex[] vertices, TreeNode pathEnd, 
                                TreeNode pathStart, List<Integer> removedVertices) {
        TreeNode parent = pathEnd.getParent();
        TreeNode child = pathEnd;
        TreeNode lastRemoved = null;
        
        while (child != pathStart) {
            // Remove edge from parent to child
            vertices[parent.getVertexId()].removeOutgoingEdge(child.getVertexId());
            
            removedVertices.add(child.getVertexId());
            lastRemoved = child;
            
            // Move up the tree
            child = parent;
            parent = parent.getParent();
        }
        
        return lastRemoved;
    }
    
    /**
     * Resets the found flags for all nodes in a subtree.
     */
    private void resetTreeNodeFlags(Vertex[] vertices, TreeNode node) {
        if (node == null) return;
        
        vertices[node.getVertexId()].setFound(false);
        vertices[node.getVertexId()].setTempNode(null);
        
        for (TreeNode child : node.getChildren()) {
            resetTreeNodeFlags(vertices, child);
        }
    }
    
    /**
     * Gets the number of bubbles detected.
     */
    public int getBubblesDetected() {
        return bubblesDetected;
    }
    
    /**
     * Gets the number of bubbles resolved.
     */
    public int getBubblesResolved() {
        return bubblesResolved;
    }
    
    /**
     * Gets statistics about bubble detection.
     */
    public BubbleStats getStats() {
        return new BubbleStats(bubblesDetected, bubblesResolved);
    }
    
    /**
     * Statistics container for bubble detection.
     */
    public static class BubbleStats {
        public final int bubblesDetected;
        public final int bubblesResolved;
        
        public BubbleStats(int detected, int resolved) {
            this.bubblesDetected = detected;
            this.bubblesResolved = resolved;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Bubble Detection Statistics:\n" +
                "  Bubbles detected: %d\n" +
                "  Bubbles resolved: %d",
                bubblesDetected, bubblesResolved
            );
        }
    }
}
