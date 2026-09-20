import React, { useState } from 'react';
import ChatWindow from './ChatWindow';
import DocumentUploader from './DocumentUploader';

function App() {
  const [documentsIngested, setDocumentsIngested] = useState(0);

  return (
    <>
      <div className="glass-panel" style={{ flex: '1', display: 'flex', flexDirection: 'column', maxWidth: '350px' }}>
        <div style={{ padding: '2rem', borderBottom: '1px solid var(--panel-border)' }}>
          <h1 style={{ fontSize: '1.5rem', fontWeight: '700', marginBottom: '0.5rem', background: 'linear-gradient(to right, #60a5fa, #a78bfa)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            Forge AI
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Your intelligent agent with custom semantic memory.
          </p>
        </div>
        
        <div style={{ padding: '2rem', flex: 1 }}>
          <h2 style={{ fontSize: '1.1rem', marginBottom: '1.5rem', fontWeight: '600' }}>Knowledge Base</h2>
          <DocumentUploader onUploadSuccess={() => setDocumentsIngested(prev => prev + 1)} />
          
          {documentsIngested > 0 && (
            <div style={{ marginTop: '2rem', padding: '1rem', background: 'rgba(59, 130, 246, 0.1)', borderRadius: '12px', border: '1px solid rgba(59, 130, 246, 0.2)' }}>
              <p style={{ color: '#60a5fa', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ display: 'inline-block', width: '8px', height: '8px', background: '#60a5fa', borderRadius: '50%', boxShadow: '0 0 10px #60a5fa' }}></span>
                {documentsIngested} document(s) active in memory
              </p>
            </div>
          )}
        </div>
      </div>

      <div className="glass-panel" style={{ flex: '2', display: 'flex', flexDirection: 'column' }}>
        <ChatWindow />
      </div>
    </>
  );
}

export default App;
