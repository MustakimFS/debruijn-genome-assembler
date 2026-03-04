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

            // Read the file and assemble
            String[] reads = readReadsFromFile(filePath.toString());

            // Call your existing assembler code
            // TODO: This needs to be adapted based on your actual assembler API
            // For now, this is a placeholder that mimics the structure
            String genome = runAssembler(reads, kmerSize);

            // Generate statistics (placeholder - adapt based on your actual code)
            int genomeLength = genome.length();
            int inputReads = reads.length;

            // Clean up temporary file
            Files.deleteIfExists(filePath);

            long assemblyTime = System.currentTimeMillis() - startTime;

            return AssemblyResponse.success(
                    genome,
                    genomeLength,
                    inputReads,
                    0, // graphVertices - TODO: extract from your assembler
                    0, // graphEdges - TODO: extract from your assembler
                    0, // tipsRemoved - TODO: extract from your assembler
                    0, // bubblesResolved - TODO: extract from your assembler
                    assemblyTime
            );

        } catch (Exception e) {
            return AssemblyResponse.error("Assembly failed: " + e.getMessage());
        }
    }

    private String[] readReadsFromFile(String filePath) throws IOException {
        List<String> readList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    readList.add(line);
                }
            }
        }

        return readList.toArray(new String[0]);
    }

    private String runAssembler(String[] reads, int kmerSize) {
        // TODO: Call your actual genome assembler code here
        // This is a placeholder - we'll integrate your real assembler next

        // For now, return a mock result
        return "ATGC" + "N".repeat(100) + "TACG"; // Placeholder
    }
}