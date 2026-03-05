package com.genome.api;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssemblerService {

    private static final String UPLOAD_DIR = "uploads/";
    private static final String CLI_JAR_PATH = "target/genome-toolkit-1.0.0.jar";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    public AssemblerService() {
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }

    public AssemblyResponse assembleGenome(MultipartFile file, int kmerSize) {
        long startTime = System.currentTimeMillis();

        try {
            // Validate file
            if (file.isEmpty()) {
                return AssemblyResponse.error("File is empty");
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return AssemblyResponse.error("File too large. Maximum size is 50MB");
            }

            // Save uploaded file temporarily
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Call the CLI assembler
            AssemblyResult result = runCLIAssembler(filePath.toString(), kmerSize);

            // Clean up temporary file
            Files.deleteIfExists(filePath);

            if (result == null || result.genome == null) {
                return AssemblyResponse.error("Assembly failed - no output from assembler");
            }

            long assemblyTime = System.currentTimeMillis() - startTime;

            return AssemblyResponse.success(
                    result.genome,
                    result.genomeLength,
                    result.inputReads,
                    result.graphVertices,
                    result.graphEdges,
                    result.tipsRemoved,
                    result.bubblesResolved,
                    assemblyTime
            );

        } catch (Exception e) {
            e.printStackTrace();
            return AssemblyResponse.error("Assembly failed: " + e.getMessage());
        }
    }

    private AssemblyResult runCLIAssembler(String filePath, int kmerSize) throws IOException, InterruptedException {
        // Check if JAR exists
        File jarFile = new File(CLI_JAR_PATH);
        if (!jarFile.exists()) {
            throw new IOException("Assembler JAR not found at: " + jarFile.getAbsolutePath());
        }

        // Build command to run CLI
        // Detect FASTA format to apply appropriate flags
        boolean isFasta = filePath.toLowerCase().endsWith(".fasta") ||
                filePath.toLowerCase().endsWith(".fa");

// Build command to run CLI
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(jarFile.getAbsolutePath());
        command.add("assemble");
        command.add(new File(filePath).getAbsolutePath());
        command.add("-k");
        command.add(String.valueOf(kmerSize));
        command.add("--stats");
        if (isFasta) {
            command.add("--no-tips");
            command.add("--no-trim");
        }

        ProcessBuilder pb = new ProcessBuilder(command);

        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Capture output
        StringBuilder output = new StringBuilder();
        StringBuilder statsOutput = new StringBuilder();
        String genome = null;
        boolean assemblyComplete = false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");

                // Mark when we've passed the "Assembly Result:" section
                if (line.contains("Assembly Result:")) {
                    assemblyComplete = true;
                }

                // Only capture genome sequence AFTER assembly is complete
                if (assemblyComplete && line.matches("^[ATGCN]+$") && line.length() >= 5) {
                    genome = line;
                }

                // Capture stats output
                if (line.contains("Assembly Result:") || line.contains("Genome length:") ||
                        line.contains("Input reads:") || line.contains("Graph vertices:")) {
                    statsOutput.append(line).append("\n");
                }
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Assembly failed with exit code: " + exitCode);
        }

        if (genome == null) {
            throw new IOException("No genome sequence found in assembler output");
        }

        // Parse stats from output
        return parseAssemblyOutput(genome, statsOutput.toString());
    }

    public AssemblyResponse assembleDemoData(int kmerSize) {
        long startTime = System.currentTimeMillis();

        try {
            String demoFilePath = "data/dataset1.txt";
            File demoFile = new File(demoFilePath);

            if (!demoFile.exists()) {
                return AssemblyResponse.error("Demo dataset not found at: " + demoFile.getAbsolutePath());
            }

            // Call the CLI assembler with demo data
            AssemblyResult result = runCLIAssembler(demoFile.getAbsolutePath(), kmerSize);

            if (result == null || result.genome == null) {
                return AssemblyResponse.error("Demo assembly failed - no output from assembler");
            }

            long assemblyTime = System.currentTimeMillis() - startTime;

            return AssemblyResponse.success(
                    result.genome,
                    result.genomeLength,
                    result.inputReads,
                    result.graphVertices,
                    result.graphEdges,
                    result.tipsRemoved,
                    result.bubblesResolved,
                    assemblyTime
            );

        } catch (Exception e) {
            e.printStackTrace();
            return AssemblyResponse.error("Demo assembly failed: " + e.getMessage());
        }
    }

    private AssemblyResult parseAssemblyOutput(String genome, String statsOutput) {
        AssemblyResult result = new AssemblyResult();
        result.genome = genome;
        result.genomeLength = genome != null ? genome.length() : 0;

        // Parse stats using regex
        try {
            if (statsOutput.contains("Input reads:")) {
                String readsLine = statsOutput.lines()
                        .filter(l -> l.contains("Input reads:"))
                        .findFirst().orElse("");
                result.inputReads = Integer.parseInt(readsLine.replaceAll("[^0-9]", ""));
            }

            if (statsOutput.contains("Graph vertices:")) {
                String verticesLine = statsOutput.lines()
                        .filter(l -> l.contains("Graph vertices:"))
                        .findFirst().orElse("");
                result.graphVertices = Integer.parseInt(verticesLine.replaceAll("[^0-9]", ""));
            }

            if (statsOutput.contains("Graph edges:")) {
                String edgesLine = statsOutput.lines()
                        .filter(l -> l.contains("Graph edges:"))
                        .findFirst().orElse("");
                result.graphEdges = Integer.parseInt(edgesLine.replaceAll("[^0-9]", ""));
            }

            if (statsOutput.contains("Tips removed:")) {
                String tipsLine = statsOutput.lines()
                        .filter(l -> l.contains("Tips removed:"))
                        .findFirst().orElse("");
                result.tipsRemoved = Integer.parseInt(tipsLine.replaceAll("[^0-9]", ""));
            }

            if (statsOutput.contains("Bubbles resolved:")) {
                String bubblesLine = statsOutput.lines()
                        .filter(l -> l.contains("Bubbles resolved:"))
                        .findFirst().orElse("");
                result.bubblesResolved = Integer.parseInt(bubblesLine.replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) {
            System.err.println("Error parsing stats: " + e.getMessage());
        }

        return result;
    }

    // Inner class to hold assembly results
    private static class AssemblyResult {
        String genome;
        int genomeLength;
        int inputReads;
        int graphVertices;
        int graphEdges;
        int tipsRemoved;
        int bubblesResolved;
    }
}