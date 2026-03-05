package com.genome;

import com.genome.assembly.GenomeAssembler;
import com.genome.assembly.GenomeAssembler.AssemblyConfig;
import com.genome.assembly.GenomeAssembler.AssemblyResult;
import com.genome.overlap.OverlapAssembler;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Genome Toolkit CLI
 *
 * Commands:
 *   assemble <reads-file> [options]
 *   overlap  <reads-file>
 */
public class GenomeAssemblerCLI {

    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String command = args[0].toLowerCase();

        switch (command) {

            case "assemble":
                runAssembly(Arrays.copyOfRange(args, 1, args.length));
                break;

            case "overlap":
                runOverlap(Arrays.copyOfRange(args, 1, args.length));
                break;

            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }

    private static void runAssembly(String[] args) {

        if (args.length == 0) {
            System.err.println("assemble requires input file.");
            printUsage();
            System.exit(1);
        }

        try {
            String inputFile = args[0];
            String outputFile = null;
            int kmerSize = 20;
            boolean printStats = false;
            boolean noTrim = false;
            boolean noTips = false;

            for (int i = 1; i < args.length; i++) {
                switch (args[i]) {
                    case "-k":
                        kmerSize = Integer.parseInt(args[++i]);
                        break;
                    case "-o":
                        outputFile = args[++i];
                        break;
                    case "--stats":
                        printStats = true;
                        break;
                    case "--no-trim":
                        noTrim = true;
                        break;
                    case "--no-tips":
                        noTips = true;
                        break;
                    case "-h":
                    case "--help":
                        printUsage();
                        System.exit(0);
                    default:
                        System.err.println("Unknown option: " + args[i]);
                        printUsage();
                        System.exit(1);
                }
            }

            System.err.println("Reading reads from: " + inputFile);
            List<String> reads = readReadsFromFile(inputFile);
            System.err.println("Loaded " + reads.size() + " reads");

            AssemblyConfig config = new AssemblyConfig(kmerSize);
            if (noTrim) {
                config.noTrimming();
            }
            if (noTips) {
                config.skipTipRemoval = true;
            }

            System.err.println("Assembling genome with k=" + kmerSize + "...");
            GenomeAssembler assembler = new GenomeAssembler(config);
            String genome = assembler.assemble(reads);

            AssemblyResult result = assembler.getResult();

            if (printStats) {
                System.err.println("\n" + result.toString());
                System.err.println();
            }

            if (outputFile != null) {
                writeGenomeToFile(genome, outputFile);
                System.err.println("Genome written to: " + outputFile);
            } else {
                System.out.println(genome);
            }

        } catch (Exception e) {
            System.err.println("Assembly failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runOverlap(String[] args) {

        if (args.length == 0) {
            System.err.println("overlap requires input file.");
            printUsage();
            System.exit(1);
        }

        try {
            String inputFile = args[0];

            List<String> reads = readReadsFromFile(inputFile);

            OverlapAssembler assembler = new OverlapAssembler();
            String genome = assembler.assemble(reads);

            System.out.println(genome);

        } catch (Exception e) {
            System.err.println("Overlap assembly failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static List<String> readReadsFromFile(String filename) throws IOException {
        List<String> reads = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String firstLine = reader.readLine();
            if (firstLine == null) return reads;

            // FASTA format: header starts with '>'
            if (firstLine.startsWith(">")) {
                StringBuilder sequence = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith(">")) continue; // skip additional headers
                    if (!line.isEmpty()) sequence.append(line);
                }
                // Generate sliding-window reads from the full sequence
                String fullSeq = sequence.toString();
                int readLen = 70;   // simulated read length
                int step = 10;      // step between reads (controls coverage)
                for (int i = 0; i + readLen <= fullSeq.length(); i += step) {
                    reads.add(fullSeq.substring(i, i + readLen));
                }
                return reads;
            }

            // Plain format: optional count on first line
            try {
                Integer.parseInt(firstLine.trim());
            } catch (NumberFormatException ignored) {
                if (!firstLine.trim().isEmpty()) reads.add(firstLine.trim());
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String read = line.trim();
                if (!read.isEmpty()) reads.add(read);
            }
        }

        return reads;
    }

    private static void writeGenomeToFile(String genome, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(genome);
        }
    }

    private static void printUsage() {
        System.err.println(
                "Genome Toolkit CLI\n\n" +
                        "Commands:\n" +
                        "  assemble <reads-file> [options]\n" +
                        "  overlap  <reads-file>\n\n" +
                        "Options (assemble only):\n" +
                        "  -k <size>        K-mer size (default: 20)\n" +
                        "  -o <output>      Output file\n" +
                        "  --no-trim        Disable circular trimming\n" +
                        "  --no-tips        Disable tip removal (use for clean/simulated reads)\n" +
                        "  --stats          Print assembly statistics\n"
        );
    }
}