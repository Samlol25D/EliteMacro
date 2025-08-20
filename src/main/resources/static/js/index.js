// Mostrar usuario logueado y botón admin si corresponde
fetch("/api/usuario")
  .then(res => res.text())
  .then(data => {
    // Mostrar nombre de usuario
    const nombre = data.split(" ")[0]; // ejemplo simple
    document.getElementById("username").innerText = nombre || "Invocador";

    // Mostrar botón solo si el usuario tiene el rol admin
    if (data.includes("ROLE_ADMIN")) {
      document.getElementById("acciones-admin").innerHTML = `
        <a href="admin.html" class="btn-header admin-only" id="acciones-admin" style="display: none;">Panel Admin</a>
      `;
    }
  });

// Obtener hábitos en lista (si usas un <ul id="lista-habitos">)
function cargarHabitos() {
  fetch("/api/habitos")
    .then(res => res.json())
    .then(habitos => {
      const lista = document.getElementById("lista-habitos");
      if (lista) {
        lista.innerHTML = "";
        habitos.forEach(h => {
          const item = document.createElement("li");
          item.textContent = `${h.nombre} - ${h.descripcion} (${h.rol}) - Completado: ${h.completado ? 'Sí' : 'No'}`;
          lista.appendChild(item);
        });
      }
    });
}

// Enviar nuevo hábito
document.getElementById("form-habito")?.addEventListener("submit", function(e) {
  e.preventDefault();

  const nuevoHabito = {
    nombre: document.getElementById("nombre").value,
    descripcion: document.getElementById("descripcion").value,
    rol: document.getElementById("rol").value,
    completado: document.getElementById("completado")?.checked || false
  };

  fetch("/api/habitos", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(nuevoHabito)
  }).then(() => {
    this.reset();
    cargarHabitos();
  });
});

// Mostrar hábitos como tarjetas en el panel
document.addEventListener('DOMContentLoaded', function () {
  fetch('/api/habitos')
    .then(response => response.json())
    .then(data => {
      const container = document.getElementById('habit-container');
      if (!container) return;
      container.innerHTML = '';

      data.forEach(habito => {
        const card = document.createElement('div');
        card.classList.add('habit-card');
        card.innerHTML = `
          <h3>${habito.nombre}</h3>
          <p>${habito.descripcion}</p>
          <span class="frecuencia">${habito.frecuencia || ''}</span>
        `;
        container.appendChild(card);
      });
    })
    .catch(error => {
      console.error('Error al cargar los hábitos:', error);
    });

  cargarHabitos();
});
