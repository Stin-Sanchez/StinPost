document.addEventListener("DOMContentLoaded", () => {
    listarClientes();

    const inputSearch = document.getElementById('inputSearch');
    if (inputSearch) {
        inputSearch.addEventListener('input', debounce(function (e) {
            const termino = e.target.value.trim();
            if (termino.length > 0) {
                buscarClientes(termino);
            } else {
                listarClientes();
            }
        }, 300));
    }
});

const API_URL = '/api/clients';

function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

async function listarClientes() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error('Error al conectar con la API');
        renderizarTabla(await response.json());
    } catch (error) {
        console.error(error);
        mostrarAlerta('Error cargando clientes: ' + error.message, 'danger');
    }
}

async function buscarClientes(termino) {
    try {
        const response = await fetch(`${API_URL}/search/${termino}`);
        if (response.ok) renderizarTabla(await response.json());
    } catch (error) {
        console.error(error);
    }
}

function renderizarTabla(clients) {
    const tbody = document.getElementById('table-users-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (clients.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No se encontraron clientes.</td></tr>`;
        return;
    }

    let html = '';
    clients.forEach(c => {
        const creado = c.createdAt ? new Date(c.createdAt).toLocaleDateString() : 'N/A';
        html += `
        <tr>
            <td>${c.id}</td>
            <td>${c.fullName}</td>
            <td>${c.dni}</td>
            <td>${c.cellPhone}</td>
            <td>${c.email}</td>
            <td>${creado}</td>
            <td>${c.direction}</td>
            <td>
                <a class="btn btn-sm btn-warning me-1" href="/clients/FormClients/${c.id}" title="Editar"><i class="bi bi-pencil-square"></i></a>
                <button class="btn btn-sm btn-danger" onclick="eliminarUsuario(${c.id})" title="Eliminar"><i class="bi bi-trash"></i></button>
            </td>
        </tr>`;
    });
    tbody.innerHTML = html;
}

async function eliminarUsuario(id) {
    if (!confirm('¿Estás seguro de eliminar este usuario?')) return;

    try {
        const response = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        if (response.ok) {
            mostrarAlerta('Cliente eliminado con éxito', 'success');
            listarClientes();
        } else {
            mostrarAlerta('No se pudo eliminar el usuario', 'danger');
        }
    } catch (error) {
        console.error(error);
        mostrarAlerta('Error de conexión', 'danger');
    }
}

function mostrarAlerta(mensaje, tipo) {
    const container = document.getElementById('alert-container');
    if (!container) return;

    container.innerHTML = `<div class="alert alert-${tipo} alert-dismissible fade show" role="alert">${mensaje}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>`;
    setTimeout(() => {
        const alertEl = container.querySelector('.alert');
        if(alertEl) new bootstrap.Alert(alertEl).close();
    }, 3000);
}
