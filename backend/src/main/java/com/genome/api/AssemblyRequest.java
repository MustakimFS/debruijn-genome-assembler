package com.genome.api;

public class AssemblyRequest {
    private String fileName;
    private int kmerSize = 20;
    private boolean enableStats = true;

    public AssemblyRequest() {
    }

    public AssemblyRequest(String fileName, int kmerSize, boolean enableStats) {
        this.fileName = fileName;
        this.kmerSize = kmerSize;
        this.enableStats = enableStats;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getKmerSize() {
        return kmerSize;
    }

    public void setKmerSize(int kmerSize) {
        this.kmerSize = kmerSize;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
    }
}