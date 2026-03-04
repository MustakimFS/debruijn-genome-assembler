package com.genome.overlap;

import java.util.*;
import java.io.*;

/**
 * Assignment 1: Assembling phi X174 Using Overlap Graphs
 *
 * Algorithm:
 * 1. Build suffix array for efficient overlap finding (O(n log n) doubling)
 * 2. Use BWT (Burrows-Wheeler Transform) for pattern matching
 * 3. Use k-mer seed (k=12) to find candidate overlapping reads efficiently
 * 4. Greedy Hamiltonian path: always extend with max-overlap read
 * 5. Trim circular overlap at the end
 *
 * Bugs fixed from original:
 * - occurrences used HashMap (unordered) → changed to TreeMap so smallest pos
 *   (= longest overlap) is found first.
 * - Seed length was min(K_MER_SIZE, patternLen) → must be min(K_MER_SIZE, patternLen-1)
 *   because overlap is always strictly shorter than the full read.
 * - Overlap validation skipped pos==0 check (pos==0 means full-text match, not overlap).
 * - Replaced fragile substring-equals with regionMatches for exact overlap verification.
 */
public class OverlapAssembler {

    private static final int K_MER_SIZE = 12; // For fast overlap detection

    public static String assemble(List<String> reads) {
        // Remove duplicate reads
        Set<String> uniqueReads = new HashSet<>(reads);
        List<String> readList = new ArrayList<>(uniqueReads);

        if (readList.isEmpty()) {
            return "";
        }

        // Start with first read
        int currentIndex = 0;
        StringBuilder genome = new StringBuilder(readList.get(0));
        String firstRead = readList.get(currentIndex);

        // Keep assembling until only one read left
        while (readList.size() > 1) {
            String currentRead = readList.get(currentIndex);
            readList.remove(currentIndex);

            // Find read with longest overlap with current read
            OverlapResult result = findLongestOverlap(currentRead + "$", readList);
            currentIndex = result.readIndex;
            int overlap = result.overlapLength;

            // Append non-overlapping part
            genome.append(readList.get(currentIndex).substring(overlap));
        }

        // Handle circular genome - check if end overlaps with beginning
        OverlapResult circularResult = findLongestOverlap(
                readList.get(0) + "$",
                Arrays.asList(firstRead)
        );

        if (circularResult.overlapLength > 0) {
            // Remove circular overlap
            return genome.substring(0, genome.length() - circularResult.overlapLength);
        }

        return genome.toString();
    }

    private static OverlapResult findLongestOverlap(String text, List<String> patterns) {
        // Build suffix array
        int[] suffixArray = buildSuffixArray(text);

        // Build BWT and supporting structures
        BWTResult bwtResult = buildBWT(text, suffixArray);

        int textLength = text.length() - 1; // Exclude '$'
        // BUG FIX: Use TreeMap so positions are iterated in ascending order.
        // Smallest position = longest suffix overlap. HashMap gave random order.
        Map<Integer, List<Integer>> occurrences = new TreeMap<>();

        // For each pattern, find occurrences using BWT
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            // BUG FIX: Seed must be < pattern.length() because overlap is always
            // strictly shorter than the read. If seed == full read, it can only
            // match at pos=0 in text (no actual overlap). Use min(K_MER_SIZE, len-1).
            int seedLen = Math.min(K_MER_SIZE, pattern.length() - 1);
            if (seedLen <= 0) continue;
            String searchPattern = pattern.substring(0, seedLen);

            // BWT-based pattern matching
            int top = 0;
            int bottom = bwtResult.bwt.length - 1;
            int currentIndex = searchPattern.length() - 1;

            while (top <= bottom) {
                if (currentIndex >= 0) {
                    char symbol = searchPattern.charAt(currentIndex);
                    currentIndex--;

                    int countTop = bwtResult.counts.get(symbol)[top];
                    int countBottom = bwtResult.counts.get(symbol)[bottom + 1];

                    if (countBottom - countTop > 0) {
                        top = bwtResult.starts.get(symbol) + countTop;
                        bottom = bwtResult.starts.get(symbol) + countBottom - 1;
                    } else {
                        break; // Pattern not found
                    }
                } else {
                    // Found pattern, record occurrences
                    for (int j = top; j <= bottom; j++) {
                        int pos = suffixArray[j];
                        occurrences.computeIfAbsent(pos, k -> new ArrayList<>()).add(i);
                    }
                    break;
                }
            }
        }

        // Find longest overlap — TreeMap gives us ascending pos order,
        // so first valid match is automatically the longest overlap.
        for (Map.Entry<Integer, List<Integer>> entry : occurrences.entrySet()) {
            int pos = entry.getKey();
            // BUG FIX: pos==0 means seed matched at start of text (full match, not an overlap).
            // pos>=textLength means zero-length suffix — no overlap possible.
            if (pos == 0 || pos >= textLength) continue;
            for (int readIndex : entry.getValue()) {
                String pattern = patterns.get(readIndex);
                int overlapLen = textLength - pos;
                // Verify: suffix of text (excluding '$') equals prefix of pattern
                if (overlapLen <= pattern.length() &&
                        text.regionMatches(pos, pattern, 0, overlapLen)) {
                    return new OverlapResult(readIndex, overlapLen);
                }
            }
        }

        // Brute-force fallback: BWT seed missed small overlaps (overlap < K_MER_SIZE).
        // Find the maximum overlap via direct suffix-prefix comparison.
        int bestOverlap = 0;
        int bestIndex = 0;
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            int maxCheck = Math.min(textLength, pattern.length());
            for (int len = maxCheck; len >= 1; len--) {
                if (text.regionMatches(textLength - len, pattern, 0, len)) {
                    if (len > bestOverlap) {
                        bestOverlap = len;
                        bestIndex = i;
                    }
                    break;
                }
            }
        }
        return new OverlapResult(bestIndex, bestOverlap);
    }

    private static int[] buildSuffixArray(String text) {
        int n = text.length();
        int[] order = sortCharacters(text);
        int[] classes = computeCharClasses(text, order);

        int L = 1;
        while (L < n) {
            order = sortDoubled(text, L, order, classes);
            classes = updateClasses(order, classes, L);
            L *= 2;
        }

        return order;
    }

    private static int[] sortCharacters(String text) {
        int n = text.length();
        int[] order = new int[n];
        Map<Character, Integer> count = new HashMap<>();

        // Count characters
        for (char c : text.toCharArray()) {
            count.put(c, count.getOrDefault(c, 0) + 1);
        }

        // Compute cumulative counts
        List<Character> chars = new ArrayList<>(count.keySet());
        Collections.sort(chars);

        for (int i = 1; i < chars.size(); i++) {
            char curr = chars.get(i);
            char prev = chars.get(i - 1);
            count.put(curr, count.get(curr) + count.get(prev));
        }

        // Build order array
        for (int i = n - 1; i >= 0; i--) {
            char c = text.charAt(i);
            count.put(c, count.get(c) - 1);
            order[count.get(c)] = i;
        }

        return order;
    }

    private static int[] computeCharClasses(String text, int[] order) {
        int n = text.length();
        int[] classes = new int[n];
        classes[order[0]] = 0;

        for (int i = 1; i < n; i++) {
            if (text.charAt(order[i]) != text.charAt(order[i - 1])) {
                classes[order[i]] = classes[order[i - 1]] + 1;
            } else {
                classes[order[i]] = classes[order[i - 1]];
            }
        }

        return classes;
    }

    private static int[] sortDoubled(String text, int L, int[] order, int[] classes) {
        int n = text.length();
        int[] count = new int[n];
        int[] newOrder = new int[n];

        for (int i = 0; i < n; i++) {
            count[classes[i]]++;
        }

        for (int i = 1; i < n; i++) {
            count[i] += count[i - 1];
        }

        for (int i = n - 1; i >= 0; i--) {
            int start = (order[i] - L + n) % n;
            int cl = classes[start];
            count[cl]--;
            newOrder[count[cl]] = start;
        }

        return newOrder;
    }

    private static int[] updateClasses(int[] order, int[] classes, int L) {
        int n = order.length;
        int[] newClasses = new int[n];
        newClasses[order[0]] = 0;

        for (int i = 1; i < n; i++) {
            int curr = order[i];
            int prev = order[i - 1];
            int mid = (curr + L) % n;
            int midPrev = (prev + L) % n;

            if (classes[curr] != classes[prev] || classes[mid] != classes[midPrev]) {
                newClasses[curr] = newClasses[prev] + 1;
            } else {
                newClasses[curr] = newClasses[prev];
            }
        }

        return newClasses;
    }

    private static BWTResult buildBWT(String text, int[] suffixArray) {
        int n = text.length();
        char[] bwt = new char[n];

        // Build BWT
        for (int i = 0; i < n; i++) {
            bwt[i] = text.charAt((suffixArray[i] + n - 1) % n);
        }

        // Build count and start arrays
        char[] alphabet = {'$', 'A', 'C', 'G', 'T'};
        Map<Character, int[]> counts = new HashMap<>();
        Map<Character, Integer> starts = new HashMap<>();

        for (char c : alphabet) {
            counts.put(c, new int[n + 1]);
        }

        // Compute cumulative counts
        for (int i = 0; i < n; i++) {
            char currentChar = bwt[i];
            for (char c : alphabet) {
                counts.get(c)[i + 1] = counts.get(c)[i];
            }
            counts.get(currentChar)[i + 1]++;
        }

        // Compute start positions
        int currentIndex = 0;
        for (char c : alphabet) {
            starts.put(c, currentIndex);
            currentIndex += counts.get(c)[n];
        }

        return new BWTResult(bwt, starts, counts);
    }

    private static class BWTResult {
        char[] bwt;
        Map<Character, Integer> starts;
        Map<Character, int[]> counts;

        BWTResult(char[] bwt, Map<Character, Integer> starts, Map<Character, int[]> counts) {
            this.bwt = bwt;
            this.starts = starts;
            this.counts = counts;
        }
    }

    private static class OverlapResult {
        int readIndex;
        int overlapLength;

        OverlapResult(int readIndex, int overlapLength) {
            this.readIndex = readIndex;
            this.overlapLength = overlapLength;
        }
    }

    public static void main(String[] args) throws IOException {
        // Read from stdin
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> reads = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                reads.add(line);
            }
        }

        // Assemble genome
        String genome = assemble(reads);
        System.out.println(genome);
    }
}