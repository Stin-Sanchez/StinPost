const API_URL = '/api/products';

document.addEventListener("DOMContentLoaded", () => {
    verificarEdicion();
    actualizarEstadoVisual();

    // Escuchar cambios en AMBOS inputs
    const stockInput = document.getElementById('stock');
    const minStockInput = document.getElementById('minStock');

    if(stockInput) stockInput.addEventListener('input', actualizarEstadoVisual);
    if(minStockInput) minStockInput.addEventListener('input', actualizarEstadoVisual);

    // Formulario
    const form = document.getElementById('productForm');
    if(form) {
        form.addEventListener('submit', guardarProducto);
    }
});

function actualizarEstadoVisual() {
    // Obtenemos valores del DOM
    const stockVal = document.getElementById('stock').value;
    const minStockVal = document.getElementById('minStock').value; // Asegurate de tener este ID
    const stateSelect = document.getElementById('state');

    if(!stateSelect) return;

    const stock = parseInt(stockVal) || 0;
    const minStock = parseInt(minStockVal) || 5; // Default 5 si está vacío

    // Umbral para "Casi Agotado" (Mismo que en Java)
    const umbralCasiAgotado = minStock + 10;

    let nuevoEstado = "";
    let claseColor = "";

    // LÓGICA ESPEJO (Idéntica a Java)
    if (stock <= 0) {
        nuevoEstado = "AGOTADO";
        claseColor = "text-danger"; // Rojo
    } else if (stock <= minStock) {
        nuevoEstado = "CON_STOCK_MINIMO";
        claseColor = "text-warning"; // Naranja/Amarillo oscuro
    } else if (stock <= umbralCasiAgotado) {
        nuevoEstado = "CASI_AGOTADO";
        claseColor = "text-info"; // Azulito o Amarillo claro
    } else {
        nuevoEstado = "DISPONIBLE";
        claseColor = "text-success"; // Verde
    }

    // Aplicar al select
    stateSelect.value = nuevoEstado;

    // Limpieza visual (Opcional: cambiar bordes o colores)
    stateSelect.className = 'form-select bg-light ' + claseColor;
}

// 1. PREVIEW DE IMAGEN LOCAL
function previewImage(event) {
    const file = event.target.files[0];
    const preview = document.getElementById('imagePreview');
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.classList.remove('d-none');
        }
        reader.readAsDataURL(file);
    }
}

// 2. VERIFICAR EDICIÓN
async function verificarEdicion() {
    const pathParts = window.location.pathname.split('/');
    const id = pathParts[pathParts.length - 1];

    if (!isNaN(id) && id !== 'formProducts') {
        document.getElementById('formTitle').innerText = 'Editar Producto';
        const btn = document.getElementById('btnSubmit');
        btn.innerHTML = '<span class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span> Actualizar Producto';

        document.getElementById('id').value = id;
        await cargarDatosProducto(id);
    }
}

// 3. CARGAR DATOS
async function cargarDatosProducto(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`);
        if (!response.ok) throw new Error("Error al cargar producto");
        const p = await response.json();

        // Llenar campos
        document.getElementById('code').value = p.code;
        document.getElementById('nameProducto').value = p.nameProducto;
        document.getElementById('description').value = p.description;
        document.getElementById('marca').value = p.marca;
        document.getElementById('price').value = p.price;
        document.getElementById('stock').value = p.stock;
        document.getElementById('minStock').value = p.minStock;
        document.getElementById('expirationDate').value = p.expirationDate; // Formato YYYY-MM-DD

        const stateInput = document.getElementById('state');
        if(stateInput){
            stateInput.value= p.state;
        }

        actualizarEstadoVisual();


        // Manejo de Imagen existente
        if (p.image) {
            const preview = document.getElementById('imagePreview');
            preview.src = p.image;
            preview.classList.remove('d-none');
            document.getElementById('currentImageUrl').value = p.image;
        }

    } catch (error) {
        console.error(error);
        mostrarErrorGlobal("No se pudieron cargar los datos del producto");
    }
}

// 4. ENVIAR FORMULARIO (FormData para Archivos)
async function guardarProducto(e) {
    e.preventDefault();
    limpiarErrores();
    mostrarCargando(true);

    const id = document.getElementById('id').value;
    const esEdicion = !!id;

    // --- AQUÍ ESTÁ LA MAGIA: FormData ---
    const formData = new FormData();

    // Agregamos los campos de texto
    // Nota: Los nombres ("code", "nameProducto") deben coincidir con tu DTO o Entidad Java
    formData.append('code', document.getElementById('code').value);
    formData.append('nameProducto', document.getElementById('nameProducto').value);
    formData.append('description', document.getElementById('description').value);
    formData.append('marca', document.getElementById('marca').value);
    formData.append('price', document.getElementById('price').value);
    formData.append('stock', document.getElementById('stock').value);
    formData.append('minStock', document.getElementById('minStock').value);
    formData.append('expirationDate', document.getElementById('expirationDate').value);
    formData.append('state', document.getElementById('state').value);

    // Agregamos la imagen SOLO si el usuario seleccionó una nueva
    const imageInput = document.getElementById('imageFile');
    if (imageInput.files[0]) {
        formData.append('imageFile', imageInput.files[0]);
    }

    const url = esEdicion ? `${API_URL}/${id}` : API_URL;
    const method = esEdicion ? 'PUT' : 'POST';

    try {
        // NOTA: Al usar FormData, NO se pone el header 'Content-Type': 'application/json'
        // El navegador lo detecta automáticamente como multipart/form-data
        const response = await fetch(url, {
            method: method,
            body: formData
        });

        if (response.ok) {
            window.location.href = '/products';
        } else {
            const errorData = await response.json();
            if (response.status === 400 && errorData.errors) {
                mostrarErroresValidacion(errorData.errors);
            } else {
                mostrarErrorGlobal("Error del servidor: " + response.status);
            }
        }
    } catch (error) {
        console.error(error);
        mostrarErrorGlobal("Error de conexión");
    } finally {
        mostrarCargando(false);
    }
}

// Utilidades (Mismas que en Usuarios)
function limpiarErrores() {
    document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    document.querySelectorAll('.invalid-feedback').forEach(el => el.innerText = '');
    document.getElementById('globalError').classList.add('d-none');
}

function mostrarErroresValidacion(errors) {
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
         for (const [field, msg] of Object.entries(errors)) {
            const input = document.getElementById(field);
            if (input) {
                input.classList.add('is-invalid');
                const feedback = document.getElementById(`error-${field}`);
                if (feedback) feedback.innerText = msg;
            }
         }
    }
    mostrarErrorGlobal("Corrija los errores en el formulario");
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
