import { useState } from 'react'
import { api } from './api'

const empty = { username: '', password: '', role: 'CLIENT', name: '', email: '', age: '' }

export default function Login({ onLogin }) {
  const [mode, setMode] = useState('login')
  const [f, setF] = useState(empty)
  const [msg, setMsg] = useState('')
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setMsg('')
    try {
      if (mode === 'login') {
        onLogin(await api('POST', '/auth/login', { username: f.username, password: f.password }))
      } else {
        await api('POST', '/auth/register', { ...f, age: f.age ? Number(f.age) : null })
        setMsg('Registered. You can log in now.')
        setMode('login')
      }
    } catch (err) {
      setMsg(err.message)
    }
  }

  return (
    <form className="card" onSubmit={submit}>
      <h2>{mode === 'login' ? 'Login' : 'Register'}</h2>
      <input placeholder="username" value={f.username} onChange={set('username')} required />
      <input placeholder="password" type="password" value={f.password} onChange={set('password')} required />
      {mode === 'register' && (
        <>
          <input placeholder="name" value={f.name} onChange={set('name')} />
          <input placeholder="email" value={f.email} onChange={set('email')} />
          <input placeholder="age" type="number" value={f.age} onChange={set('age')} />
          <select value={f.role} onChange={set('role')}>
            <option>CLIENT</option>
            <option>ADMIN</option>
          </select>
        </>
      )}
      <button type="submit">{mode === 'login' ? 'Login' : 'Register'}</button>
      <a href="#" onClick={(e) => { e.preventDefault(); setMode(mode === 'login' ? 'register' : 'login') }}>
        {mode === 'login' ? 'Need an account? Register' : 'Back to login'}
      </a>
      {msg && <p className="error">{msg}</p>}
    </form>
  )
}
