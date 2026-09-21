import React, { useState, useEffect } from 'react';
import ChatWindow from './ChatWindow';
import DocumentUploader from './DocumentUploader';

function App() {
  const [documentsIngested, setDocumentsIngested] = useState(0);
  
  // Default to dark mode
  const [isDarkMode, setIsDarkMode] = useState(true);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  return (
    <>
      <div className="panel" style={{ flex: '1', display: 'flex', flexDirection: 'column', maxWidth: '350px' }}>
        <div style={{ padding: '2rem', borderBottom: '1px solid var(--panel-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 style={{ fontSize: '1.5rem', fontWeight: '700', marginBottom: '0.25rem' }}>
              Forge AI
            </h1>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
              Minimalist Semantic Agent
            </p>
          </div>
          <button 
            onClick={() => setIsDarkMode(!isDarkMode)}
            style={{ 
              padding: '0.5rem', 
              borderRadius: '50%', 
              width: '40px', 
              height: '40px', 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center',
              background: 'var(--panel-border)',
              color: 'var(--text-primary)'
            }}
            title="Toggle Theme"
          >
            {isDarkMode ? '☀️' : '🌙'}
          </button>
        </div>
        
        <div style={{ padding: '2rem', flex: 1 }}>
          <h2 style={{ fontSize: '1.1rem', marginBottom: '1.5rem', fontWeight: '600' }}>Knowledge Base</h2>
          <DocumentUploader onUploadSuccess={() => setDocumentsIngested(prev => prev + 1)} />
          
          {documentsIngested > 0 && (
            <div style={{ marginTop: '2rem', padding: '1rem', background: 'var(--panel-border)', borderRadius: '16px' }}>
              <p style={{ fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: '500' }}>
                <span style={{ display: 'inline-block', width: '8px', height: '8px', background: 'var(--accent-color)', borderRadius: '50%' }}></span>
                {documentsIngested} document(s) active
              </p>
            </div>
          )}
        </div>
      </div>

      <div className="panel" style={{ flex: '2', display: 'flex', flexDirection: 'column' }}>
        <ChatWindow />
      </div>
    </>
  );
}

export default App;
