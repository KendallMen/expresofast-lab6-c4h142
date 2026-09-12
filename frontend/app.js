const API_BASE = "http://localhost:8080/api";
let envios = [];
let filtroActual = "TODOS";
let historialActual = [];

// --- Protección de la página ---
if (!localStorage.getItem("jwt_token")) {
  window.location.href = "login.html";
}

document.getElementById("usuarioActivo").textContent =
  localStorage.getItem("username") || "";

document.getElementById("btnLogout").addEventListener("click", () => {
  localStorage.removeItem("jwt_token");
  localStorage.removeItem("username");
  localStorage.removeItem("roles");
  window.location.href = "login.html";
});

// --- Utilidades de rol ---
function getRoles() {
  return JSON.parse(localStorage.getItem("roles") || "[]");
}

function tieneRol(rol) {
  return getRoles().includes(rol);
}

// --- Fetch con Authorization header + manejo de expiración ---
async function fetchWithAuth(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${localStorage.getItem("jwt_token")}`,
    ...(options.headers || {})
  };

  const res = await fetch(url, { ...options, headers });

  if (res.status === 401 || res.status === 403) {
    localStorage.removeItem("jwt_token");
    localStorage.removeItem("username");
    localStorage.removeItem("roles");
    window.location.href = "login.html";
    throw new Error("Sesión expirada");
  }

  return res;
}

// --- Ocultar UI según rol ---
function aplicarPermisosUI() {
  const formEnvioSection = document.getElementById("formEnvio")?.closest("section");
  const tabFlota = document.getElementById("tabFlota");

  const soloConductor = tieneRol("ROLE_CONDUCTOR")
    && !tieneRol("ROLE_ADMIN")
    && !tieneRol("ROLE_OPERADOR");

  if (soloConductor) {
    if (formEnvioSection) formEnvioSection.style.display = "none";
  }

  if (tabFlota && !tieneRol("ROLE_ADMIN")) {
    tabFlota.style.display = "none";
  }
}

// --- Tablero de envíos ---
async function cargarEnvios() {
  try {
    const res = await fetchWithAuth(`${API_BASE}/envios/optimizados`);
    if (!res.ok) throw new Error("No se pudieron cargar los envíos");
    envios = await res.json();
    renderizarTablero();
  } catch (error) {
    document.getElementById("tablero").innerHTML = `<p class="error-msg">${error.message}</p>`;
  }
}

function renderizarTablero() {
  const tablero = document.getElementById("tablero");
  const visibles = filtroActual === "TODOS"
    ? envios
    : envios.filter(e => e.estadoEnvio === filtroActual);

  document.getElementById("contadorEnvios").textContent = `${visibles.length} envíos`;

  const mostrarBitacora = tieneRol("ROLE_ADMIN") || tieneRol("ROLE_OPERADOR");
  const puedeCambiarEstado = tieneRol("ROLE_ADMIN") || tieneRol("ROLE_CONDUCTOR");

  tablero.innerHTML = visibles.map(e => `
    <article class="tarjeta-envio" data-id="${e.id}">
      <h3>${e.codigoRastreo}</h3>
      <p>${e.direccionDestino}</p>
      <p>${e.pesoKg} kg — ₡${e.costo}</p>
      <p>Vehículo: ${e.placaVehiculo} · Empresa: ${e.nombreEmpresa}</p>
      <p>Conductor: ${e.nombreConductor}</p>
      <span class="pill-status ${e.estadoEnvio}">${e.estadoEnvio}</span>
      <div class="acciones">
        ${puedeCambiarEstado ? `<button onclick="cambiarEstado(${e.id}, 'EN_TRANSITO')">Marcar en Tránsito</button>
        <button onclick="cambiarEstado(${e.id}, 'ENTREGADO')">Marcar Entregado</button>` : ""}
        ${mostrarBitacora ? `<button onclick="verBitacora(${e.id})">Ver Bitácora</button>` : ""}
      </div>
    </article>
  `).join("");
}

async function cambiarEstado(id, nuevoEstado) {
  const res = await fetchWithAuth(`${API_BASE}/envios/${id}/estado`, {
    method: "PATCH",
    body: JSON.stringify({ nuevoEstado, observaciones: "" })
  });

  if (res.ok) {
    cargarEnvios();
  } else {
    const error = await res.json();
    alert(error.mensaje || "No se pudo actualizar el estado");
  }
}

// --- Registro de nuevo envío ---
document.getElementById("formEnvio").addEventListener("submit", async (ev) => {
  ev.preventDefault();

  const payload = {
    codigoRastreo: document.getElementById("codigoRastreo").value,
    direccionDestino: document.getElementById("direccionDestino").value,
    pesoKg: parseFloat(document.getElementById("pesoKg").value),
    costo: parseFloat(document.getElementById("costo").value),
    vehiculoId: parseInt(document.getElementById("vehiculoId").value),
    conductorId: parseInt(document.getElementById("conductorId").value)
  };

  const res = await fetchWithAuth(`${API_BASE}/envios`, {
    method: "POST",
    body: JSON.stringify(payload)
  });

  const errorMsg = document.getElementById("errorFormEnvio");

  if (res.ok) {
    errorMsg.textContent = "";
    ev.target.reset();
    cargarEnvios();
  } else {
    const error = await res.json();
    errorMsg.textContent = error.mensaje || Object.values(error.errores || {}).join(" ");
  }
});

// --- Filtros de estado del tablero ---
document.getElementById("filtros").addEventListener("click", (ev) => {
  if (ev.target.tagName !== "BUTTON") return;
  document.querySelectorAll("#filtros button").forEach(b => b.classList.remove("activo"));
  ev.target.classList.add("activo");
  filtroActual = ev.target.dataset.filtro;
  renderizarTablero();
});

// --- Bitácora de auditoría ---
async function verBitacora(envioId) {
  const res = await fetchWithAuth(`${API_BASE}/envios/${envioId}/bitacora`);
  historialActual = await res.json();

  // Limpiar filtros de fecha al abrir un nuevo envío
  document.getElementById("fechaDesde").value = "";
  document.getElementById("fechaHasta").value = "";

  pintarBitacora(historialActual);
  document.getElementById("modalBitacora").style.display = "flex";
}

function pintarBitacora(lista) {
  const contenido = document.getElementById("bitacoraContenido");

  contenido.innerHTML = lista.map(h => `
    <div class="fila-bitacora">
      <strong>${h.estadoAnterior} → ${h.estadoNuevo}</strong>
      <span>${new Date(h.fechaCambio).toLocaleString()}</span>
      <span>Usuario: ${h.usuario}</span>
      ${h.observaciones ? `<p>${h.observaciones}</p>` : ""}
    </div>
  `).join("") || "<p>Sin historial registrado en este rango.</p>";
}

function filtrarBitacoraPorFecha() {
  const desde = document.getElementById("fechaDesde").value;
  const hasta = document.getElementById("fechaHasta").value;

  const filtrado = historialActual.filter(h => {
    const fecha = h.fechaCambio.substring(0, 10); // "2026-09-11T14:30:00" -> "2026-09-11"
    return (!desde || fecha >= desde) && (!hasta || fecha <= hasta);
  });

  pintarBitacora(filtrado);
}

document.getElementById("fechaDesde").addEventListener("change", filtrarBitacoraPorFecha);
document.getElementById("fechaHasta").addEventListener("change", filtrarBitacoraPorFecha);

document.getElementById("cerrarModal").addEventListener("click", () => {
  document.getElementById("modalBitacora").style.display = "none";
});

// --- Inicio ---
aplicarPermisosUI();
cargarEnvios();