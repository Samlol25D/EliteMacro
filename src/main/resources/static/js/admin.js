const api = "/api/admin";

// Inicializar jsPDF
const { jsPDF } = window.jspdf;

// Variable global para almacenar todos los hábitos
let todosLosHabitos = [];

document.addEventListener("DOMContentLoaded", () => {
  cargarUsuarios();
  cargarHabitos();

  document.getElementById("form-habito").addEventListener("submit", function (e) {
    e.preventDefault();

    const habito = {
      nombre: document.getElementById("nombre").value,
      descripcion: document.getElementById("descripcion").value,
      rol: document.getElementById("rol").value,
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
      cargarHabitos(); // Recargar todos los hábitos
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

      if (data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center;">No hay usuarios registrados</td></tr>`;
        return;
      }

      data.forEach(u => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
          <td>${u.id}</td>
          <td>${u.username}</td>
          <td>${u.rol || 'USER'}</td>
          <td>${u.activo ? 'Sí' : 'No'}</td>
          <td class="acciones">
            <button class="btn" onclick="cambiarEstado(${u.id}, ${!u.activo})">
              ${u.activo ? 'Desactivar' : 'Activar'}
            </button>
            <button class="btn btn-pdf" onclick="generarReporteUsuario(${u.id}, '${u.username}')">
              📊 Reporte Individual
            </button>
          </td>`;
        tbody.appendChild(fila);
      });
    })
    .catch(error => {
      console.error('Error:', error);
      const tbody = document.querySelector("#tabla-usuarios tbody");
      tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #ff6b6b;">Error al cargar usuarios</td></tr>`;
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

// Función para generar reporte general de todos los usuarios
async function generarReporteGeneral() {
  try {
    const response = await fetch(api + "/usuarios");
    if (!response.ok) throw new Error('Error al cargar usuarios');
    const usuarios = await response.json();

    const doc = new jsPDF();

    // Encabezado del reporte
    doc.setFontSize(20);
    doc.setTextColor(0, 191, 255);
    doc.text('Reporte General de Usuarios - EliteMacro', 20, 30);

    // Información de la empresa
    doc.setFontSize(12);
    doc.setTextColor(100, 100, 100);
    doc.text('Fecha: ' + new Date().toLocaleDateString(), 20, 45);
    doc.text('Total de usuarios: ' + usuarios.length, 20, 55);

    // Tabla de usuarios
    const tableColumn = ["ID", "Usuario", "Rol", "Activo"];
    const tableRows = [];

    usuarios.forEach(usuario => {
      const usuarioData = [
        usuario.id.toString(),
        usuario.username,
        usuario.rol || 'USER',
        usuario.activo ? 'Sí' : 'No'
      ];
      tableRows.push(usuarioData);
    });

    doc.autoTable({
      head: [tableColumn],
      body: tableRows,
      startY: 70,
      theme: 'grid',
      styles: {
        fontSize: 10,
        cellPadding: 3,
      },
      headStyles: {
        fillColor: [0, 191, 255],
        textColor: 255,
        fontStyle: 'bold'
      },
      alternateRowStyles: {
        fillColor: [240, 240, 240]
      }
    });

    // Estadísticas
    const finalY = doc.lastAutoTable.finalY + 15;
    doc.setFontSize(14);
    doc.setTextColor(0, 0, 0);
    doc.text('Estadísticas:', 20, finalY);

    const usuariosActivos = usuarios.filter(u => u.activo).length;
    const usuariosInactivos = usuarios.length - usuariosActivos;

    doc.setFontSize(10);
    doc.text(`• Usuarios activos: ${usuariosActivos}`, 20, finalY + 10);
    doc.text(`• Usuarios inactivos: ${usuariosInactivos}`, 20, finalY + 20);
    doc.text(`• Porcentaje de actividad: ${((usuariosActivos / usuarios.length) * 100).toFixed(1)}%`, 20, finalY + 30);

    // Pie de página
    doc.setFontSize(8);
    doc.setTextColor(150, 150, 150);
    doc.text('Generado automáticamente por EliteMacro Admin Panel', 20, doc.internal.pageSize.height - 10);

    // Guardar el PDF
    doc.save(`reporte_general_usuarios_${new Date().toISOString().split('T')[0]}.pdf`);

  } catch (error) {
    console.error('Error:', error);
    alert('Error al generar el reporte: ' + error.message);
  }
}

// Función para generar reporte individual de usuario
async function generarReporteUsuario(usuarioId, username) {
  try {
    const userResponse = await fetch(`${api}/usuarios/${usuarioId}`);
    if (!userResponse.ok) throw new Error('Error al cargar datos del usuario');
    const usuario = await userResponse.json();

    let habitos = [];
    try {
      const habitosResponse = await fetch(`${api}/usuarios/${usuarioId}/habitos`);
      if (habitosResponse.ok) {
        habitos = await habitosResponse.json();
      }
    } catch (e) {
      console.log('No se pudieron cargar los hábitos del usuario');
    }

    const doc = new jsPDF();

    // Encabezado del reporte
    doc.setFontSize(20);
    doc.setTextColor(0, 191, 255);
    doc.text(`Reporte de Usuario - ${username}`, 20, 30);

    // Información del usuario
    doc.setFontSize(12);
    doc.setTextColor(0, 0, 0);
    doc.text(`ID: ${usuario.id}`, 20, 50);
    doc.text(`Usuario: ${usuario.username}`, 20, 60);
    doc.text(`Rol: ${usuario.rol || 'USER'}`, 20, 70);
    doc.text(`Estado: ${usuario.activo ? 'Activo' : 'Inactivo'}`, 20, 80);
    doc.text(`Fecha de registro: ${new Date().toLocaleDateString()}`, 20, 90);

    if (habitos.length > 0) {
      doc.setFontSize(14);
      doc.text('Hábitos del Usuario:', 20, 110);

      const tableColumn = ["Hábito", "Descripción", "Rol", "Completado"];
      const tableRows = [];

      habitos.forEach(habito => {
        const habitoData = [
          habito.nombre,
          habito.descripcion,
          habito.rol,
          habito.completado ? 'Sí' : 'No'
        ];
        tableRows.push(habitoData);
      });

      doc.autoTable({
        head: [tableColumn],
        body: tableRows,
        startY: 120,
        theme: 'grid',
        styles: {
          fontSize: 8,
          cellPadding: 2,
        },
        headStyles: {
          fillColor: [0, 191, 255],
          textColor: 255,
          fontStyle: 'bold'
        }
      });
    } else {
      doc.setFontSize(12);
      doc.text('No hay hábitos registrados para este usuario.', 20, 110);
    }

    const finalY = habitos.length > 0 ? doc.lastAutoTable.finalY + 15 : 120;
    doc.setFontSize(14);
    doc.text('Estadísticas:', 20, finalY);

    const habitosCompletados = habitos.filter(h => h.completado).length;
    const totalHabitos = habitos.length;
    const porcentajeCompletado = totalHabitos > 0 ? (habitosCompletados / totalHabitos * 100).toFixed(1) : 0;

    doc.setFontSize(10);
    doc.text(`• Hábitos asignados: ${totalHabitos}`, 20, finalY + 10);
    doc.text(`• Hábitos completados: ${habitosCompletados}`, 20, finalY + 20);
    doc.text(`• Porcentaje de completado: ${porcentajeCompletado}%`, 20, finalY + 30);

    doc.setFontSize(8);
    doc.setTextColor(150, 150, 150);
    doc.text('Generado automáticamente por EliteMacro Admin Panel', 20, doc.internal.pageSize.height - 10);

    doc.save(`reporte_${username}_${new Date().toISOString().split('T')[0]}.pdf`);

  } catch (error) {
    console.error('Error:', error);
    alert('Error al generar el reporte del usuario: ' + error.message);
  }
}

function cargarHabitos() {
  fetch(api + "/habitos")
    .then(res => {
      if (!res.ok) throw new Error('Error al cargar hábitos');
      return res.json();
    })
    .then(data => {
      // Guardar todos los hábitos en variable global
      todosLosHabitos = data;
      // Aplicar filtro actual
      aplicarFiltroHabitos();
    })
    .catch(error => {
      console.error('Error:', error);
      const tbody = document.querySelector("#tabla-habitos tbody");
      tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #ff6b6b;">Error al cargar hábitos</td></tr>`;
    });
}

// Función para filtrar hábitos por rol
function filtrarHabitos() {
  aplicarFiltroHabitos();
}

// Función para aplicar el filtro actual
function aplicarFiltroHabitos() {
  const filtroRol = document.getElementById("filtro-rol-habitos").value;
  const habitosFiltrados = filtrarHabitosPorRol(todosLosHabitos, filtroRol);
  mostrarHabitosEnTabla(habitosFiltrados);
}

// Función para filtrar hábitos por rol
function filtrarHabitosPorRol(habitos, rolFiltro) {
  if (rolFiltro === "TODOS") {
    return habitos;
  }
  return habitos.filter(habito => habito.rol === rolFiltro);
}

// Función para mostrar hábitos en la tabla
function mostrarHabitosEnTabla(habitos) {
  const tbody = document.querySelector("#tabla-habitos tbody");
  tbody.innerHTML = "";

  if (habitos.length === 0) {
    const filtroRol = document.getElementById("filtro-rol-habitos").value;
    const mensaje = filtroRol === "TODOS"
      ? "No hay hábitos registrados"
      : `No hay hábitos para el rol ${filtroRol}`;

    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center;">${mensaje}</td></tr>`;
    return;
  }

  habitos.forEach(h => {
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
      <td class="acciones">
        <button class="btn" onclick="editarHabito(${h.id})">Guardar</button>
        <button class="btn btn-danger" onclick="eliminarHabito(${h.id})">Eliminar</button>
      </td>
    `;
    tbody.appendChild(fila);
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
      cargarHabitos(); // Recargar para actualizar el filtro si es necesario
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Error al actualizar hábito');
    });
}

function eliminarHabito(id) {
  if (!confirm('¿Estás seguro de que quieres eliminar este hábito?')) return;

  fetch(`${api}/habitos/${id}`, {
    method: "DELETE"
  })
    .then(response => {
      if (!response.ok) throw new Error('Error al eliminar');
      alert("Hábito eliminado correctamente");
      cargarHabitos(); // Recargar para actualizar el filtro
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Error al eliminar hábito');
    });
}
