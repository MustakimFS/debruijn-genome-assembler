package com.genome.graph;

import java.util.*;

/**
 * De Bruijn Graph for genome assembly.
 * 
 * A de Bruijn graph is constructed from k-mers extracted from sequencing reads.
 * - Vertices represent (k-1)-mers
 * - Edges represent k-mers (overlaps between vertices)
 * 
 * For genome assembly:
 * 1. Extract all k-mers from reads
 * 2. Build graph from k-mers
 * 3. Find Eulerian cycle (visits each edge exactly once)
 * 4. Reconstruct genome from the cycle
 */
public class DeBruijnGraph {
    
    private final int kmerSize;
    private final Vertex[] vertices;
    private final Map<String, Integer> sequenceToVertexId;
    private final Map<String, Integer> kmerCoverage;
    
    /**
     * Creates a de Bruijn graph from sequencing reads.
     * 
     * @param reads List of sequencing reads
     * @param kmerSize Size of k-mers to use (e.g., 20)
     */
    public DeBruijnGraph(List<String> reads, int kmerSize) {
        this.kmerSize = kmerSize;
        this.kmerCoverage = new HashMap<>();
        this.sequenceToVertexId = new HashMap<>();
        
        // Extract k-mers and build graph structure
        Map<String, List<Integer>> outgoingEdgesMap = new HashMap<>();
        Map<String, List<Integer>> incomingEdgesMap = new HashMap<>();
        
        buildGraphStructure(reads, outgoingEdgesMap, incomingEdgesMap);
        
        // Create vertex array
        this.vertices = createVertexArray(outgoingEdgesMap, incomingEdgesMap);
    }
    
    /**
     * Builds the graph structure by extracting k-mers from reads.
     */
    private void buildGraphStructure(List<String> reads, 
                                     Map<String, List<Integer>> outgoingEdgesMap,
                                     Map<String, List<Integer>> incomingEdgesMap) {
        Set<String> uniqueKmers = new HashSet<>();
        int vertexId = 0;
        
        for (String read : reads) {
            // Extract all k-mers from this read
            for (int i = 0; i <= read.length() - kmerSize; i++) {
                String kmer = read.substring(i, i + kmerSize);
                
                // Track k-mer coverage (for bubble resolution)
                kmerCoverage.put(kmer, kmerCoverage.getOrDefault(kmer, 0) + 1);
                
                // Skip if we've already processed this k-mer
                if (uniqueKmers.contains(kmer)) {
                    continue;
                }
                uniqueKmers.add(kmer);
                
                // Extract prefix and suffix
                String prefix = kmer.substring(0, kmerSize - 1);      // First k-1 characters
                String suffix = kmer.substring(1);                     // Last k-1 characters
                
                // Create vertices for prefix and suffix if they don't exist
                if (!sequenceToVertexId.containsKey(prefix)) {
                    sequenceToVertexId.put(prefix, vertexId);
                    outgoingEdgesMap.put(prefix, new ArrayList<>());
                    incomingEdgesMap.put(prefix, new ArrayList<>());
                    vertexId++;
                }
                
                if (!sequenceToVertexId.containsKey(suffix)) {
                    sequenceToVertexId.put(suffix, vertexId);
                    outgoingEdgesMap.put(suffix, new ArrayList<>());
                    incomingEdgesMap.put(suffix, new ArrayList<>());
                    vertexId++;
                }
                
                // Add edge from prefix to suffix (if they overlap correctly)
                if (hasOverlap(prefix, suffix)) {
                    int prefixId = sequenceToVertexId.get(prefix);
                    int suffixId = sequenceToVertexId.get(suffix);
                    outgoingEdgesMap.get(prefix).add(suffixId);
                    incomingEdgesMap.get(suffix).add(prefixId);
                }
            }
        }
    }
    
    /**
     * Checks if two sequences overlap correctly for de Bruijn graph.
     * For prefix "ACG" and suffix "CGT", they overlap if prefix[1:] == suffix[:-1]
     */
    private boolean hasOverlap(String prefix, String suffix) {
        for (int i = 1; i < prefix.length(); i++) {
            if (prefix.charAt(i) != suffix.charAt(i - 1)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Creates the vertex array from the edge maps.
     */
    private Vertex[] createVertexArray(Map<String, List<Integer>> outgoingEdgesMap,
                                       Map<String, List<Integer>> incomingEdgesMap) {
        Vertex[] vertexArray = new Vertex[sequenceToVertexId.size()];
        
        for (Map.Entry<String, Integer> entry : sequenceToVertexId.entrySet()) {
            String sequence = entry.getKey();
            int id = entry.getValue();
            
            List<Integer> outgoing = outgoingEdgesMap.get(sequence);
            List<Integer> incoming = incomingEdgesMap.get(sequence);
            
            vertexArray[id] = new Vertex(id, sequence, outgoing, incoming);
        }
        
        return vertexArray;
    }
    
    // Getters
    public int getKmerSize() { return kmerSize; }
    public Vertex[] getVertices() { return vertices; }
    public int getVertexCount() { return vertices.length; }
    
    /**
     * Gets the coverage (frequency) of a k-mer.
     */
    public int getKmerCoverage(String kmer) {
        return kmerCoverage.getOrDefault(kmer, 0);
    }
    
    /**
     * Checks if the graph has an Eulerian cycle.
     * An Eulerian cycle exists if and only if every vertex is balanced
     * (in-degree equals out-degree).
     */
    public boolean hasEulerianCycle() {
        for (Vertex vertex : vertices) {
            if (!vertex.isBalanced()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets statistics about the graph.
     */
    public GraphStats getStats() {
        int totalEdges = 0;
        int balancedVertices = 0;
        int sources = 0;
        int sinks = 0;
        int branchingVertices = 0;
        
        for (Vertex vertex : vertices) {
            if (!vertex.isRemoved()) {
                totalEdges += vertex.getOutDegree();
                if (vertex.isBalanced()) balancedVertices++;
                if (vertex.isSource()) sources++;
                if (vertex.isSink()) sinks++;
                if (vertex.isBranchingOut() || vertex.isBranchingIn()) branchingVertices++;
            }
        }
        
        return new GraphStats(vertices.length, totalEdges, balancedVertices, 
                            sources, sinks, branchingVertices);
    }
    
    /**
     * Graph statistics container.
     */
    public static class GraphStats {
        public final int vertices;
        public final int edges;
        public final int balancedVertices;
        public final int sources;
        public final int sinks;
        public final int branchingVertices;
        
        public GraphStats(int vertices, int edges, int balancedVertices,
                         int sources, int sinks, int branchingVertices) {
            this.vertices = vertices;
            this.edges = edges;
            this.balancedVertices = balancedVertices;
            this.sources = sources;
            this.sinks = sinks;
            this.branchingVertices = branchingVertices;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Graph Statistics:\n" +
                "  Vertices: %d\n" +
                "  Edges: %d\n" +
                "  Balanced vertices: %d\n" +
                "  Sources: %d\n" +
                "  Sinks: %d\n" +
                "  Branching vertices: %d",
                vertices, edges, balancedVertices, sources, sinks, branchingVertices
            );
        }
    }
    
    @Override
    public String toString() {
        return String.format("DeBruijnGraph[k=%d, vertices=%d, edges=%d]",
            kmerSize, vertices.length, getStats().edges);
    }
}
