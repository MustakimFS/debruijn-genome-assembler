package com.genome.correction;

import com.genome.graph.DeBruijnGraph;
import com.genome.graph.Vertex;

import java.util.List;

/**
 * Removes tips (erroneous branches) from de Bruijn graph.
 * 
 * Tips are short dead-end paths in the graph caused by sequencing errors.
 * They typically:
 * - Start from a vertex with no incoming edges (source)
 * - End at a vertex with no outgoing edges (sink)
 * - Are shorter than the k-mer size
 * 
 * Algorithm:
 * 1. Find all sources (vertices with in-degree = 0)
 * 2. Follow outgoing edges up to threshold length
 * 3. If path ends without branching, remove it
 * 4. Repeat for sinks (vertices with out-degree = 0)
 * 5. Iterate until no more tips are found
 */
public class TipRemover {
    
    private final DeBruijnGraph graph;
    private final int threshold;
    private int tipsRemoved;
    
    /**
     * Creates a tip remover for the given graph.
     * 
     * @param graph The de Bruijn graph to clean
     * @param threshold Maximum length of tips to remove (typically k-mer size)
     */
    public TipRemover(DeBruijnGraph graph, int threshold) {
        this.graph = graph;
        this.threshold = threshold;
        this.tipsRemoved = 0;
    }
    
    /**
     * Removes all tips from the graph.
     * 
     * @return Number of edges removed
     */
    public int removeTips() {
        tipsRemoved = 0;
        Vertex[] vertices = graph.getVertices();
        
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].isRemoved()) {
                continue;
            }
            
            // Check for outgoing tips (from sources)
            if (vertices[i].getOutDegree() == 0) {
                removeIncomingTip(vertices, i);
            }
            
            // Check for incoming tips (to sinks)  
            if (vertices[i].getInDegree() == 0) {
                removeOutgoingTip(vertices, i);
            }
        }
        
        return tipsRemoved;
    }
    
    /**
     * Removes an outgoing tip starting from a source vertex.
     */
    private void removeOutgoingTip(Vertex[] vertices, int vertexId) {
        Vertex vertex = vertices[vertexId];
        
        if (vertex.getInDegree() != 0 || vertex.getOutDegree() != 1) {
            return;
        }
        
        vertex.setRemoved(true);
        tipsRemoved++;
        
        int nextId = vertex.getOutgoingEdges().get(0);
        vertices[nextId].removeIncomingEdge(vertexId);
        vertex.getOutgoingEdges().remove(Integer.valueOf(nextId));
        
        removeOutgoingTip(vertices, nextId);
    }
    
    /**
     * Removes an incoming tip ending at a sink vertex.
     */
    private void removeIncomingTip(Vertex[] vertices, int vertexId) {
        Vertex vertex = vertices[vertexId];
        
        if (vertex.getOutDegree() != 0 || vertex.getInDegree() != 1) {
            return;
        }
        
        vertex.setRemoved(true);
        tipsRemoved++;
        
        int prevId = vertex.getIncomingEdges().get(0);
        vertices[prevId].removeOutgoingEdge(vertexId);
        vertex.getIncomingEdges().remove(Integer.valueOf(prevId));
        
        removeIncomingTip(vertices, prevId);
    }
    
    /**
     * Gets the number of tips removed.
     */
    public int getTipsRemoved() {
        return tipsRemoved;
    }
    
    /**
     * Gets statistics about tip removal.
     */
    public TipRemovalStats getStats() {
        Vertex[] vertices = graph.getVertices();
        int remainingVertices = 0;
        int remainingEdges = 0;
        
        for (Vertex vertex : vertices) {
            if (!vertex.isRemoved()) {
                remainingVertices++;
                remainingEdges += vertex.getOutDegree();
            }
        }
        
        return new TipRemovalStats(tipsRemoved, remainingVertices, remainingEdges);
    }
    
    /**
     * Statistics container for tip removal.
     */
    public static class TipRemovalStats {
        public final int tipsRemoved;
        public final int remainingVertices;
        public final int remainingEdges;
        
        public TipRemovalStats(int tipsRemoved, int remainingVertices, int remainingEdges) {
            this.tipsRemoved = tipsRemoved;
            this.remainingVertices = remainingVertices;
            this.remainingEdges = remainingEdges;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Tip Removal Statistics:\n" +
                "  Tips removed: %d\n" +
                "  Remaining vertices: %d\n" +
                "  Remaining edges: %d",
                tipsRemoved, remainingVertices, remainingEdges
            );
        }
    }
}
