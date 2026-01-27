let currentPage = 0;
let currentSize = 10;
let totalPages = 0;
let currentTerm = '';
let currentState = 'ALL';

document.addEventListener('DOMContentLoaded', function () {
    cargarProductos();

    const inputSearch = document.getElementById('inputSearch');
    inputSearch.addEventListener('input', debounce(function (e) {
        currentTerm = e.target.value.trim();
        currentPage = 0;
        cargarProductos();
    }, 300));

    const inputFilter = document.getElementById('inputFilter');
    inputFilter.addEventListener('change', function (e) {
        currentState = e.target.value;
        currentPage = 0;
        cargarProductos();
    });
});

function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

async function cargarProductos() {
    let url = `/api/products?page=${currentPage}&size=${currentSize}`;

    if (currentTerm.length > 0) {
        url = `/api/products/search/${currentTerm}?page=${currentPage}&size=${currentSize}`;
    } else if (currentState !== 'ALL') {
        url = `/api/products?state=${currentState}&page=${currentPage}&size=${currentSize}`;
    }

    try {
        const response = await fetch(url);
        if (response.ok) {
            const pageWrapper = await response.json();
            const pageData = { content: pageWrapper.content, ...pageWrapper.page };
            renderizarTabla(pageData.content);
            actualizarPaginacion(pageData);
        } else {
            mostrarAlerta('Error al cargar productos', 'danger');
        }
    } catch (error) {
        console.error("Error en cargarProductos:", error);
        mostrarAlerta('Error de conexión', 'danger');
    }
}

function cambiarPagina(delta) {
    const newPage = currentPage + delta;
    if (newPage >= 0 && newPage < totalPages) {
        currentPage = newPage;
        cargarProductos();
    }
}

async function eliminarProducto(id) {
    if (!confirm('¿Estás seguro de eliminar este producto?')) return;

    try {
        const response = await fetch(`/api/products/${id}`, { method: 'DELETE' });
        if (response.ok) {
            mostrarAlerta('Producto eliminado correctamente', 'success');
            cargarProductos();
        } else {
            mostrarAlerta('No se pudo eliminar el producto', 'danger');
        }
    } catch (error) {
        console.error(error);
        mostrarAlerta('Error al eliminar', 'danger');
    }
}

function renderizarTabla(productos) {
    const tbody = document.getElementById('productTableBody');
    tbody.innerHTML = '';

    if (!productos || productos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted">No se encontraron productos</td></tr>`;
        return;
    }

    productos.forEach(p => {
        let badgeClass = 'bg-secondary';
        if (p.state === 'DISPONIBLE') badgeClass = 'bg-success';
        else if (p.state === 'AGOTADO') badgeClass = 'bg-danger';
        else if (p.state === 'CASI_AGOTADO') badgeClass = 'bg-warning text-dark';

        const imgUrl = p.image ? p.image : 'https://placehold.co/50x50?text=N/A';

        const row = `
            <tr>
                <td class="text-nowrap-cell">${p.id}</td>
                <td class="text-nowrap-cell"><img src="${imgUrl}" alt="img" width="50" height="50" class="rounded border" onerror="this.onerror=null; this.src='https://placehold.co/50x50?text=N/A';"></td>
                <td class="text-nowrap-cell">${p.code}</td>
                <td class="text-wrap-cell">${p.nameProducto}</td>
                <td class="text-nowrap-cell">${p.marca}</td>
                <td class="text-nowrap-cell">$${p.price.toFixed(2)}</td>
                <td class="text-nowrap-cell">${p.stock}</td>
                <td class="text-nowrap-cell"><span class="badge ${badgeClass}">${p.state}</span></td>
                <td class="text-nowrap-cell">
                    <a href="/products/edit/${p.id}" class="btn btn-sm btn-warning" title="Editar"><i class="bi bi-pencil-square"></i></a>
                    <button onclick="eliminarProducto(${p.id})" class="btn btn-sm btn-danger" title="Eliminar"><i class="bi bi-trash"></i></button>
                </td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}

function actualizarPaginacion(pageData) {
    if (!pageData) {
        console.error("actualizarPaginacion recibió pageData nulo o undefined");
        return;
    }

    totalPages = pageData.totalPages || 0;
    const totalElements = pageData.totalElements || 0;
    const number = pageData.number || 0;

    const isFirst = (number === 0);
    const isLast = (number >= totalPages - 1);

    let start = 0;
    let end = 0;

    if (totalElements > 0) {
        start = number * currentSize + 1;
        end = Math.min((number + 1) * currentSize, totalElements);
    }

    document.getElementById('paginationInfo').innerText = `Mostrando ${start}-${end} de ${totalElements}`;

    const btnPrev = document.getElementById('btnPrev');
    const btnNext = document.getElementById('btnNext');

    if (isFirst || totalPages === 0) {
        btnPrev.classList.add('disabled');
    } else {
        btnPrev.classList.remove('disabled');
    }

    if (isLast || totalPages === 0) {
        btnNext.classList.add('disabled');
    } else {
        btnNext.classList.remove('disabled');
    }
}

function mostrarAlerta(mensaje, tipo) {
    const container = document.getElementById('alertContainer');
    container.innerHTML = `
        <div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    setTimeout(() => {
        container.innerHTML = '';
    }, 3000);
}
