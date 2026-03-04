package com.genome.graph;

/**
 * Represents a directed edge in the de Bruijn graph.
 * Each edge corresponds to a k-mer in the original reads.
 * 
 * For k-mer "ACGT":
 * - from: vertex for prefix "ACG"
 * - to: vertex for suffix "CGT"
 */
public class Edge {
    
    private final int from;
    private final int to;
    private boolean used;
    
    /**
     * Creates a directed edge from one vertex to another.
     * 
     * @param from Source vertex ID
     * @param to Target vertex ID
     */
    public Edge(int from, int to) {
        this.from = from;
        this.to = to;
        this.used = false;
    }
    
    public int getFrom() { return from; }
    public int getTo() { return to; }
    
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    
    /**
     * Marks this edge as used (for Eulerian cycle traversal).
     */
    public void markUsed() {
        this.used = true;
    }
    
    @Override
    public String toString() {
        return String.format("Edge[%d -> %d, used=%b]", from, to, used);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Edge)) return false;
        Edge other = (Edge) obj;
        return this.from == other.from && this.to == other.to;
    }
    
    @Override
    public int hashCode() {
        return 31 * from + to;
    }
}
