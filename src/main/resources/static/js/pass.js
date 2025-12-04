document.getElementById('forgotPasswordForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const messageDiv = document.getElementById('message');

        try {
            const response = await fetch('/api/password/forgot', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email: email })
            });

            const data = await response.json();

            if (data.success) {
                messageDiv.innerHTML = `<div class="success">${data.message}</div>`;
                messageDiv.style.display = 'block';
                document.getElementById('forgotPasswordForm').reset();
            } else {
                messageDiv.innerHTML = `<div class="error">${data.message}</div>`;
                messageDiv.style.display = 'block';
            }
        } catch (error) {
            messageDiv.innerHTML = `<div class="error">Error al procesar la solicitud</div>`;
            messageDiv.style.display = 'block';
        }
    });