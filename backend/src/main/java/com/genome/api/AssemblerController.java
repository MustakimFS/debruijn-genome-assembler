package com.genome.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api")
public class AssemblerController {

    @Autowired
    private AssemblerService assemblerService;

    @PostMapping("/assemble")
    public ResponseEntity<AssemblyResponse> assembleGenome(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "kmerSize", defaultValue = "20") int kmerSize) {

        try {
            AssemblyResponse response = assemblerService.assembleGenome(file, kmerSize);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            AssemblyResponse errorResponse = AssemblyResponse.error(
                    "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Genome Assembler API is running");
    }

    @GetMapping("/demo-data")
    public ResponseEntity<String> getDemoData() {
        // Returns path to demo dataset for frontend
        return ResponseEntity.ok("dataset1.txt available");
    }

    @PostMapping("/assemble-demo")
    public ResponseEntity<AssemblyResponse> assembleDemoData(
            @RequestParam(value = "kmerSize", defaultValue = "20") int kmerSize) {

        try {
            // Use the actual dataset1.txt from data folder
            File demoFile = new File("data/dataset1.txt");

            if (!demoFile.exists()) {
                return ResponseEntity.badRequest()
                        .body(AssemblyResponse.error("Demo dataset not found"));
            }

            AssemblyResponse response = assemblerService.assembleDemoData(kmerSize);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            AssemblyResponse errorResponse = AssemblyResponse.error(
                    "Demo assembly failed: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}