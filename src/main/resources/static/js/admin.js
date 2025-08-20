const api = "/api/admin";

document.addEventListener("DOMContentLoaded", () => {
  cargarUsuarios();
  cargarHabitos();

  document.getElementById("form-habito").addEventListener("submit", function (e) {
    e.preventDefault();
    const habito = {
      nombre: document.getElementById("nombre").value,
      descripcion: document.getElementById("descripcion").value,
      rol: document.querySelector('input[name="rol"]:checked').value,
      completado: false
    };

    fetch(api + "/habitos", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(habito)
    }).then(() => {
      alert("Hábito creado");
      this.reset();
      cargarHabitos();
    });
  });
});

function cargarUsuarios() {
  fetch(api + "/usuarios")
    .then(res => res.json())
    .then(data => {
      const tbody = document.querySelector("#tabla-usuarios tbody");
      tbody.innerHTML = "";
      data.forEach(u => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
          <td>${u.id}</td>
          <td>${u.username}</td>
          <td>${u.rol}</td>
          <td>${u.activo ? 'Sí' : 'No'}</td>
          <td>
            <button class="btn" onclick="cambiarEstado(${u.id}, ${!u.activo})">
              ${u.activo ? 'Desactivar' : 'Activar'}
            </button>
          </td>`;
        tbody.appendChild(fila);
      });
    });
}

function cambiarEstado(id, activo) {
  fetch(`${api}/usuarios/${id}/estado?activo=${activo}`, { method: "PUT" })
    .then(() => cargarUsuarios());
}

function cargarHabitos() {
  fetch(api + "/habitos")
    .then(res => res.json())
    .then(data => {
      const tbody = document.querySelector("#tabla-habitos tbody");
      tbody.innerHTML = "";
      data.forEach(h => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
          <td>${h.id}</td>
          <td><input type="text" value="${h.nombre}" id="nombre-${h.id}"></td>
          <td><input type="text" value="${h.descripcion}" id="desc-${h.id}"></td>
          <td><input type="text" value="${h.rol}" id="rol-${h.id}"></td>
          <td><button onclick="editarHabito(${h.id})">Guardar</button></td>
        `;
        tbody.appendChild(fila);
      });
    });
}

function editarHabito(id) {
  const nombre = document.getElementById(`nombre-${id}`).value;
  const descripcion = document.getElementById(`desc-${id}`).value;
  const rol = document.getElementById(`rol-${id}`).value;

  const actualizado = { nombre, descripcion, rol };

  fetch(`${api}/habitos/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(actualizado)
  }).then(() => {
    alert("Hábito actualizado correctamente");
    cargarHabitos();
  });
}
