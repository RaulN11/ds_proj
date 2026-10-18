let token = null
let listener = () => {}

export const setToken = (t) => { token = t }
export const onLog = (fn) => { listener = fn }

const serviceOf = (url) =>
  url.startsWith('/auth') ? 'auth-service'
  : url.startsWith('/users') ? 'user-service'
  : url.startsWith('/devices') ? 'device-service' : '?'

// Every request goes through here, so the Log panel can show what is called.
export async function api(method, url, body) {
  const t0 = performance.now()
  const res = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  })
  const text = await res.text()
  let data = text
  try { data = text ? JSON.parse(text) : null } catch { /* plain text */ }

  listener({
    time: new Date().toLocaleTimeString(),
    method, url, status: res.status,
    ms: Math.round(performance.now() - t0),
    service: serviceOf(url), body, data,
  })

  if (!res.ok) {
    throw new Error((data && (data.message || data.error)) || (typeof data === 'string' && data) || `HTTP ${res.status}`)
  }
  return data
}
