import React, { useState, useRef, useEffect } from 'react';

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
        body: JSON.stringify({ message: userMsg, conversationId: "default" })
      });
      
      const data = await response.json();
      
      setMessages(prev => [...prev, { 
        role: 'agent', 
        content: data.response,
        tools: data.toolCalls
      }]);
    } catch (err) {
      setMessages(prev => [...prev, { role: 'error', content: 'Failed to connect to the agent.' }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: '2rem' }}>
      <div style={{ flex: 1, overflowY: 'auto', marginBottom: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {messages.length === 0 ? (
          <div style={{ margin: 'auto', color: 'var(--text-secondary)', textAlign: 'center' }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>✨</div>
            <p>Start a conversation with Forge AI</p>
          </div>
        ) : (
          messages.map((msg, i) => (
            <div key={i} style={{
              alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
              maxWidth: '80%',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.5rem'
            }}>
              <div style={{
                background: msg.role === 'user' ? 'var(--accent-color)' : msg.role === 'error' ? 'rgba(239, 68, 68, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                padding: '1rem 1.25rem',
                borderRadius: '16px',
                borderBottomRightRadius: msg.role === 'user' ? '4px' : '16px',
                borderTopLeftRadius: msg.role === 'agent' ? '4px' : '16px',
                border: msg.role === 'error' ? '1px solid rgba(239, 68, 68, 0.5)' : '1px solid rgba(255, 255, 255, 0.05)',
                lineHeight: '1.5'
              }}>
                {msg.content}
              </div>
              
              {msg.tools && msg.tools.length > 0 && (
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                  {msg.tools.map((t, j) => (
                    <span key={j} style={{ background: 'rgba(0,0,0,0.3)', padding: '0.25rem 0.5rem', borderRadius: '4px' }}>
                      🔧 {t.toolName}
                    </span>
                  ))}
                </div>
              )}
            </div>
          ))
        )}
        {isLoading && (
          <div style={{ alignSelf: 'flex-start', color: 'var(--text-secondary)', padding: '1rem' }}>
            <span className="dot-pulse">Agent is thinking...</span>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '1rem' }}>
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="Ask me anything or query your documents..."
          style={{
            flex: 1,
            background: 'rgba(0, 0, 0, 0.2)',
            border: '1px solid var(--panel-border)',
            borderRadius: '12px',
            padding: '1rem 1.25rem',
            color: 'white',
            outline: 'none',
            fontSize: '1rem'
          }}
        />
        <button type="submit" disabled={isLoading || !input.trim()}>
          {isLoading ? '...' : 'Send'}
        </button>
      </form>
    </div>
  );
}
