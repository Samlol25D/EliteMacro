document.addEventListener('DOMContentLoaded', function() {
  const form = document.getElementById('form-registro');
  const usernameInput = document.getElementById('username');
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const confirmPasswordInput = document.getElementById('confirm-password');
  const messageContainer = document.getElementById('message-container');
  const submitButton = form.querySelector('.btn-primary');

  // Elementos de requisitos de contraseña
  const reqLength = document.getElementById('req-length');
  const reqUppercase = document.getElementById('req-uppercase');
  const reqSpecial = document.getElementById('req-special');
  const reqMatch = document.getElementById('req-match');

  // Validación en tiempo real
  passwordInput.addEventListener('input', validatePassword);
  confirmPasswordInput.addEventListener('input', validatePassword);
  usernameInput.addEventListener('blur', checkUsernameAvailability);

  // Validar formulario antes de enviar
  form.addEventListener('submit', validateBeforeSubmit);

  function showMessage(text, type = 'error') {
    messageContainer.innerHTML = '';

    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;

    const icon = type === 'error' ? 'fas fa-exclamation-circle' : 'fas fa-check-circle';
    messageDiv.innerHTML = `
      <i class="${icon}"></i>
      <span>${text}</span>
    `;

    messageContainer.appendChild(messageDiv);
  }

  function validatePassword() {
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    let isValid = true;

    // Validar longitud
    if (password.length >= 6) {
      reqLength.classList.add('valid');
      reqLength.classList.remove('invalid');
      reqLength.querySelector('i').className = 'fas fa-check-circle';
    } else {
      reqLength.classList.add('invalid');
      reqLength.classList.remove('valid');
      reqLength.querySelector('i').className = 'fas fa-times-circle';
      isValid = false;
    }

    // Validar mayúscula
    if (/[A-Z]/.test(password)) {
      reqUppercase.classList.add('valid');
      reqUppercase.classList.remove('invalid');
      reqUppercase.querySelector('i').className = 'fas fa-check-circle';
    } else {
      reqUppercase.classList.add('invalid');
      reqUppercase.classList.remove('valid');
      reqUppercase.querySelector('i').className = 'fas fa-times-circle';
      isValid = false;
    }

    // Validar carácter especial
    if (/[@$!%*?&]/.test(password)) {
      reqSpecial.classList.add('valid');
      reqSpecial.classList.remove('invalid');
      reqSpecial.querySelector('i').className = 'fas fa-check-circle';
    } else {
      reqSpecial.classList.add('invalid');
      reqSpecial.classList.remove('valid');
      reqSpecial.querySelector('i').className = 'fas fa-times-circle';
      isValid = false;
    }

    // Validar coincidencia
    if (password === confirmPassword && password !== '') {
      reqMatch.classList.add('valid');
      reqMatch.classList.remove('invalid');
      reqMatch.querySelector('i').className = 'fas fa-check-circle';
    } else {
      reqMatch.classList.add('invalid');
      reqMatch.classList.remove('valid');
      reqMatch.querySelector('i').className = 'fas fa-times-circle';
      isValid = false;
    }

    // Habilitar/deshabilitar botón
    submitButton.disabled = !isValid;

    return isValid;
  }

  async function checkUsernameAvailability() {
    const username = usernameInput.value.trim();

    if (username.length < 3) return;

    try {
      const response = await fetch(`/api/registro/check-username?username=${encodeURIComponent(username)}`);
      const data = await response.json();

      if (data.exists) {
        usernameInput.classList.add('error-border');
        showMessage('❌ Este nombre ya ha sido registrado', 'error');
        submitButton.disabled = true;
      } else {
        usernameInput.classList.remove('error-border');
        clearMessage();
        submitButton.disabled = false;
      }
    } catch (error) {
      console.error('Error checking username:', error);
    }
  }

  function clearMessage() {
    messageContainer.innerHTML = '';
  }

  function validateBeforeSubmit(e) {
    // Validar contraseña
    if (!validatePassword()) {
      e.preventDefault();
      showMessage('Por favor, cumple todos los requisitos de la contraseña', 'error');
      return false;
    }

    // Validar email
    const email = emailInput.value.trim();
    if (!email || !email.includes('@')) {
      e.preventDefault();
      emailInput.classList.add('error-border');
      showMessage('❌ Por favor, introduce un correo electrónico válido', 'error');
      return false;
    }

    // Si todo está bien, permitir el envío del formulario
    submitButton.disabled = true;
    submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Registrando...';

    // El formulario se enviará normalmente al servidor Spring
    return true;
  }

  // Validación inicial
  validatePassword();
});