const API_URL = '/api/clients';

document.addEventListener("DOMContentLoaded", () => {
    verificarEdicion();

    const form = document.getElementById('userForm');
    if(form) {
        form.addEventListener('submit', guardarCliente);
    }
});

// 1. DETECTAR SI ES EDICIÓN (Mirando la URL)
async function verificarEdicion() {
    // Obtenemos el ID de la URL. Ej: /users/form/5  -> id = 5
    const pathParts = window.location.pathname.split('/');
    const id = pathParts[pathParts.length - 1]; // El último segmento

    // Si es un número, estamos editando
    if (!isNaN(id) && id !== 'FormClients') {
        document.getElementById('formTitle').innerText = 'Editar Cliente';
        document.getElementById('btnSubmit').innerHTML = '<span class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span> Actualizar Cliente';
        document.getElementById('id').value = id;
        await cargarDatosUsuario(id);
    }
}

// 2. CARGAR DATOS (GET)
async function cargarDatosUsuario(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`);
        if (!response.ok) throw new Error("No se pudo cargar el cliente");
        const client = await response.json();

        // Rellenar campos
        document.getElementById('name').value = client.name;
        document.getElementById('lastname').value = client.lastname;
        document.getElementById('dni').value = client.dni;
        document.getElementById('email').value = client.email;
        document.getElementById('cellPhone').value = client.cellPhone;
        document.getElementById('age').value = client.age;
        document.getElementById('direction').value = client.direction;


    } catch (error) {
        console.error(error);
        alert("Error cargando datos del cliente");
    }
}

// 3. ENVIAR FORMULARIO (POST/PUT)
async function guardarCliente(e) {
    e.preventDefault();
    limpiarErrores();
    mostrarCargando(true);

    const id = document.getElementById('id').value;
    const esEdicion = !!id;

    // Construir JSON
    const clientData = {
        id: id ? parseInt(id) : null,
        name: document.getElementById('name').value,
        lastname: document.getElementById('lastname').value,
        dni: document.getElementById('dni').value,
        email: document.getElementById('email').value,
        cellPhone: document.getElementById('cellPhone').value,
        age: document.getElementById('age').value ? parseInt(document.getElementById('age').value) : null,
        direction: document.getElementById('direction').value,

    };

    const method = esEdicion ? 'PUT' : 'POST';
    const url = esEdicion ? `${API_URL}/${id}` : API_URL;

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(clientData)
        });

        if (response.ok) {
            // Éxito: Redirigir a la lista
            window.location.href = '/clients';
        } else {
            // Error: Probablemente validación (400)
            const errorData = await response.json();
            if (response.status === 400 && errorData.errors) {
                 // Manejar errores de Spring Validation
                 mostrarErroresValidacion(errorData.errors);
            } else {
                mostrarErrorGlobal("Ocurrió un error inesperado al guardar.");
            }
        }
    } catch (error) {
        console.error(error);
        mostrarErrorGlobal("Error de conexión con el servidor.");
    } finally {
        mostrarCargando(false);
    }
}

// 4. UTILIDADES VISUALES
function limpiarErrores() {
    document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    document.querySelectorAll('.invalid-feedback').forEach(el => el.innerText = '');
    document.getElementById('globalError').classList.add('d-none');
}

function mostrarErroresValidacion(errors) {
    // Spring Boot puede devolver errores como lista o mapa.
    // Si usas BindingResult, suele devolver un JSON con campo y mensaje.
    // Adaptar según cómo devuelva tu API los errores.

    // Ejemplo asumiendo estructura estándar: [{field: "email", defaultMessage: "Error..."}]
    if (Array.isArray(errors)) {
        errors.forEach(err => {
            const input = document.getElementById(err.field);
            if (input) {
                input.classList.add('is-invalid');
                const feedback = document.getElementById(`error-${err.field}`);
                if (feedback) feedback.innerText = err.defaultMessage;
            }
        });
    } else {
         // Si devuelve un mapa simple { "email": "Error..." }
         for (const [field, msg] of Object.entries(errors)) {
            const input = document.getElementById(field);
            if (input) {
                input.classList.add('is-invalid');
                const feedback = document.getElementById(`error-${field}`);
                if (feedback) feedback.innerText = msg;
            }
         }
    }
    mostrarErrorGlobal("Por favor corrija los errores en el formulario.");
}

function mostrarErrorGlobal(msg) {
    const alerta = document.getElementById('globalError');
    alerta.innerText = msg;
    alerta.classList.remove('d-none');
}

function mostrarCargando(activo) {
    const btn = document.getElementById('btnSubmit');
    const spinner = btn.querySelector('.spinner-border');
    if (activo) {
        btn.disabled = true;
        spinner.classList.remove('d-none');
    } else {
        btn.disabled = false;
        spinner.classList.add('d-none');
    }
}
