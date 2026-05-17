const API_BASE = 'http://localhost:8080';

document.getElementById('loginForm').addEventListener('submit', async function (e) {
  e.preventDefault();

  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;
  const errorMsg = document.getElementById('errorMsg');
  const errorText = document.getElementById('errorText');

  errorMsg.style.display = 'none';

  try {
    const response = await fetch(API_BASE + '/pistaPadel/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ email, password })
    });

    if (response.ok) {
      window.location.href = 'pistas.html';
    } else if (response.status === 401) {
      errorText.textContent = 'Correo o contraseña incorrectos';
      errorMsg.style.display = 'block';
    } else {
      errorText.textContent = 'Error al iniciar sesión. Inténtalo de nuevo.';
      errorMsg.style.display = 'block';
    }
  } catch (err) {
    errorText.textContent = 'No se pudo conectar con el servidor.';
    errorMsg.style.display = 'block';
  }
});
