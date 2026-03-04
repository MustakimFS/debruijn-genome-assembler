import { useState } from 'react';
import { useDropzone } from 'react-dropzone';
import axios from 'axios';
import { Upload, Loader2, Download, CheckCircle, AlertCircle, FileText, Activity } from 'lucide-react';
import './App.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

function App() {
    const [file, setFile] = useState(null);
    const [kmerSize, setKmerSize] = useState(20);
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);

    const onDrop = (acceptedFiles) => {
        if (acceptedFiles.length > 0) {
            setFile(acceptedFiles[0]);
            setResult(null);
            setError(null);
        }
    };

    const { getRootProps, getInputProps, isDragActive } = useDropzone({
        onDrop,
        accept: {
            'text/plain': ['.txt', '.fasta', '.fa', '.fna']
        },
        maxFiles: 1,
        maxSize: 50 * 1024 * 1024 // 50MB
    });

    const handleAssemble = async () => {
        if (!file) {
            setError('Please select a file first');
            return;
        }

        setLoading(true);
        setError(null);
        setResult(null);

        try {
            let response;

            // Check if this is demo data
            if (file.name.includes('Demo Data')) {
                // Use demo endpoint
                response = await axios.post(`${API_URL}/api/assemble-demo`, null, {
                    params: { kmerSize }
                });
            } else {
                // Use regular file upload endpoint
                const formData = new FormData();
                formData.append('file', file);
                formData.append('kmerSize', kmerSize);

                response = await axios.post(`${API_URL}/api/assemble`, formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                });
            }

            if (response.data.success) {
                setResult(response.data);
            } else {
                setError(response.data.errorMessage || 'Assembly failed');
            }
        } catch (err) {
            setError(err.response?.data?.errorMessage || err.message || 'Failed to connect to server');
        } finally {
            setLoading(false);
        }
    };

    const handleDownload = () => {
        if (!result?.genome) return;

        const blob = new Blob([result.genome], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `assembled_genome_${Date.now()}.txt`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    };

    const loadDemoData = () => {
        // Create a mock file object to indicate demo data is selected
        const demoFile = new File(['demo-placeholder'], 'dataset1.txt (Demo Data)', {
            type: 'text/plain'
        });
        setFile(demoFile);
        setError(null);
        setResult(null);
    };

    return (
        <div className="app">
            <div className="container">
                {/* Header */}
                <header className="header">
                    <div className="header-content">
                        <h1>🧬 Genome Assembler</h1>
                        <p>De Bruijn Graph-based Genome Assembly</p>
                    </div>
                    <a
                        href="https://github.com/MustakimFS/debruijn-genome-assembler"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="github-link"
                    >
                        <svg height="24" width="24" viewBox="0 0 16 16" fill="currentColor">
                            <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/>
                        </svg>
                        View on GitHub
                    </a>
                </header>

                {/* Upload Section */}
                <div className="upload-section">
                    <div
                        {...getRootProps()}
                        className={`dropzone ${isDragActive ? 'active' : ''} ${file ? 'has-file' : ''}`}
                    >
                        <input {...getInputProps()} />
                        {file ? (
                            <div className="file-info">
                                <FileText size={48} />
                                <p className="file-name">{file.name}</p>
                                <p className="file-size">{(file.size / 1024).toFixed(2)} KB</p>
                            </div>
                        ) : (
                            <div className="upload-prompt">
                                <Upload size={48} />
                                <p>Drag & drop FASTA file here</p>
                                <p className="upload-hint">or click to browse</p>
                                <p className="upload-limit">Max 50MB • .fasta, .fa, .txt</p>
                            </div>
                        )}
                    </div>

                    <div className="controls">
                        <div className="kmer-control">
                            <label htmlFor="kmer">K-mer size:</label>
                            <input
                                id="kmer"
                                type="number"
                                min="5"
                                max="50"
                                value={kmerSize}
                                onChange={(e) => setKmerSize(parseInt(e.target.value))}
                                disabled={loading}
                            />
                        </div>

                        <div className="button-group">
                            <button
                                onClick={loadDemoData}
                                className="button button-secondary"
                                disabled={loading}
                            >
                                Load Demo Data
                            </button>
                            <button
                                onClick={handleAssemble}
                                className="button button-primary"
                                disabled={loading || !file}
                            >
                                {loading ? (
                                    <>
                                        <Loader2 size={20} className="spin" />
                                        Assembling...
                                    </>
                                ) : (
                                    <>
                                        <Activity size={20} />
                                        Assemble Genome
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Error Display */}
                {error && (
                    <div className="alert alert-error">
                        <AlertCircle size={20} />
                        <span>{error}</span>
                    </div>
                )}

                {/* Results Display */}
                {result && (
                    <div className="results">
                        <div className="results-header">
                            <CheckCircle size={24} />
                            <h2>Assembly Complete</h2>
                        </div>

                        <div className="stats-grid">
                            <div className="stat-card">
                                <span className="stat-label">Genome Length</span>
                                <span className="stat-value">{result.genomeLength.toLocaleString()} bp</span>
                            </div>
                            <div className="stat-card">
                                <span className="stat-label">Input Reads</span>
                                <span className="stat-value">{result.inputReads.toLocaleString()}</span>
                            </div>
                            <div className="stat-card">
                                <span className="stat-label">Assembly Time</span>
                                <span className="stat-value">{result.assemblyTimeMs} ms</span>
                            </div>
                            <div className="stat-card">
                                <span className="stat-label">Graph Edges</span>
                                <span className="stat-value">{result.graphEdges.toLocaleString()}</span>
                            </div>
                        </div>

                        <div className="genome-preview">
                            <div className="genome-header">
                                <h3>Assembled Genome Sequence</h3>
                                <button onClick={handleDownload} className="button button-small">
                                    <Download size={16} />
                                    Download
                                </button>
                            </div>
                            <div className="genome-sequence">
                                {result.genome.substring(0, 500)}
                                {result.genome.length > 500 && '...'}
                            </div>
                            <p className="genome-info">
                                Showing first 500 bases of {result.genomeLength.toLocaleString()} total
                            </p>
                        </div>
                    </div>
                )}

                {/* Footer */}
                <footer className="footer">
                    <p>Built by Mustakim Shikalgar • MS Software Engineering, Arizona State University</p>
                </footer>
            </div>
        </div>
    );
}

export default App;