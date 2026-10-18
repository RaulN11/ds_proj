// Shows every HTTP call the frontend makes and which microservice handled it.
export default function Log({ logs }) {
  return (
    <aside>
      <h2>Request log</h2>
      <small>Calls between services (e.g. auth → user) show up in <code>docker compose logs -f</code>.</small>
      {logs.map((l, i) => (
        <details key={i} className={l.status >= 400 ? 'bad' : 'ok'}>
          <summary>
            <b>{l.method}</b> {l.url} → {l.status} <i>({l.service}, {l.ms}ms)</i>
          </summary>
          <pre>{JSON.stringify({ request: l.body ? { ...l.body, password: l.body.password ? '***' : undefined } : undefined, response: l.data }, null, 2)}</pre>
        </details>
      ))}
    </aside>
  )
}
