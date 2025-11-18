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

// Función para calcular puntos de experiencia según dificultad
function calcularPuntosExperiencia(dificultad) {
  switch(dificultad) {
    case 'BAJA': return 10;
    case 'MEDIA': return 20;
    case 'ALTA': return 30;
    default: return 20;
  }
}

// Función para agregar hábito
function agregarHabito() {
  const nombre = document.getElementById("nombre").value;
  const descripcion = document.getElementById("descripcion").value;
  const rol = document.getElementById("rol").value;
  const dificultad = document.getElementById("dificultad").value;

  if (!nombre || !descripcion) {
    alert('Por favor completa todos los campos');
    return;
  }

  const puntosExperiencia = calcularPuntosExperiencia(dificultad);

  const nuevoHabito = {
    nombre: nombre,
    descripcion: descripcion,
    rol: rol,
    dificultad: dificultad,
    puntosExperiencia: puntosExperiencia,
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
    document.getElementById("dificultad").value = 'MEDIA';

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
          <div class="habit-details">
            <span class="rol">Rol: ${habito.rol}</span>
            <span class="dificultad ${habito.dificultad?.toLowerCase() || 'media'}">
              Dificultad: ${habito.dificultad || 'MEDIA'}
            </span>
            <span class="experiencia">${habito.puntosExperiencia || 20} XP</span>
          </div>
          <span class="estado ${habito.completado ? 'completado' : 'pendiente'}">
            ${habito.completado ? '✅ Completado' : '⏳ Pendiente'}
          </span>
          <div class="habit-actions">
            <button onclick="marcarCompletado(${habito.id}, ${!habito.completado})" class="btn-small ${habito.completado ? 'btn-warning' : 'btn-success'}">
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

// Función para actualizar el círculo de progreso en el header
function actualizarProgresoHeader(porcentaje) {
    const circle = document.querySelector('.progress-ring-fill');
    const circumference = 2 * Math.PI * 36; // 2πr donde r=36
    const offset = circumference - (porcentaje / 100) * circumference;

    if (circle) {
        circle.style.strokeDasharray = `${circumference} ${circumference}`;
        circle.style.strokeDashoffset = offset;
    }
}

// Función para cargar las estadísticas del usuario
function cargarEstadisticasUsuario() {
  fetch("/api/usuario/estadisticas")
    .then(response => {
      if (!response.ok) throw new Error('Error al cargar estadísticas');
      return response.json();
    })
    .then(stats => {
      console.log("Estadísticas recibidas:", stats);

      // Actualizar nivel y rango en el panel principal
      if (document.getElementById('user-level-large')) {
        document.getElementById('user-level-large').textContent = stats.nivel;
      }

      if (document.getElementById('user-rank')) {
        document.getElementById('user-rank').textContent = stats.rango;
        document.getElementById('user-rank').className = `rank-badge ${stats.rango.toLowerCase()}`;
      }

      // Actualizar nivel en el header
      if (document.getElementById('user-level')) {
        document.getElementById('user-level').textContent = stats.nivel;
      }

      // Actualizar barra de progreso
      if (document.getElementById('current-exp')) {
        document.getElementById('current-exp').textContent = stats.experienciaActual;
      }

      if (document.getElementById('next-level-exp')) {
        document.getElementById('next-level-exp').textContent = stats.experienciaParaSiguienteNivel;
      }

      if (document.getElementById('progress-percent')) {
        document.getElementById('progress-percent').textContent = stats.progresoNivel;
      }

      if (document.getElementById('next-level')) {
        document.getElementById('next-level').textContent = stats.siguienteNivel;
      }

      // Actualizar barras visuales
      const progressFill = document.getElementById('progress-fill');
      if (progressFill) {
        progressFill.style.width = stats.progresoNivel;
      }

      // Actualizar círculo de progreso en header
      if (stats.porcentajeProgresoNumero !== undefined) {
        actualizarProgresoHeader(stats.porcentajeProgresoNumero);
      }

      // Actualizar experiencia total
      if (document.getElementById('total-experiencia')) {
        document.getElementById('total-experiencia').textContent = stats.experienciaTotal;
      }
    })
    .catch(error => {
      console.error('Error al cargar estadísticas:', error);
    });
}

// Función para marcar como completado/pendiente
function marcarCompletado(id, completado) {
    console.log(`Marcando hábito ${id} como ${completado ? 'completado' : 'pendiente'}`);

    // Primero obtener el hábito actual para mantener los otros campos
    fetch(`/api/habitos/${id}`)
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Error al obtener hábito');
                });
            }
            return response.json();
        })
        .then(habito => {
            console.log("Hábito obtenido:", habito);

            // Actualizar solo el campo completado
            return fetch(`/api/habitos/${id}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    nombre: habito.nombre,
                    descripcion: habito.descripcion,
                    rol: habito.rol,
                    dificultad: habito.dificultad,
                    puntosExperiencia: habito.puntosExperiencia,
                    completado: completado
                })
            });
        })
        .then(response => {
            console.log("Respuesta de actualización:", response);

            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || `Error HTTP: ${response.status}`);
                });
            }
            return response.json();
        })
        .then(habitoActualizado => {
            console.log("Hábito actualizado:", habitoActualizado);

            // Recargar la lista y estadísticas
            cargarMisHabitos();
            cargarEstadisticasUsuario();

            // Mostrar mensaje de éxito
            if (completado) {
                const expGanada = habitoActualizado.puntosExperiencia || 20;
                mostrarMensajeExito(`¡Hábito completado! +${expGanada} XP ganados`);
            } else {
                mostrarMensajeExito('Hábito marcado como pendiente');
            }
        })
        .catch(error => {
            console.error('Error completo al actualizar hábito:', error);
            mostrarErrorCompleto('Error al actualizar el hábito', error);
        });
}

// Función para mostrar mensajes de éxito
function mostrarMensajeExito(mensaje) {
    const notification = document.createElement('div');
    notification.className = 'success-notification';
    notification.innerHTML = `
        <div class="notification-content">
            <span class="notification-icon">✅</span>
            <span class="notification-text">${mensaje}</span>
            <button class="notification-close" onclick="this.parentElement.parentElement.remove()">×</button>
        </div>
    `;

    document.body.appendChild(notification);

    // Auto-remover después de 3 segundos
    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 3000);
}

// Función mejorada para mostrar errores
function mostrarErrorCompleto(titulo, error) {
    console.error(`${titulo}:`, error);

    const modalError = document.createElement('div');
    modalError.className = 'error-modal';
    modalError.innerHTML = `
        <div class="error-content">
            <h3>${titulo}</h3>
            <p><strong>Detalles:</strong> ${error.message}</p>
            <div class="error-actions">
                <button onclick="this.closest('.error-modal').remove(); recargarTodo();" class="btn-small btn-primary">Reintentar</button>
                <button onclick="this.closest('.error-modal').remove()" class="btn-small">Aceptar</button>
            </div>
        </div>
    `;
    document.body.appendChild(modalError);
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

// Cargar hábitos y estadísticas cuando la página se carga
document.addEventListener('DOMContentLoaded', function() {
  console.log("Página cargada, iniciando carga de hábitos y estadísticas...");
  cargarMisHabitos();
  cargarEstadisticasUsuario();
});

function recargarTodo() {
  cargarMisHabitos();
  cargarEstadisticasUsuario();
}

// Funciones para el modal de perfil con animación recta hacia la izquierda
function abrirModalPerfil() {
    const modal = document.getElementById('modal-perfil');
    const modalContent = modal.querySelector('.modal-content');

    // Aplicar animación desde la izquierda
    modalContent.style.animation = 'modalSlideFromLeft 0.4s ease-out';
    modal.style.display = 'block';

    cargarDatosPerfil();
}

function cerrarModalPerfil() {
    const modal = document.getElementById('modal-perfil');
    const modalContent = modal.querySelector('.modal-content');

    // Animación de salida hacia la izquierda
    modalContent.style.animation = 'modalSlideToLeft 0.3s ease-in';

    setTimeout(() => {
        modal.style.display = 'none';
        // Restaurar animación original para la próxima vez
        modalContent.style.animation = 'modalSlideFromLeft 0.4s ease-out';
    }, 250);
}

// Cerrar modal al hacer click fuera del contenido
window.onclick = function(event) {
    const modal = document.getElementById('modal-perfil');
    if (event.target === modal) {
        cerrarModalPerfil();
    }
}

// Cerrar con tecla ESC
document.addEventListener('keydown', function(event) {
    const modal = document.getElementById('modal-perfil');
    if (event.key === 'Escape' && modal.style.display === 'block') {
        cerrarModalPerfil();
    }
});

// Cargar datos del perfil en el modal
function cargarDatosPerfil() {
    fetch("/api/usuario/info-completa")
        .then(response => {
            if (!response.ok) throw new Error('Error al cargar datos del perfil');
            return response.json();
        })
        .then(userInfo => {
            // Información de la cuenta
            document.getElementById('info-username').textContent = userInfo.username;
            document.getElementById('info-email').textContent = userInfo.email || 'No especificado';
            document.getElementById('info-level').textContent = userInfo.nivel;
            document.getElementById('info-rank').textContent = userInfo.rango;
            document.getElementById('info-exp-total').textContent = userInfo.experienciaTotal + ' XP';
            document.getElementById('info-joined').textContent = '2025'; // Puedes agregar fecha de registro en el backend

            // Formulario de edición
            document.getElementById('edit-username').value = userInfo.username;
            document.getElementById('edit-email').value = userInfo.email || '';
        })
        .catch(error => {
            console.error('Error al cargar datos del perfil:', error);
            alert('Error al cargar los datos del perfil');
        });
}

// Manejar cambio de avatar
document.getElementById('avatar-upload').addEventListener('change', function(event) {
    const file = event.target.files[0];
    if (file) {
        if (file.size > 5 * 1024 * 1024) { // 5MB limit
            alert('La imagen es demasiado grande. Máximo 5MB.');
            return;
        }

        if (!file.type.startsWith('image/')) {
            alert('Por favor selecciona una imagen válida.');
            return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            // Actualizar imagen en el modal
            document.getElementById('current-avatar-img').src = e.target.result;
            // Actualizar imagen en el header
            document.getElementById('user-avatar').src = e.target.result;

            // Aquí podrías enviar la imagen al servidor
            // uploadAvatarToServer(file);
        };
        reader.readAsDataURL(file);
    }
});

function restablecerAvatar() {
    const defaultAvatar = 'css/img/default-avatar.jpg';
    document.getElementById('current-avatar-img').src = defaultAvatar;
    document.getElementById('user-avatar').src = defaultAvatar;

    // Aquí podrías notificar al servidor para restablecer el avatar
    // resetAvatarOnServer();
}

// Manejar envío del formulario de edición
document.getElementById('form-editar-perfil').addEventListener('submit', function(event) {
    event.preventDefault();

    const formData = {
        username: document.getElementById('edit-username').value,
        email: document.getElementById('edit-email').value
    };

    const password = document.getElementById('edit-password').value;
    const passwordConfirm = document.getElementById('edit-password-confirm').value;

    if (password) {
        if (password !== passwordConfirm) {
            alert('Las contraseñas no coinciden.');
            return;
        }
        if (password.length < 6) {
            alert('La contraseña debe tener al menos 6 caracteres.');
            return;
        }
        formData.password = password;
    }

    // Enviar datos al servidor
    fetch("/api/usuario/actualizar", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(formData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text) });
        }
        return response.json();
    })
    .then(updatedUser => {
        alert('Perfil actualizado exitosamente!');
        cerrarModalPerfil();

        // Actualizar información en la página
        document.getElementById('username').textContent = updatedUser.username;
        cargarEstadisticasUsuario();
    })
    .catch(error => {
        console.error('Error al actualizar perfil:', error);
        alert('Error al actualizar el perfil: ' + error.message);
    });
});

// Función para subir avatar al servidor (ejemplo)
function uploadAvatarToServer(file) {
    const formData = new FormData();
    formData.append('avatar', file);

    fetch("/api/usuario/avatar", {
        method: "POST",
        body: formData
    })
    .then(response => {
        if (!response.ok) throw new Error('Error al subir avatar');
        return response.json();
    })
    .then(result => {
        console.log('Avatar actualizado:', result);
    })
    .catch(error => {
        console.error('Error al subir avatar:', error);
    });
}