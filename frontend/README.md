# Genome Assembler - Frontend

Modern React web interface for the de Bruijn genome assembler. Dark-themed UI with drag-and-drop file upload.

## 🚀 Quick Start

### Development
```bash
# Install dependencies
npm install

# Start dev server
npm run dev
```

Open http://localhost:3000

### Build for Production
```bash
npm run build
```

Outputs to `dist/` folder, ready for deployment.

## 🎨 Features

- **Drag & Drop Upload** - Easy file upload interface
- **Real-time Results** - Assembly statistics and genome sequence
- **Dark Theme** - Professional, easy-on-the-eyes design
- **Responsive** - Works on desktop and mobile
- **Download Results** - Export assembled genome as .txt

## 🔧 Configuration

Edit `.env` to configure backend URL:
```
VITE_API_URL=http://localhost:8080
```

For production, set this to your deployed backend URL.

## 📦 Tech Stack

- React 18
- Vite
- Axios (HTTP client)
- React Dropzone (file upload)
- Lucide React (icons)

## 🚀 Deployment

### Vercel (Recommended)
```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
vercel
```

Or connect your GitHub repo to Vercel for auto-deployment.

### Environment Variables

Set in Vercel dashboard:
- `VITE_API_URL` = Your Railway backend URL

## 📁 Project Structure
```
frontend/
├── src/
│   ├── App.jsx          # Main component
│   ├── App.css          # Styles
│   └── main.jsx         # Entry point
├── public/              # Static assets
├── .env                 # Environment config
└── package.json         # Dependencies
```

## 🎯 API Integration

Connects to backend at `/api/assemble`:
```javascript
POST /api/assemble
- file: FASTA file
- kmerSize: K-mer size (default: 20)

Response:
{
  "success": true,
  "genome": "ATGC...",
  "genomeLength": 5396,
  "assemblyTimeMs": 1812
}
```