package com.genome.api;

public class AssemblyResponse {
    private boolean success;
    private String genome;
    private int genomeLength;
    private int inputReads;
    private int graphVertices;
    private int graphEdges;
    private int tipsRemoved;
    private int bubblesResolved;
    private long assemblyTimeMs;
    private String errorMessage;

    public AssemblyResponse() {
    }

    // Success constructor
    public static AssemblyResponse success(String genome, int genomeLength, int inputReads,
                                           int graphVertices, int graphEdges, int tipsRemoved,
                                           int bubblesResolved, long assemblyTimeMs) {
        AssemblyResponse response = new AssemblyResponse();
        response.success = true;
        response.genome = genome;
        response.genomeLength = genomeLength;
        response.inputReads = inputReads;
        response.graphVertices = graphVertices;
        response.graphEdges = graphEdges;
        response.tipsRemoved = tipsRemoved;
        response.bubblesResolved = bubblesResolved;
        response.assemblyTimeMs = assemblyTimeMs;
        return response;
    }

    // Error constructor
    public static AssemblyResponse error(String errorMessage) {
        AssemblyResponse response = new AssemblyResponse();
        response.success = false;
        response.errorMessage = errorMessage;
        return response;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getGenome() {
        return genome;
    }

    public void setGenome(String genome) {
        this.genome = genome;
    }

    public int getGenomeLength() {
        return genomeLength;
    }

    public void setGenomeLength(int genomeLength) {
        this.genomeLength = genomeLength;
    }

    public int getInputReads() {
        return inputReads;
    }

    public void setInputReads(int inputReads) {
        this.inputReads = inputReads;
    }

    public int getGraphVertices() {
        return graphVertices;
    }

    public void setGraphVertices(int graphVertices) {
        this.graphVertices = graphVertices;
    }

    public int getGraphEdges() {
        return graphEdges;
    }

    public void setGraphEdges(int graphEdges) {
        this.graphEdges = graphEdges;
    }

    public int getTipsRemoved() {
        return tipsRemoved;
    }

    public void setTipsRemoved(int tipsRemoved) {
        this.tipsRemoved = tipsRemoved;
    }

    public int getBubblesResolved() {
        return bubblesResolved;
    }

    public void setBubblesResolved(int bubblesResolved) {
        this.bubblesResolved = bubblesResolved;
    }

    public long getAssemblyTimeMs() {
        return assemblyTimeMs;
    }

    public void setAssemblyTimeMs(long assemblyTimeMs) {
        this.assemblyTimeMs = assemblyTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}