# Genome Toolkit — De Bruijn Graph Genome Assembler

A modular genome assembly toolkit implemented in Java using de Bruijn graph algorithms and error correction techniques. This project provides a production-ready command-line genome assembler with a clean Maven build system and executable JAR packaging.

---

## Overview

This toolkit reconstructs circular bacterial genomes from sequencing reads using de Bruijn graph construction, Eulerian cycle reconstruction via Hierholzer's algorithm, tip removal for sequencing error correction, bubble detection and resolution, an optional greedy overlap assembly baseline, and a modular CLI architecture.

Originally built for assembling the phi X174 bacteriophage genome (~5.4 kb).

---

## Features

### De Bruijn Graph Assembler (Primary Mode)

Pipeline:

1. Extract k-mers from reads
2. Build de Bruijn graph
3. Remove tips (error correction)
4. Detect and resolve bubbles
5. Find Eulerian cycle
6. Trim circular genome
7. Output genome and statistics

### Greedy Overlap Assembler (Baseline)

Implements maximum suffix-prefix overlap merging for comparison with graph-based assembly.

---

## Project Structure

```
genome-assembler/
├── src/main/java/com/genome/
│   ├── assembly/              # De Bruijn assembly pipeline
│   ├── correction/            # Tip and bubble correction
│   ├── graph/                 # Graph structures and Eulerian traversal
│   ├── overlap/               # Greedy overlap assembler
│   ├── errorProneAssembly/    # Experimental and legacy components
│   └── GenomeAssemblerCLI.java
│
├── data/                      # Test datasets and reference genome
├── pom.xml                    # Maven build configuration
└── README.md
```

---

## Build

**Requirements:**

- Java 17+
- IntelliJ IDEA with Maven plugin enabled

**Steps:**

1. Open the Maven Tool Window in IntelliJ
2. Run `clean`, then `package`

This generates:

```
target/genome-toolkit-1.0.0.jar
```

---

## Usage

### De Bruijn Assembly

```bash
java -jar target/genome-toolkit-1.0.0.jar assemble <reads-file> [options]
```

**Options:**

| Flag | Description |
|------|-------------|
| `-k <size>` | K-mer size (default: 20) |
| `-o <file>` | Write assembled genome to file |
| `--no-trim` | Disable circular trimming |
| `--stats` | Print assembly statistics |

**Example:**

```bash
java -jar target/genome-toolkit-1.0.0.jar assemble data/dataset1.txt --stats
```

### Greedy Overlap Assembly

```bash
java -jar target/genome-toolkit-1.0.0.jar overlap <reads-file>
```

**Example:**

```bash
java -jar target/genome-toolkit-1.0.0.jar overlap data/simple_test.txt
```

---

## Example Output

```
Reading reads from: data/dataset1.txt
Loaded 33609 reads
Assembling genome with k=20...

Assembly Result:
  Genome length:   5396 bases
  Input reads:     33609
  Graph vertices:  111283
  Graph edges:     111624
  Tips removed:    18
  Bubbles resolved: 2
  Assembly time:   1812 ms
```

---

## Algorithm Details

### De Bruijn Graph

- Vertices represent (k-1)-mers
- Edges represent k-mers
- Genome reconstruction is performed via Eulerian cycle traversal

### Eulerian Cycle

Uses an iterative implementation of Hierholzer's algorithm to ensure all edges are consumed exactly once.

### Error Correction

- **Tips:** Short dead-end branches are detected and removed
- **Bubbles:** Competing parallel paths are resolved via coverage comparison

---

## Testing

Run with the provided dataset:

```bash
java -jar target/genome-toolkit-1.0.0.jar assemble data/dataset1.txt --stats
```

The reference genome is available at:

```
data/genome1.txt
```

---

## Performance

Benchmarked on phi X174:

| Metric | Value |
|--------|-------|
| Input reads | 33,609 |
| Assembled genome length | 5,396 bp |
| Graph size | ~111,000 edges |
| Runtime | ~2 seconds |
| Accuracy | >99% |

---

## Design

- Pure Java with no external frameworks
- Lightweight Maven build
- Modular package architecture
- CLI-first interface
- Designed for experimentation and algorithm study

---

## Author

Mustakim Shikalgar  
MS Software Engineering, Arizona State University