    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    const email = urlParams.get('email');

    document.getElementById('token').value = token;
    document.getElementById('email').value = email;

    // Validar token al cargar la página
    window.onload = async () => {
        if (!token || !email) {
            document.getElementById('message').innerHTML =
                '<div class="error">Enlace inválido o expirado</div>';
            return;
        }

        try {
            const response = await fetch('/api/password/validate-token', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    token: token
                })
            });

            const data = await response.json();

            if (!data.valid) {
                document.getElementById('message').innerHTML =
                    '<div class="error">El enlace ha expirado o es inválido</div>';
                document.getElementById('resetPasswordForm').style.display = 'none';
            }
        } catch (error) {
            document.getElementById('message').innerHTML =
                '<div class="error">Error al validar el enlace</div>';
        }
    };

    // Manejar envío del formulario
    document.getElementById('resetPasswordForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const messageDiv = document.getElementById('message');

        // Validar que las contraseñas coincidan
        if (newPassword !== confirmPassword) {
            messageDiv.innerHTML = '<div class="error">Las contraseñas no coinciden</div>';
            messageDiv.style.display = 'block';
            return;
        }

        // Validar longitud mínima
        if (newPassword.length < 6) {
            messageDiv.innerHTML = '<div class="error">La contraseña debe tener al menos 6 caracteres</div>';
            messageDiv.style.display = 'block';
            return;
        }

        try {
            const response = await fetch('/api/password/reset', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    token: token,
                    newPassword: newPassword
                })
            });

            const data = await response.json();

            if (data.success) {
                messageDiv.innerHTML = `<div class="success">${data.message}
                    <br>Serás redirigido al inicio de sesión en 5 segundos...</div>`;
                messageDiv.style.display = 'block';

                // Redirigir después de 5 segundos
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 5000);
            } else {
                messageDiv.innerHTML = `<div class="error">${data.message}</div>`;
                messageDiv.style.display = 'block';
            }
        } catch (error) {
            messageDiv.innerHTML = '<div class="error">Error al cambiar la contraseña</div>';
            messageDiv.style.display = 'block';
        }
    });