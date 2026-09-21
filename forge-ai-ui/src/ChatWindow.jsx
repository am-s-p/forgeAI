import React, { useState, useRef, useEffect } from 'react';

function FeedbackComponent({ conversationId }) {
  const [showInput, setShowInput] = useState(false);
  const [correction, setCorrection] = useState('');
  const [status, setStatus] = useState(null); // 'submitting', 'success', 'error'

  const handleFeedback = async (isPositive, text = '') => {
    if (!isPositive && !showInput) {
      setShowInput(true);
      return;
    }

    setStatus('submitting');
    try {
      await fetch('http://localhost:8080/api/chat/feedback', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          conversationId, 
          isPositive, 
          correctionText: text 
        })
      });
      setStatus('success');
      if (isPositive) setShowInput(false);
    } catch (err) {
      setStatus('error');
    }
  };

  if (status === 'success') {
    return <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>✓ Feedback saved</div>;
  }

  return (
    <div style={{ marginTop: '0.5rem' }}>
      {!showInput ? (
        <div style={{ display: 'flex', gap: '0.25rem' }}>
          <button className="feedback-icon" onClick={() => handleFeedback(true)} title="Good response">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path></svg>
          </button>
          <button className="feedback-icon" onClick={() => handleFeedback(false)} title="Needs correction">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7-13h2.67A2.31 2.31 0 0 1 22 4v7a2.31 2.31 0 0 1-2.33 2H17"></path></svg>
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginTop: '0.5rem' }}>
          <input 
            type="text" 
            value={correction}
            onChange={(e) => setCorrection(e.target.value)}
            placeholder="Tell the agent what rule to follow..."
            style={{
              flex: 1, background: 'var(--input-bg)', border: '1px solid var(--panel-border)', 
              borderRadius: '100px', padding: '0.5rem 1rem', color: 'var(--text-primary)', fontSize: '0.85rem', outline: 'none'
            }}
            autoFocus
          />
          <button 
            onClick={() => handleFeedback(false, correction)}
            disabled={!correction.trim() || status === 'submitting'}
            style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
          >
            {status === 'submitting' ? '...' : 'Save Rule'}
          </button>
        </div>
      )}
    </div>
  );
}

export default function ChatWindow() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const [conversationId] = useState(() => crypto.randomUUID());

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMsg = input.trim();
    setInput('');
    setMessages(prev => [...prev, { role: 'user', content: userMsg }]);
    setIsLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: userMsg, conversationId: conversationId })
      });
      
      const data = await response.json();
      
      setMessages(prev => [...prev, { 
        role: 'agent', 
        content: data.plan.actionSteps[0],
        tools: []
      }]);
    } catch (err) {
      setMessages(prev => [...prev, { role: 'error', content: 'Failed to connect to the agent.' }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: '2rem' }}>
      <div style={{ flex: 1, overflowY: 'auto', marginBottom: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {messages.length === 0 ? (
          <div style={{ margin: 'auto', color: 'var(--text-secondary)', textAlign: 'center' }}>
            <div style={{ width: '48px', height: '48px', background: 'var(--text-primary)', borderRadius: '50%', margin: '0 auto 1rem' }}></div>
            <p style={{ fontWeight: '500' }}>Forge AI is ready.</p>
          </div>
        ) : (
          messages.map((msg, i) => (
            <div key={i} className="bubble" style={{
              alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
              maxWidth: '85%',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.25rem'
            }}>
              <div style={{
                background: msg.role === 'user' ? 'var(--user-bubble)' : msg.role === 'error' ? 'rgba(239, 68, 68, 0.1)' : 'var(--agent-bubble)',
                color: msg.role === 'user' ? 'var(--user-text)' : 'var(--text-primary)',
                padding: '1.25rem',
                borderRadius: '24px',
                borderBottomRightRadius: msg.role === 'user' ? '8px' : '24px',
                borderTopLeftRadius: msg.role === 'agent' ? '8px' : '24px',
                border: msg.role === 'error' ? '1px solid rgba(239, 68, 68, 0.5)' : '1px solid transparent',
                lineHeight: '1.6',
                boxShadow: msg.role === 'agent' ? '0 4px 6px -1px rgba(0, 0, 0, 0.05)' : 'none'
              }}>
                {msg.content}
              </div>
              
              {msg.role === 'agent' && <FeedbackComponent conversationId={conversationId} />}
            </div>
          ))
        )}
        {isLoading && (
          <div className="bubble" style={{ alignSelf: 'flex-start', color: 'var(--text-secondary)', padding: '1rem' }}>
            <span style={{ animation: 'pulse 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite' }}>Agent is reasoning...</span>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '1rem', background: 'var(--input-bg)', padding: '0.75rem', borderRadius: '100px', border: '1px solid var(--panel-border)', boxShadow: 'var(--shadow)' }}>
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="Type your message..."
          style={{
            flex: 1, background: 'transparent', border: 'none', padding: '0 1rem', color: 'var(--text-primary)', outline: 'none', fontSize: '1rem'
          }}
        />
        <button type="submit" disabled={isLoading || !input.trim()} style={{ borderRadius: '100px', padding: '0.75rem 1.5rem' }}>
          {isLoading ? '...' : 'Send'}
        </button>
      </form>
    </div>
  );
}
