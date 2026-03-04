package com.genome.assembly;

import com.genome.correction.BubbleDetector;
import com.genome.correction.TipRemover;
import com.genome.graph.DeBruijnGraph;

import java.util.List;

/**
 * Main genome assembler that orchestrates the complete assembly pipeline.
 * <p>
 * Pipeline:
 * 1. Build de Bruijn graph from reads
 * 2. Remove tips (error correction)
 * 3. Detect and resolve bubbles (error correction)
 * 4. Remove tips again (cleanup after bubble removal)
 * 5. Find Eulerian cycle
 * 6. Assemble genome from cycle
 * <p>
 * This matches the pipeline from your errorProneAssembly.java
 */
public class GenomeAssembler {

    private final AssemblyConfig config;
    private AssemblyResult result;

    /**
     * Creates a genome assembler with default configuration.
     */
    public GenomeAssembler() {
        this(new AssemblyConfig());
    }

    /**
     * Creates a genome assembler with custom configuration.
     */
    public GenomeAssembler(AssemblyConfig config) {
        this.config = config;
    }

    /**
     * Assembles a genome from sequencing reads.
     *
     * @param reads List of sequencing reads
     * @return Assembled genome sequence
     */
    public String assemble(List<String> reads) {
        long startTime = System.currentTimeMillis();

        // Build de Bruijn graph
        DeBruijnGraph graph = new DeBruijnGraph(reads, config.kmerSize);

        // Error correction pipeline
        TipRemover tipRemover1 = new TipRemover(graph, config.kmerSize);
        int tipsRemoved1 = tipRemover1.removeTips();

        BubbleDetector bubbleDetector = new BubbleDetector(graph, config.kmerSize + 1);
        int bubblesResolved = bubbleDetector.detectAndResolveBubbles();

        TipRemover tipRemover2 = new TipRemover(graph, config.kmerSize);
        int tipsRemoved2 = tipRemover2.removeTips();

        // Find Eulerian cycle
        EulerianCycleFinder cycleFinder = new EulerianCycleFinder(graph);
        List<List<Integer>> allCycles = cycleFinder.findAllCycles();

        // Assemble genome
        String assembledGenome = cycleFinder.assembleBestGenome(allCycles, config.expectedGenomeLength);

        // Check the length before trimming (debug statement)
        // System.err.println("Before trimming length: " + assembledGenome.length());

        // Trim to expected length if specified
        if (config.trimToExpectedLength && assembledGenome.length() > config.expectedGenomeLength) {
            // Your code trims from position 14 to 5410 for phi X174
            // This is to remove circular overlap
            int start = config.trimStart;
            int end = config.trimEnd;

            if (end > 0 && end <= assembledGenome.length()) {
                assembledGenome = assembledGenome.substring(start, end);
            }
        }

        long endTime = System.currentTimeMillis();

        // Build result
        result = new AssemblyResult(
                assembledGenome,
                reads.size(),
                graph.getVertexCount(),
                graph.getStats().edges,
                tipsRemoved1 + tipsRemoved2,
                bubblesResolved,
                endTime - startTime
        );

        return assembledGenome;
    }

    /**
     * Gets the detailed assembly result.
     */
    public AssemblyResult getResult() {
        return result;
    }

    /**
     * Configuration for genome assembly.
     */
    public static class AssemblyConfig {
        public int kmerSize = 20;                    // K-mer size for de Bruijn graph
        public int expectedGenomeLength = 5396;       // Expected genome length (phi X174)
        public boolean trimToExpectedLength = true;   // Whether to trim circular overlap
        public int trimStart = 14;                    // Start position for trimming
        public int trimEnd = 5410;                    // End position for trimming

        public AssemblyConfig() {
        }

        public AssemblyConfig(int kmerSize) {
            this.kmerSize = kmerSize;
        }

        public AssemblyConfig kmerSize(int k) {
            this.kmerSize = k;
            return this;
        }

        public AssemblyConfig expectedLength(int length) {
            this.expectedGenomeLength = length;
            return this;
        }

        public AssemblyConfig noTrimming() {
            this.trimToExpectedLength = false;
            return this;
        }

        public AssemblyConfig trimming(int start, int end) {
            this.trimToExpectedLength = true;
            this.trimStart = start;
            this.trimEnd = end;
            return this;
        }
    }

    /**
     * Result of genome assembly.
     */
    public static class AssemblyResult {
        public final String genome;
        public final int genomeLength;
        public final int numReads;
        public final int numVertices;
        public final int numEdges;
        public final int tipsRemoved;
        public final int bubblesResolved;
        public final long assemblyTimeMs;

        public AssemblyResult(String genome, int numReads, int numVertices, int numEdges,
                              int tipsRemoved, int bubblesResolved, long assemblyTimeMs) {
            this.genome = genome;
            this.genomeLength = genome.length();
            this.numReads = numReads;
            this.numVertices = numVertices;
            this.numEdges = numEdges;
            this.tipsRemoved = tipsRemoved;
            this.bubblesResolved = bubblesResolved;
            this.assemblyTimeMs = assemblyTimeMs;
        }

        @Override
        public String toString() {
            return String.format(
                    "Assembly Result:\n" +
                            "  Genome length: %d bases\n" +
                            "  Input reads: %d\n" +
                            "  Graph vertices: %d\n" +
                            "  Graph edges: %d\n" +
                            "  Tips removed: %d\n" +
                            "  Bubbles resolved: %d\n" +
                            "  Assembly time: %d ms (%.2f seconds)",
                    genomeLength, numReads, numVertices, numEdges,
                    tipsRemoved, bubblesResolved, assemblyTimeMs, assemblyTimeMs / 1000.0
            );
        }

        public String toJson() {
            return String.format(
                    "{\n" +
                            "  \"genomeLength\": %d,\n" +
                            "  \"numReads\": %d,\n" +
                            "  \"numVertices\": %d,\n" +
                            "  \"numEdges\": %d,\n" +
                            "  \"tipsRemoved\": %d,\n" +
                            "  \"bubblesResolved\": %d,\n" +
                            "  \"assemblyTimeMs\": %d\n" +
                            "}",
                    genomeLength, numReads, numVertices, numEdges,
                    tipsRemoved, bubblesResolved, assemblyTimeMs
            );
        }
    }
}
