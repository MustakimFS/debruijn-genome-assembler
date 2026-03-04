package com.genome.assembly;

import com.genome.graph.DeBruijnGraph;
import com.genome.graph.Edge;
import com.genome.graph.Vertex;

import java.util.*;

/**
 * Finds Eulerian cycles in de Bruijn graphs using Hierholzer's algorithm.
 * 
 * An Eulerian cycle is a path that visits every edge exactly once and returns
 * to the starting vertex. For genome assembly, the Eulerian cycle corresponds
 * to the genome sequence.
 * 
 * Algorithm:
 * 1. Start from any vertex with edges
 * 2. Follow edges until returning to start (forms a cycle)
 * 3. If unvisited edges remain, find vertex in cycle with unvisited edges
 * 4. Repeat from that vertex, merging new cycle with existing path
 * 5. Continue until all edges are visited
 */
public class EulerianCycleFinder {
    
    private final DeBruijnGraph graph;
    private final List<Edge> edges;
    
    /**
     * Creates an Eulerian cycle finder for the given graph.
     */
    public EulerianCycleFinder(DeBruijnGraph graph) {
        this.graph = graph;
        this.edges = new ArrayList<>();
    }
    
    /**
     * Finds an Eulerian cycle in the graph.
     * 
     * @return List of vertex IDs forming the cycle, or null if no cycle exists
     */
    public List<Integer> findCycle() {
        if (!graph.hasEulerianCycle()) {
            return null;
        }
        
        // Build edge list from graph
        buildEdgeList();
        
        // Find all cycles starting from each unvisited vertex
        List<Integer> completeCycle = new ArrayList<>();
        Vertex[] vertices = graph.getVertices();
        
        for (int i = 0; i < vertices.length; i++) {
            if (!vertices[i].isRemoved() && !vertices[i].isVisited()) {
                List<Integer> cycle = new ArrayList<>();
                exploreCycle(vertices, i, cycle);
                
                // Merge with complete cycle
                if (completeCycle.isEmpty()) {
                    completeCycle = cycle;
                } else {
                    // This handles disconnected components
                    completeCycle.addAll(cycle);
                }
            }
        }
        
        return completeCycle;
    }
    
    /**
     * Finds multiple Eulerian paths (for disconnected graphs).
     * Useful when graph has multiple connected components.
     */
    public List<List<Integer>> findAllCycles() {
        buildEdgeList();

        List<List<Integer>> allCycles = new ArrayList<>();

        Vertex[] vertices = graph.getVertices();

        // Find first non-removed vertex
        int start = -1;
        for (int i = 0; i < vertices.length; i++) {
            if (!vertices[i].isRemoved()) {
                start = i;
                break;
            }
        }

        if (start == -1) return allCycles;

        List<Integer> fullCycle = new ArrayList<>();
        exploreCycle(vertices, start, fullCycle);

        allCycles.add(fullCycle);
        return allCycles;
    }
    
    /**
     * Builds the edge list from the graph vertices.
     */
    private void buildEdgeList() {
        edges.clear();
        Vertex[] vertices = graph.getVertices();
        
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].isRemoved()) {
                continue;
            }
            
            List<Integer> outgoing = vertices[i].getOutgoingEdges();
            for (int targetId : outgoing) {
                Edge edge = new Edge(i, targetId);
                vertices[i].getEdgeIndices().add(edges.size());
                edges.add(edge);
            }
        }
    }
    
    /**
     * Explores and builds an Eulerian cycle starting from a vertex.
     * Uses iterative DFS with explicit stack to avoid stack overflow.
     */
    private void exploreCycle(Vertex[] vertices, int startVertex, List<Integer> cycle) {
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        stack.push(startVertex);
        
        while (!stack.isEmpty()) {
            int currentVertex = stack.peek();
            Vertex vertex = vertices[currentVertex];
            
            List<Integer> edgeIndices = vertex.getEdgeIndices();
            boolean foundUnusedEdge = false;
            
            for (int edgeIndex : edgeIndices) {
                Edge edge = edges.get(edgeIndex);
                
                if (!edge.isUsed()) {
                    edge.markUsed();
                    stack.push(edge.getTo());
                    foundUnusedEdge = true;
                    break;
                }
            }
            
            if (!foundUnusedEdge) {
                // All edges from this vertex are used
                cycle.add(stack.pop());
            }
        }
    }
    
    /**
     * Assembles a genome sequence from an Eulerian cycle.
     * 
     * The cycle is a sequence of vertices (k-1)-mers.
     * To get the genome, we take the first vertex's full sequence,
     * then append the last character of each subsequent vertex.
     */
    public String assembleGenome(List<Integer> cycle) {
        if (cycle == null || cycle.isEmpty()) {
            return "";
        }
        
        Vertex[] vertices = graph.getVertices();
        
        // Start with the last vertex's sequence (cycle is in reverse)
        StringBuilder genome = new StringBuilder(vertices[cycle.get(cycle.size() - 1)].getSequence());
        
        // Append last character of each vertex (going backwards through cycle)
        for (int i = cycle.size() - 2; i >= 0; i--) {
            String sequence = vertices[cycle.get(i)].getSequence();
            genome.append(sequence.charAt(sequence.length() - 1));
        }
        
        return genome.toString();
    }
    
    /**
     * Assembles genome from multiple cycles.
     * Returns the longest assembled sequence, or tries to find the correct one
     * based on expected genome length.
     */
    public String assembleBestGenome(List<List<Integer>> cycles, int expectedLength) {
        String bestGenome = "";
        int bestScore = Integer.MAX_VALUE;
        
        for (List<Integer> cycle : cycles) {
            String genome = assembleGenome(cycle);
            
            // Prefer genomes close to expected length
            int score = Math.abs(genome.length() - expectedLength);
            
            if (score < bestScore || 
                (score == bestScore && genome.length() > bestGenome.length())) {
                bestGenome = genome;
                bestScore = score;
            }
        }
        
        return bestGenome;
    }
    
    /**
     * Result container for Eulerian cycle finding.
     */
    public static class CycleResult {
        public final List<Integer> cycle;
        public final String genome;
        public final int length;
        
        public CycleResult(List<Integer> cycle, String genome) {
            this.cycle = cycle;
            this.genome = genome;
            this.length = genome.length();
        }
        
        @Override
        public String toString() {
            return String.format("CycleResult[vertices=%d, genome_length=%d]",
                cycle != null ? cycle.size() : 0, length);
        }
    }
}
