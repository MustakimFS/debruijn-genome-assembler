# Genome Assembly - Assignment-by-Assignment Solutions

## Overview

Converting Coursera genome assembly assignments from Python to Java, testing each one individually before integration.

---

## Assignment 1: Error-Free Assembly (Overlap Graphs)

### Problem:
Assemble phi X174 genome from error-free reads using overlap graph approach.

### Algorithm:
1. **Suffix Array Construction** - O(n log n) using doubling algorithm
2. **BWT (Burrows-Wheeler Transform)** - For efficient pattern matching  
3. **Overlap Finding** - Use BWT for fast overlap detection (k=12)
4. **Greedy Assembly** - Chain reads with longest overlaps
5. **Circular Genome Handling** - Detect and remove circular overlap

### Implementation:
- **File**: `src/main/java/com/genome/assignment1/OverlapAssembler.java`
- **Status**: ✅ Compiled
- **Testing**: ⏳ In progress

### Key Classes:
- `OverlapAssembler` - Main assembly logic
- `BWTResult` - BWT data structure  
- `OverlapResult` - Overlap information

### Test Data:
- **Simple test**: 5 reads → Works ✅
- **Full dataset**: dataset1.txt (33,609 reads) → Pending

---

## Assignment 2: De Bruijn Graphs (Error-Free)

### Problem:
- **Part 1**: Puzzle Assembly (warm-up)
- **Part 2**: Eulerian Cycle finding
- **Part 3**: k-Universal String
- **Part 4**: Assemble phi X174 from k-mer composition

### Algorithm:
1. Build de Bruijn graph from k-mers
2. Find Eulerian cycle (Hierholzer's algorithm)
3. Reconstruct genome from cycle

### Implementation:
- **Status**: ⏸️ Pending
- **Files Needed**: 
  - `PuzzleAssembler.java`
  - `EulerianCycle.java`
  - `CircularString.java`
  - `PhiX174Assembler.java`

### Your Solutions:
- ✅ `puzzleAssembly.py`
- ✅ `eulerianCycle.py`
- ✅ `circularString.py`
- ✅ `phageX174.py`

---

## Assignment 3: Error-Prone Assembly (Real Data)

### Problem:
Handle real sequencing data with errors using:
1. Circulation in network (max flow)
2. Optimal k-mer size selection
3. Bubble detection
4. Tip removal
5. Full assembler with error correction

### Algorithm:
1. Build de Bruijn graph with k=15 or k=20
2. Remove tips (short dead-end paths)
3. Detect and resolve bubbles (alternative paths)
4. Find Eulerian cycle
5. Assemble genome

### Implementation:
- **Status**: ⏸️ Pending (this is what we were working on)
- **Files**: Already created in checkpoint 1

### Your Solutions:
- ✅ `circulation.py`
- ✅ `kmerSelection.py`
- ✅ `bubbleDetection.py`
- ✅ `tipRemoval.py`
- ✅ `errorProneAssembly.java` (original working version)

---

## Implementation Strategy

### Phase 1: Convert Python → Java (Assignment by Assignment)
For each assignment:
1. ✅ Create Java class
2. ✅ Compile and test with simple data
3. ✅ Test with full dataset
4. ✅ Verify output matches expected
5. ✅ Document and move to next

### Phase 2: Integration
Once all assignments work independently:
1. Integrate into unified GenomeAssembler
2. Add configuration for different approaches
3. Create comprehensive test suite
4. Add REST API
5. Deploy

---

## Testing Plan

### Assignment 1 Tests:
```bash
# Simple test
java -cp target/classes com.genome.overlap.OverlapAssembler < data/test_reads.txt

# Full dataset (error-free reads)
# Need to create error-free dataset or use subset
```

### Assignment 2 Tests:
```bash
# Eulerian cycle
java -cp target/classes com.genome.assignment2.EulerianCycle < data/eulerian_test.txt

# Puzzle
java -cp target/classes com.genome.assignment2.PuzzleAssembler < data/puzzle_test.txt

# Phi X174 (from k-mers)
java -cp target/classes com.genome.assignment2.PhiX174Assembler < data/kmers.txt
```

### Assignment 3 Tests:
```bash
# With error correction (this is dataset1.txt)
java -cp target/classes com.genome.errorProneAssembly.ErrorProneAssembler < data/dataset1.txt
```

---

## Current Status

| Assignment | Python | Java | Tested | Status |
|------------|--------|------|--------|--------|
| 1: Overlap | ✅ | ✅ | ⏳ | In Progress |
| 2: De Bruijn | ✅ | ❌ | ❌ | Pending |
| 3: Error-Prone | ✅ | ✅ | ❌ | Needs Testing |

---

## Next Steps

1. **✅ Test Assignment 1** with full dataset
2. **Create Assignment 2** Java implementations
3. **Fix Assignment 3** using lessons from 1 & 2
4. **Integrate** all three approaches
5. **Deploy** as web service

---

## File Organization

```
src/main/java/com/genome/
├── assignment1/
│   └── OverlapAssembler.java       ✅ Done
├── assignment2/
│   ├── PuzzleAssembler.java        ❌ TODO
│   ├── EulerianCycle.java          ❌ TODO
│   ├── CircularString.java         ❌ TODO
│   └── PhiX174Assembler.java       ❌ TODO
├── assignment3/
│   ├── Circulation.java            ❌ TODO
│   ├── KmerOptimizer.java          ❌ TODO
│   ├── BubbleDetection.java        ❌ TODO
│   ├── TipRemoval.java             ❌ TODO
│   └── ErrorProneAssembler.java    ❌ TODO (use original)
└── integrated/
    └── GenomeAssembler.java         (After all 3 work)
```

---

*Last Updated: December 25, 2024*
*Next: Test Assignment 1 with full dataset*
