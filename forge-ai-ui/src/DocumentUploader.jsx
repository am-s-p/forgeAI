import React, { useState, useRef } from 'react';

export default function DocumentUploader({ onUploadSuccess }) {
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [status, setStatus] = useState(null);
  const fileInputRef = useRef(null);

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setIsDragging(true);
    } else if (e.type === 'dragleave') {
      setIsDragging(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleUpload(e.dataTransfer.files[0]);
    }
  };

  const handleUpload = async (file) => {
    setIsUploading(true);
    setStatus('Uploading...');
    
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('http://localhost:8080/api/documents', {
        method: 'POST',
        body: formData,
      });
      
      if (!response.ok) throw new Error('Upload failed');
      const data = await response.json();
      
      setStatus(`Success! Ingested ${data.chunksIngested} chunks.`);
      if (onUploadSuccess) onUploadSuccess();
      
      setTimeout(() => setStatus(null), 3000);
    } catch (err) {
      setStatus('Error uploading file.');
      setTimeout(() => setStatus(null), 3000);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div 
      onDragEnter={handleDrag}
      onDragLeave={handleDrag}
      onDragOver={handleDrag}
      onDrop={handleDrop}
      onClick={() => fileInputRef.current?.click()}
      style={{
        border: `2px dashed ${isDragging ? 'var(--accent-color)' : 'var(--panel-border)'}`,
        background: isDragging ? 'var(--panel-border)' : 'var(--input-bg)',
        borderRadius: '24px',
        padding: '2rem',
        textAlign: 'center',
        cursor: 'pointer',
        transition: 'all 0.2s ease'
      }}
    >
      <input 
        type="file" 
        ref={fileInputRef} 
        onChange={(e) => e.target.files && handleUpload(e.target.files[0])}
        style={{ display: 'none' }} 
        accept=".pdf,.txt,.md,.docx"
      />
      
      <div style={{ fontSize: '2rem', marginBottom: '1rem', color: isDragging ? 'var(--accent-color)' : 'var(--text-secondary)' }}>
        📄
      </div>
      
      {isUploading ? (
        <p style={{ color: 'var(--text-primary)' }}>Processing document...</p>
      ) : status ? (
        <p style={{ color: status.includes('Error') ? '#ef4444' : '#10b981' }}>{status}</p>
      ) : (
        <>
          <p style={{ fontWeight: '500', marginBottom: '0.25rem' }}>Upload Document</p>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Drop a PDF or Markdown file here</p>
        </>
      )}
    </div>
  );
}
