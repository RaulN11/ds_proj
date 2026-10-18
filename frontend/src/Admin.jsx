import { useEffect, useState } from 'react'
import { api } from './api'

const emptyUser = { username: '', password: '', role: 'CLIENT', name: '', email: '', age: '' }
const emptyDevice = { name: '', description: '', maxConsumption: '' }

export default function Admin() {
  const [users, setUsers] = useState([])
  const [devices, setDevices] = useState([])
  const [error, setError] = useState('')
  const [newUser, setNewUser] = useState(emptyUser)
  const [editUser, setEditUser] = useState(null)
  const [device, setDevice] = useState(emptyDevice)
  const [editDeviceId, setEditDeviceId] = useState(null)

  const load = async () => {
    try {
      setUsers(await api('GET', '/users'))
      setDevices(await api('GET', '/devices'))
    } catch (e) { setError(e.message) }
  }
  useEffect(() => { load() }, [])

  const run = async (fn) => {
    try { setError(''); await fn(); await load() } catch (e) { setError(e.message) }
  }

  const username = (id) => users.find((u) => u.id === id)?.username

  return (
    <>
      {error && <p className="error">{error}</p>}

      <section className="card">
        <h2>Users</h2>
        <table>
          <thead><tr><th>Username</th><th>Name</th><th>Email</th><th>Age</th><th>Role</th><th /></tr></thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.username}</td><td>{u.name}</td><td>{u.email}</td><td>{u.age}</td><td>{u.role}</td>
                <td>
                  <button onClick={() => setEditUser({ ...u })}>Edit</button>{' '}
                  <button onClick={() => run(() => api('DELETE', `/users/${u.id}`))}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {editUser ? (
          <div className="row">
            <b>Edit {editUser.username}:</b>
            <input placeholder="name" value={editUser.name || ''} onChange={(e) => setEditUser({ ...editUser, name: e.target.value })} />
            <input placeholder="email" value={editUser.email || ''} onChange={(e) => setEditUser({ ...editUser, email: e.target.value })} />
            <input placeholder="age" type="number" value={editUser.age ?? ''} onChange={(e) => setEditUser({ ...editUser, age: e.target.value ? Number(e.target.value) : null })} />
            <button onClick={() => run(async () => { await api('PUT', `/users/${editUser.id}`, editUser); setEditUser(null) })}>Save</button>
            <button onClick={() => setEditUser(null)}>Cancel</button>
          </div>
        ) : (
          <div className="row">
            <b>New user (auth-service → user-service):</b>
            <input placeholder="username" value={newUser.username} onChange={(e) => setNewUser({ ...newUser, username: e.target.value })} />
            <input placeholder="password" type="password" value={newUser.password} onChange={(e) => setNewUser({ ...newUser, password: e.target.value })} />
            <input placeholder="name" value={newUser.name} onChange={(e) => setNewUser({ ...newUser, name: e.target.value })} />
            <input placeholder="email" value={newUser.email} onChange={(e) => setNewUser({ ...newUser, email: e.target.value })} />
            <input placeholder="age" type="number" value={newUser.age} onChange={(e) => setNewUser({ ...newUser, age: e.target.value })} />
            <select value={newUser.role} onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}>
              <option>CLIENT</option><option>ADMIN</option>
            </select>
            <button onClick={() => run(async () => {
              await api('POST', '/auth/register', { ...newUser, age: newUser.age ? Number(newUser.age) : null })
              setNewUser(emptyUser)
            })}>Create</button>
          </div>
        )}
      </section>

      <section className="card">
        <h2>Devices</h2>
        <table>
          <thead><tr><th>Name</th><th>Description</th><th>Max consumption</th><th>Assigned to</th><th /></tr></thead>
          <tbody>
            {devices.map((d) => (
              <tr key={d.id}>
                <td>{d.name}</td><td>{d.description}</td><td>{d.maxConsumption}</td>
                <td>
                  <select
                    value={d.userId || ''}
                    onChange={(e) => run(() => e.target.value
                      ? api('PUT', `/devices/${d.id}/assign/${e.target.value}`)
                      : api('PUT', `/devices/${d.id}/unassign`))}
                  >
                    <option value="">— unassigned —</option>
                    {users.map((u) => <option key={u.id} value={u.id}>{u.username}</option>)}
                  </select>
                  {d.userId && !username(d.userId) && ' (unknown user)'}
                </td>
                <td>
                  <button onClick={() => { setEditDeviceId(d.id); setDevice({ name: d.name, description: d.description || '', maxConsumption: d.maxConsumption }) }}>Edit</button>{' '}
                  <button onClick={() => run(() => api('DELETE', `/devices/${d.id}`))}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="row">
          <b>{editDeviceId ? 'Edit device:' : 'New device:'}</b>
          <input placeholder="name" value={device.name} onChange={(e) => setDevice({ ...device, name: e.target.value })} />
          <input placeholder="description" value={device.description} onChange={(e) => setDevice({ ...device, description: e.target.value })} />
          <input placeholder="max consumption" type="number" value={device.maxConsumption} onChange={(e) => setDevice({ ...device, maxConsumption: e.target.value })} />
          <button onClick={() => run(async () => {
            const body = { ...device, maxConsumption: Number(device.maxConsumption) }
            if (editDeviceId) await api('PUT', `/devices/${editDeviceId}`, body)
            else await api('POST', '/devices', body)
            setDevice(emptyDevice); setEditDeviceId(null)
          })}>{editDeviceId ? 'Save' : 'Create'}</button>
          {editDeviceId && <button onClick={() => { setEditDeviceId(null); setDevice(emptyDevice) }}>Cancel</button>}
        </div>
      </section>
    </>
  )
}
