import { useEffect, useState } from 'react'
import { api } from './api'

export default function Client({ session }) {
  const [devices, setDevices] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    api('GET', `/devices/user/${session.id}`).then(setDevices).catch((e) => setError(e.message))
  }, [session.id])

  return (
    <section className="card">
      <h2>My devices</h2>
      {error && <p className="error">{error}</p>}
      <table>
        <thead><tr><th>Name</th><th>Description</th><th>Max consumption</th></tr></thead>
        <tbody>
          {devices.map((d) => (
            <tr key={d.id}><td>{d.name}</td><td>{d.description}</td><td>{d.maxConsumption}</td></tr>
          ))}
        </tbody>
      </table>
      {devices.length === 0 && <p>No devices assigned to you.</p>}
    </section>
  )
}
