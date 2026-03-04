# Genome Assembler API - Backend

REST API for the de Bruijn genome assembler. Provides endpoints for uploading FASTA files and assembling genomes.

## Quick Start

### Local Development
```bash
# Build the core assembler first (from parent directory)
cd ..
mvn clean package

# Build and run backend
cd backend
mvn spring-boot:run
```

Server runs on: http://localhost:8080

### Test the API
```bash
# Health check
curl http://localhost:8080/api/health

# Assemble genome
curl -X POST http://localhost:8080/api/assemble \
  -F "file=@../data/dataset1.txt" \
  -F "kmerSize=20"
```

## API Endpoints

### POST /api/assemble
Assemble a genome from uploaded FASTA file.

**Parameters:**
- `file` (required): FASTA file with sequencing reads
- `kmerSize` (optional): K-mer size, default = 20

**Response:**
```json
{
  "success": true,
  "genome": "ATGCATGC...",
  "genomeLength": 5396,
  "inputReads": 33609,
  "graphVertices": 111283,
  "graphEdges": 111624,
  "tipsRemoved": 18,
  "bubblesResolved": 2,
  "assemblyTimeMs": 1812
}
```

### GET /api/health
Health check endpoint.

**Response:** `"Genome Assembler API is running"`

### GET /api/demo-data
Check if demo dataset is available.

## Docker
```bash
# Build
docker build -t genome-assembler-api .

# Run
docker run -p 8080:8080 genome-assembler-api
```

## Configuration

Edit `src/main/resources/application.properties`:
```properties
server.port=8080
spring.servlet.multipart.max-file-size=50MB
```

## Dependencies

- Spring Boot 3.2.0
- Spring Web
- Spring Validation
- Core genome-toolkit-1.0.0.jar

## Deployment

Ready for deployment on:
- Railway
- Render
- Heroku
- AWS/GCP/Azure

See main README for deployment instructions.