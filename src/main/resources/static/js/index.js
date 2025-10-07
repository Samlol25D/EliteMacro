// Mostrar usuario logueado (solo nombre)
fetch("/api/usuario")
  .then(res => {
    if (!res.ok) throw new Error('Error al obtener usuario');
    return res.text();
  })
  .then(username => {
    document.getElementById("username").innerText = username || "Invocador";
  })
  .catch(error => {
    console.error('Error:', error);
    document.getElementById("username").innerText = "Invocador";
  });

// Verificar si es admin para mostrar el botón
fetch("/api/usuario-info")
  .then(res => {
    if (!res.ok) return;
    return res.json();
  })
  .then(userInfo => {
    if (userInfo && userInfo.isAdmin) {
      document.getElementById("acciones-admin").style.display = "block";
    }
  })
  .catch(error => {
    console.error('Error al verificar admin:', error);
  });

// Función para agregar hábito
function agregarHabito() {
  const nombre = document.getElementById("nombre").value;
  const descripcion = document.getElementById("descripcion").value;
  const rol = document.getElementById("rol").value;

  if (!nombre || !descripcion) {
    alert('Por favor completa todos los campos');
    return;
  }

  const nuevoHabito = {
    nombre: nombre,
    descripcion: descripcion,
    rol: rol,
    completado: false
  };

  console.log("Enviando hábito:", nuevoHabito);

  fetch("/api/habitos", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(nuevoHabito)
  })
  .then(response => {
    console.log("Respuesta del servidor:", response);
    if (!response.ok) {
      return response.text().then(text => { throw new Error(text) });
    }
    return response.json();
  })
  .then(habitoCreado => {
    console.log("Hábito creado:", habitoCreado);

    // Limpiar formulario
    document.getElementById("nombre").value = '';
    document.getElementById("descripcion").value = '';
    document.getElementById("rol").value = 'TODOS';

    // Recargar la lista de hábitos
    cargarMisHabitos();
    alert('¡Hábito creado exitosamente!');
  })
  .catch(error => {
    console.error('Error completo:', error);
    alert('Error al crear el hábito: ' + error.message);
  });
}

// Función para cargar MIS hábitos (solo los del usuario)
function cargarMisHabitos() {
  console.log("Cargando mis hábitos...");

  fetch("/api/habitos/mis-habitos")
    .then(response => {
      console.log("Respuesta de mis-habitos:", response);
      if (!response.ok) {
        return response.text().then(text => { throw new Error(text) });
      }
      return response.json();
    })
    .then(data => {
      console.log("Hábitos recibidos:", data);
      const container = document.getElementById('habit-container');
      container.innerHTML = '';

      if (!data || data.length === 0) {
        container.innerHTML = '<p class="no-habits">No tienes hábitos registrados. ¡Crea tu primer hábito!</p>';
        return;
      }

      data.forEach(habito => {
        const card = document.createElement('div');
        card.classList.add('habit-card');
        card.innerHTML = `
          <h3>${habito.nombre}</h3>
          <p>${habito.descripcion}</p>
          <span class="rol">Rol: ${habito.rol}</span>
          <span class="estado">${habito.completado ? '✅ Completado' : '⏳ Pendiente'}</span>
          <div class="habit-actions">
            <button onclick="marcarCompletado(${habito.id}, ${!habito.completado})" class="btn-small">
              ${habito.completado ? '❌ Desmarcar' : '✅ Completar'}
            </button>
            <button onclick="eliminarHabito(${habito.id})" class="btn-small btn-danger">🗑️ Eliminar</button>
          </div>
        `;
        container.appendChild(card);
      });
    })
    .catch(error => {
      console.error('Error al cargar los hábitos:', error);
      const container = document.getElementById('habit-container');
      container.innerHTML = '<p class="error">Error al cargar los hábitos: ' + error.message + '</p>';
    });
}

// Función para marcar como completado/pendiente
function marcarCompletado(id, completado) {
  fetch(`/api/habitos/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      completado: completado
      // Mantener los otros campos igual
    })
  })
  .then(response => {
    if (!response.ok) throw new Error('Error al actualizar');
    return response.json();
  })
  .then(() => {
    cargarMisHabitos();
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error al actualizar el hábito');
  });
}

// Función para eliminar hábito
function eliminarHabito(id) {
  if (!confirm('¿Estás seguro de que quieres eliminar este hábito?')) {
    return;
  }

  fetch(`/api/habitos/${id}`, {
    method: "DELETE"
  })
  .then(response => {
    if (!response.ok) throw new Error('Error al eliminar');
    cargarMisHabitos();
    alert('Hábito eliminado correctamente');
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error al eliminar el hábito');
  });
}

// Cargar hábitos cuando la página se carga
document.addEventListener('DOMContentLoaded', function() {
  console.log("Página cargada, iniciando carga de hábitos...");
  cargarMisHabitos();
});