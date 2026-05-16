const API_BASE = 'http://localhost:8080';
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

  const telefonoRegex = /^[0-9]{9}$/;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  // Validación del nombre
  if (fullName.length < 3) {
    errorText.textContent = 'El nombre debe tener al menos 3 caracteres.';
    errorMsg.style.display = 'block';
    return;
  }

  // Validación del teléfono
  if (!telefonoRegex.test(telefono)) {
    errorText.textContent = 'El teléfono debe tener exactamente 9 números.';
    errorMsg.style.display = 'block';
    return;
  }

  // Validación del email
  if (!emailRegex.test(email)) {
    errorText.textContent = 'Introduce un correo electrónico válido.';
    errorMsg.style.display = 'block';
    return;
  }

  // Validación de contraseña mínima
  if (password.length < 6) {
    errorText.textContent = 'La contraseña debe tener al menos 6 caracteres.';
    errorMsg.style.display = 'block';
    return;
  }

  // Validación de contraseñas iguales
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