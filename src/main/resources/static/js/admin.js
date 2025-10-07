const api = "/api/admin";

document.addEventListener("DOMContentLoaded", () => {
  cargarUsuarios();
  cargarHabitos();

  document.getElementById("form-habito").addEventListener("submit", function (e) {
    e.preventDefault();

    const habito = {
      nombre: document.getElementById("nombre").value,
      descripcion: document.getElementById("descripcion").value,
      rol: document.getElementById("rol").value, // Corregido - usa el select
      completado: false
    };

    console.log("Enviando hábito:", habito);

    fetch(api + "/habitos", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(habito)
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Error al crear hábito');
      }
      return response.json();
    })
    .then(habitoCreado => {
      alert("Hábito creado exitosamente");
      this.reset();
      cargarHabitos();
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Error al crear el hábito: ' + error.message);
    });
  });
});

function cargarUsuarios() {
  fetch(api + "/usuarios")
    .then(res => {
      if (!res.ok) throw new Error('Error al cargar usuarios');
      return res.json();
    })
    .then(data => {
      const tbody = document.querySelector("#tabla-usuarios tbody");
      tbody.innerHTML = "";
      data.forEach(u => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
          <td>${u.id}</td>
          <td>${u.username}</td>
          <td>${u.rol || 'USER'}</td>
          <td>${u.activo ? 'Sí' : 'No'}</td>
          <td>
            <button class="btn" onclick="cambiarEstado(${u.id}, ${!u.activo})">
              ${u.activo ? 'Desactivar' : 'Activar'}
            </button>
            <button class="btn btn-pdf" onclick="generarReportePDF(${u.id})">
              📊 PDF
            </button>
          </td>`;
        tbody.appendChild(fila);
      });
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Error al cargar usuarios');
    });
}

function cambiarEstado(id, activo) {
  fetch(`${api}/usuarios/${id}/estado?activo=${activo}`, {
    method: "PUT"
  })
  .then(response => {
    if (!response.ok) throw new Error('Error al cambiar estado');
    cargarUsuarios();
    alert('Estado actualizado correctamente');
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error al cambiar estado');
  });
}

// Función para generar reporte PDF
function generarReportePDF(usuarioId) {
  // Por ahora solo alerta - puedes implementar la generación de PDF después
  alert(`Generando reporte PDF para usuario ID: ${usuarioId}`);
  // Aquí puedes redirigir a un endpoint que genere el PDF
  // window.open(`/api/admin/usuarios/${usuarioId}/reporte-pdf`, '_blank');
}

function cargarHabitos() {
  fetch(api + "/habitos")
    .then(res => {
      if (!res.ok) throw new Error('Error al cargar hábitos');
      return res.json();
    })
    .then(data => {
      const tbody = document.querySelector("#tabla-habitos tbody");
      tbody.innerHTML = "";
      data.forEach(h => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
          <td>${h.id}</td>
          <td><input type="text" value="${h.nombre}" id="nombre-${h.id}"></td>
          <td><input type="text" value="${h.descripcion}" id="desc-${h.id}"></td>
          <td>
            <select id="rol-${h.id}">
              <option value="TOP" ${h.rol === 'TOP' ? 'selected' : ''}>Top</option>
              <option value="JUNGLE" ${h.rol === 'JUNGLE' ? 'selected' : ''}>Jungla</option>
              <option value="MID" ${h.rol === 'MID' ? 'selected' : ''}>Mid</option>
              <option value="ADC" ${h.rol === 'ADC' ? 'selected' : ''}>ADC</option>
              <option value="SUPPORT" ${h.rol === 'SUPPORT' ? 'selected' : ''}>Soporte</option>
              <option value="TODOS" ${h.rol === 'TODOS' ? 'selected' : ''}>Todos</option>
            </select>
          </td>
          <td>
            <button class="btn" onclick="editarHabito(${h.id})">Guardar</button>
            <button class="btn btn-danger" onclick="eliminarHabito(${h.id})">Eliminar</button>
          </td>
        `;
        tbody.appendChild(fila);
      });
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Error al cargar hábitos');
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
  })
  .then(response => {
    if (!response.ok) throw new Error('Error al actualizar');
    return response.json();
  })
  .then(() => {
    alert("Hábito actualizado correctamente");
    cargarHabitos();
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error al actualizar hábito');
  });
}

// Función para eliminar hábito
function eliminarHabito(id) {
  if (!confirm('¿Estás seguro de que quieres eliminar este hábito?')) return;

  fetch(`${api}/habitos/${id}`, {
    method: "DELETE"
  })
  .then(response => {
    if (!response.ok) throw new Error('Error al eliminar');
    alert("Hábito eliminado correctamente");
    cargarHabitos();
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error al eliminar hábito');
  });
}