document.getElementById("form-registro").addEventListener("submit", function(e) {
    e.preventDefault();

    fetch("/api/registro", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            username: document.getElementById("username").value,
            password: document.getElementById("password").value
        })
    }).then(res => {
        if (res.ok) {
            alert("¡Registrado con éxito!");
            window.location.href = "login.html";
        } else {
            alert("El usuario ya existe");
        }
    });
});