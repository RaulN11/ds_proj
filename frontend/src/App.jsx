import { useEffect, useState } from 'react'
import { setToken, onLog } from './api'
import Login from './Login'
import Admin from './Admin'
import Client from './Client'
import './styles.css'

export default function App() {
  const [session, setSession] = useState(() => {
    const s = JSON.parse(localStorage.getItem('session') || 'null')
    if (s) setToken(s.token)
    return s
  })
  const [logs, setLogs] = useState([])

  useEffect(() => { onLog((e) => setLogs((l) => [e, ...l].slice(0, 40))) }, [])

  useEffect(() => {
    const out = () => { setToken(null); localStorage.removeItem('session'); setSession(null) }
    window.addEventListener('unauthorized', out)
    return () => window.removeEventListener('unauthorized', out)
  }, [])

  const login = (s) => {
    setToken(s.token)
    localStorage.setItem('session', JSON.stringify(s))
    setSession(s)
  }
  const logout = () => {
    setToken(null)
    localStorage.removeItem('session')
    setSession(null)
  }

  return (
      <div className="layout">
        <main>
          <header>
            <h1>Energy Management System</h1>
            {session && (
                <span>
              {session.username} <b>({session.role})</b>{' '}
                  <button onClick={logout}>Logout</button>
            </span>
            )}
          </header>
          {!session && <Login onLogin={login} />}
          {session?.role === 'ADMIN' && <Admin />}
          {session && session.role !== 'ADMIN' && <Client session={session} />}
        </main>
      </div>
  )
}
