/* Energy Management System frontend — plain JS, no build step.
   Talks to the Traefik gateway on the same origin:
   /auth/login, /auth/register, /users, /devices */

const UNIT = "kW"; // unit shown next to a device's max consumption
const API = ""; // same origin (Traefik). Set to "http://localhost" to run the files from elsewhere.

const $app = document.getElementById("app");
const $dialog = document.getElementById("dialog");

/* ---------- helpers ---------- */
const esc = (s) => String(s ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const fmt = (n) => Number(n).toLocaleString(undefined, { maximumFractionDigits: 2 });

const session = {
  get() { try { return JSON.parse(localStorage.getItem("ww_session")); } catch { return null; } },
  set(v) { try { localStorage.setItem("ww_session", JSON.stringify(v)); } catch {} },
  clear() { try { localStorage.removeItem("ww_session"); } catch {} },
};

function toast(msg, isErr = false) {
  const el = document.createElement("div");
  el.className = "toast" + (isErr ? " err" : "");
  el.textContent = msg;
  document.getElementById("toasts").appendChild(el);
  setTimeout(() => el.remove(), 3800);
}

async function api(path, { method = "GET", body, auth = true } = {}) {
  const headers = {};
  if (body !== undefined) headers["Content-Type"] = "application/json";
  const s = session.get();
  if (auth && s) headers.Authorization = "Bearer " + s.token;
  let res;
  try {
    res = await fetch(API + path, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined });
  } catch {
    throw new Error("Can't reach the server. Check that the services are running.");
  }
  if (res.status === 401 && auth) { session.clear(); state.user = null; render(); throw new Error("Your session expired. Sign in again."); }
  if (res.status === 403) throw new Error("You don't have permission to do that.");
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if (!res.ok) throw new Error((typeof data === "string" && data) || "Something went wrong (" + res.status + ").");
  return data;
}

/* ---------- state ---------- */
const state = { user: session.get(), mode: "login", tab: "devices", devices: null, users: null };

/* ---------- dialogs ---------- */
function openDialog(html) {
  $dialog.innerHTML = `<div class="dlg">${html}</div>`;
  if (!$dialog.open) $dialog.showModal();
  const first = $dialog.querySelector("input, select, textarea");
  if (first) first.focus();
}
function closeDialog() { if ($dialog.open) $dialog.close(); }
$dialog.addEventListener("click", (e) => { if (e.target === $dialog) closeDialog(); });

function confirmDialog({ title, text, action, onConfirm }) {
  openDialog(`<h2>${esc(title)}</h2><p>${esc(text)}</p>
    <div class="dlg-actions"><button class="btn ghost" data-close>Cancel</button><button class="btn danger" id="ok">${esc(action)}</button></div>`);
  $dialog.querySelector("[data-close]").onclick = closeDialog;
  $dialog.querySelector("#ok").onclick = async (e) => {
    e.target.disabled = true;
    try { await onConfirm(); closeDialog(); } catch (err) { toast(err.message, true); e.target.disabled = false; }
  };
}

/* ---------- auth screen ---------- */
function authView() {
  const reg = state.mode === "register";
  $app.innerHTML = `
  <div class="auth">
    <section class="auth-main">
      <form class="auth-card" id="authForm" novalidate>
        <div class="brand auth-brand"><span class="brand-mark"><i></i><i></i><i></i></span>Energy Management System</div>
        <h2>${reg ? "Create your account" : "Sign in"}</h2>
        <p class="sub">${reg ? "It takes a minute. You can sign in right after." : "Use the username and password you registered with."}</p>
        <label class="field"><span>Username</span><input name="username" autocomplete="username" required></label>
        <label class="field"><span>Password</span><input name="password" type="password" autocomplete="${reg ? "new-password" : "current-password"}" required></label>
        ${reg ? `
        <label class="field"><span>Full name</span><input name="name" autocomplete="name"></label>
        <div class="row2">
          <label class="field"><span>Email</span><input name="email" type="email" autocomplete="email"></label>
          <label class="field"><span>Age</span><input name="age" type="number" min="0" max="130"></label>
        </div>
        <div class="field"><span>Account type</span>
          <div class="seg" role="radiogroup" aria-label="Account type">
            <label><input type="radio" name="role" value="CLIENT" checked><span>Client</span></label>
            <label><input type="radio" name="role" value="ADMIN"><span>Admin</span></label>
          </div>
        </div>` : ""}
        <p class="error" id="authErr" role="alert"></p>
        <button class="btn" style="width:100%" type="submit">${reg ? "Create account" : "Sign in"}</button>
        <p class="switch">${reg ? "Already have an account?" : "New here?"}
          <button type="button" id="swap">${reg ? "Sign in" : "Create an account"}</button></p>
      </form>
    </section>
  </div>`;

  document.getElementById("swap").onclick = () => { state.mode = reg ? "login" : "register"; render(); };
  document.getElementById("authForm").onsubmit = async (e) => {
    e.preventDefault();
    const f = Object.fromEntries(new FormData(e.target));
    const err = document.getElementById("authErr");
    const btn = e.target.querySelector("button[type=submit]");
    err.textContent = "";
    if (!f.username?.trim() || !f.password) { err.textContent = "Enter a username and password."; return; }
    btn.disabled = true;
    try {
      if (reg) {
        await api("/auth/register", { method: "POST", auth: false, body: {
          username: f.username.trim(), password: f.password, role: f.role,
          name: f.name || null, email: f.email || null, age: f.age ? Number(f.age) : null } });
        toast("Account created");
      }
      const out = await api("/auth/login", { method: "POST", auth: false, body: { username: f.username.trim(), password: f.password } });
      session.set(out); state.user = out; state.tab = "devices"; state.devices = null; state.users = null;
      render();
    } catch (ex) {
      err.textContent = ex.message === "Invalid credentials" ? "That username and password don't match." : ex.message;
      btn.disabled = false;
    }
  };
}

/* ---------- shell ---------- */
function shell(inner, isAdmin) {
  const u = state.user;
  $app.innerHTML = `
  <header class="topbar"><div class="topbar-in">
    <div class="brand"><span class="brand-mark"><i></i><i></i><i></i></span>Energy Management System</div>
    ${isAdmin ? `<nav class="tabs" role="tablist">
      <button class="tab" role="tab" data-tab="devices" aria-selected="${state.tab === "devices"}">Devices</button>
      <button class="tab" role="tab" data-tab="users" aria-selected="${state.tab === "users"}">Users</button></nav>` : ""}
    <div class="who"><span>${esc(u.username)}</span><span class="badge ${isAdmin ? "admin" : ""}">${isAdmin ? "Admin" : "Client"}</span>
      <button class="btn ghost small" id="logout">Sign out</button></div>
  </div></header>
  <div class="page" id="page">${inner}</div>`;
  document.getElementById("logout").onclick = () => { session.clear(); state.user = null; state.mode = "login"; render(); };
  $app.querySelectorAll(".tab").forEach((t) => (t.onclick = () => { state.tab = t.dataset.tab; render(); }));
}
const skeleton = `<div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div>`;

/* ---------- client view ---------- */
async function clientView() {
  shell(`<div class="page-head"><div><h2>Your devices</h2><p>Devices assigned to you by an admin.</p></div></div><div id="body">${skeleton}</div>`, false);
  try {
    state.devices = await api("/devices");
    document.getElementById("body").innerHTML = state.devices.length
      ? `<div class="grid">${state.devices.map((d) => `
        <article class="device">
          <div><h3>${esc(d.name)}</h3>${d.description ? `<div class="desc">${esc(d.description)}</div>` : ""}</div>
          <div class="meter-label">${fmt(d.maxConsumption)}<small>${UNIT} max</small></div>
        </article>`).join("")}</div>`
      : `<div class="empty"><h3>No devices yet</h3><p>When an admin assigns a device to you, it will show up here.</p></div>`;
  } catch (e) { toast(e.message, true); }
}

/* ---------- admin: devices ---------- */
function deviceForm(d) {
  const edit = !!d;
  openDialog(`<h2>${edit ? "Edit device" : "Add a device"}</h2>
    <form id="df" novalidate>
      <label class="field"><span>Name</span><input name="name" required value="${esc(d?.name)}"></label>
      <label class="field"><span>Description</span><textarea name="description" rows="2">${esc(d?.description)}</textarea></label>
      <label class="field"><span>Max consumption (${UNIT})</span><input name="max" type="number" min="0" step="any" required value="${d ? esc(d.maxConsumption) : ""}"></label>
      <p class="error" id="dfErr" role="alert"></p>
      <div class="dlg-actions"><button type="button" class="btn ghost" data-close>Cancel</button><button class="btn" type="submit">${edit ? "Save changes" : "Add device"}</button></div>
    </form>`);
  $dialog.querySelector("[data-close]").onclick = closeDialog;
  $dialog.querySelector("#df").onsubmit = async (e) => {
    e.preventDefault();
    const f = Object.fromEntries(new FormData(e.target));
    const err = $dialog.querySelector("#dfErr");
    if (!f.name.trim()) { err.textContent = "Give the device a name."; return; }
    if (f.max === "" || Number(f.max) < 0) { err.textContent = "Enter a max consumption of 0 or more."; return; }
    const btn = e.target.querySelector("button[type=submit]"); btn.disabled = true;
    try {
      await api(edit ? "/devices/" + d.id : "/devices", { method: edit ? "PUT" : "POST",
        body: { name: f.name.trim(), description: f.description.trim() || null, maxConsumption: Number(f.max) } });
      closeDialog(); toast(edit ? "Changes saved" : "Device added"); render();
    } catch (ex) { err.textContent = ex.message; btn.disabled = false; }
  };
}

async function adminDevices() {
  shell(`<div class="page-head"><div><h2>Devices</h2><p>Add devices and assign them to users.</p></div>
    <button class="btn" id="add">Add device</button></div><div id="body">${skeleton}</div>`, true);
  document.getElementById("add").onclick = () => deviceForm();
  try {
    [state.devices, state.users] = await Promise.all([api("/devices"), api("/users")]);
  } catch (e) { toast(e.message, true); return; }
  const body = document.getElementById("body");
  if (!state.devices.length) {
    body.innerHTML = `<div class="empty"><h3>No devices yet</h3><p>Add your first device, then assign it to a user.</p><button class="btn" id="add2">Add device</button></div>`;
    document.getElementById("add2").onclick = () => deviceForm();
    return;
  }
  const opts = (sel) => `<option value="">Unassigned</option>` + state.users.map((u) =>
    `<option value="${esc(u.id)}" ${u.id === sel ? "selected" : ""}>${esc(u.name || u.username)} (${esc(u.username)})</option>`).join("");
  body.innerHTML = `<div class="list">${state.devices.map((d) => `
    <div class="item" data-id="${esc(d.id)}">
      <div><h3>${esc(d.name)}</h3>${d.description ? `<div class="desc">${esc(d.description)}</div>` : ""}</div>
      <div class="meter-label">${fmt(d.maxConsumption)}<small>${UNIT} max</small></div>
      <label><span class="sr">Assigned to</span><select data-assign>${opts(d.userId)}</select></label>
      <div class="actions"><button class="btn ghost small" data-edit>Edit</button><button class="btn danger small" data-del>Delete</button></div>
    </div>`).join("")}</div>`;
  body.querySelectorAll(".item").forEach((row) => {
    const d = state.devices.find((x) => x.id === row.dataset.id);
    row.querySelector("[data-edit]").onclick = () => deviceForm(d);
    row.querySelector("[data-del]").onclick = () => confirmDialog({
      title: "Delete " + d.name + "?", text: "This removes the device for everyone. You can't undo it.", action: "Delete device",
      onConfirm: async () => { await api("/devices/" + d.id, { method: "DELETE" }); toast("Device deleted"); render(); } });
    row.querySelector("[data-assign]").onchange = async (e) => {
      const uid = e.target.value;
      try {
        await api("/devices/" + d.id + (uid ? "/assign/" + uid : "/unassign"), { method: "PUT" });
        d.userId = uid || null;
        toast(uid ? "Device assigned" : "Device unassigned");
      } catch (ex) { toast(ex.message, true); e.target.value = d.userId || ""; }
    };
  });
}

/* ---------- admin: users ---------- */
function userForm(u) {
  openDialog(`<h2>Edit ${esc(u.username)}</h2>
    <form id="uf" novalidate>
      <label class="field"><span>Full name</span><input name="name" value="${esc(u.name)}"></label>
      <div class="row2">
        <label class="field"><span>Email</span><input name="email" type="email" value="${esc(u.email)}"></label>
        <label class="field"><span>Age</span><input name="age" type="number" min="0" max="130" value="${esc(u.age)}"></label>
      </div>
      <p class="error" id="ufErr" role="alert"></p>
      <div class="dlg-actions"><button type="button" class="btn ghost" data-close>Cancel</button><button class="btn" type="submit">Save changes</button></div>
    </form>`);
  $dialog.querySelector("[data-close]").onclick = closeDialog;
  $dialog.querySelector("#uf").onsubmit = async (e) => {
    e.preventDefault();
    const f = Object.fromEntries(new FormData(e.target));
    const btn = e.target.querySelector("button[type=submit]"); btn.disabled = true;
    try {
      await api("/users/" + u.id, { method: "PUT", body: { ...u, name: f.name || null, email: f.email || null, age: f.age ? Number(f.age) : null } });
      closeDialog(); toast("Changes saved"); render();
    } catch (ex) { $dialog.querySelector("#ufErr").textContent = ex.message; btn.disabled = false; }
  };
}

async function adminUsers() {
  shell(`<div class="page-head"><div><h2>Users</h2><p>People can create their own account from the sign-in page.</p></div></div><div id="body">${skeleton}</div>`, true);
  try { state.users = await api("/users"); } catch (e) { toast(e.message, true); return; }
  const me = state.user.id;
  const body = document.getElementById("body");
  if (!state.users.length) { body.innerHTML = `<div class="empty"><h3>No users yet</h3><p>Accounts appear here after someone registers.</p></div>`; return; }
  body.innerHTML = `<div class="scroll"><table class="table"><thead><tr><th>Name</th><th>Username</th><th>Email</th><th>Age</th><th>Role</th><th></th></tr></thead><tbody>
    ${state.users.map((u) => `<tr data-id="${esc(u.id)}">
      <td>${esc(u.name) || "—"}</td><td>${esc(u.username)}</td><td>${esc(u.email) || "—"}</td><td>${u.age ?? "—"}</td>
      <td><span class="badge ${u.role === "ADMIN" ? "admin" : ""}">${u.role === "ADMIN" ? "Admin" : "Client"}</span></td>
      <td><div class="actions"><button class="btn ghost small" data-edit>Edit</button>
        ${u.id === me ? "" : `<button class="btn danger small" data-del>Delete</button>`}</div></td></tr>`).join("")}
  </tbody></table></div>`;
  body.querySelectorAll("tr[data-id]").forEach((row) => {
    const u = state.users.find((x) => x.id === row.dataset.id);
    row.querySelector("[data-edit]").onclick = () => userForm(u);
    const del = row.querySelector("[data-del]");
    if (del) del.onclick = () => confirmDialog({
      title: "Delete " + u.username + "?", text: "Their account and sign-in are removed. You can't undo it.", action: "Delete user",
      onConfirm: async () => { await api("/users/" + u.id, { method: "DELETE" }); toast("User deleted"); render(); } });
  });
}

/* ---------- router ---------- */
function render() {
  closeDialog();
  if (!state.user) return authView();
  if (state.user.role === "ADMIN") return state.tab === "users" ? adminUsers() : adminDevices();
  return clientView();
}
render();
