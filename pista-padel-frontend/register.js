const API_BASE = 'http://localhost:8080';

document.getElementById('registerForm').addEventListener('submit', async function (e) {
  e.preventDefault();

  const fullName = document.getElementById('name').value.trim();
  const spaceIdx = fullName.indexOf(' ');
  const nombre = spaceIdx > 0 ? fullName.substring(0, spaceIdx) : fullName;
  const apellidos = spaceIdx > 0 ? fullName.substring(spaceIdx + 1).trim() : '-';

  const telefono = document.getElementById('phone').value.trim();
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;
  const confirmPassword = document.getElementById('confirmPassword').value;

  const errorMsg = document.getElementById('errorMsg');
  const errorText = document.getElementById('errorText');
  errorMsg.style.display = 'none';

  if (password !== confirmPassword) {
    errorText.textContent = 'Las contraseñas no coinciden.';
    errorMsg.style.display = 'block';
    return;
  }

  try {
    const response = await fetch(API_BASE + '/pistaPadel/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nombre, apellidos, email, telefono, password })
  });

    if (response.status === 201) {
      window.location.href = 'login.html';
    } else if (response.status === 409) {
      errorText.textContent = 'Ese correo ya está registrado.';
      errorMsg.style.display = 'block';
    } else if (response.status === 400) {
      const data = await response.json().catch(() => ({}));
      errorText.textContent = data.message || 'Datos incorrectos. Revisa el formulario.';
      errorMsg.style.display = 'block';
    } else {
      errorText.textContent = 'Error al registrar la cuenta. Inténtalo de nuevo.';
      errorMsg.style.display = 'block';
    }
  } catch (err) {
    errorText.textContent = 'No se pudo conectar con el servidor.';
    errorMsg.style.display = 'block';
  }
});
