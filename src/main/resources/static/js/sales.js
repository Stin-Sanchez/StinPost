const API_URL = '/api/sales';
let searchTimeout = null;

document.addEventListener("DOMContentLoaded", () => {
    listarVentas(); // Carga inicial

    const inputSearch = document.getElementById('inputSearch');
    const inputFilter = document.getElementById('inputFilter');

    if (inputSearch) inputSearch.addEventListener('input', handleSearch);
    if (inputFilter) inputFilter.addEventListener('change', handleFilterChange);
});

// --- LÓGICA DE BÚSQUEDA Y FILTRADO (Igual que Productos) ---

function handleSearch(event) {
    const searchTerm = event.target.value.trim();
    if (searchTimeout) clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        buscarVentas(searchTerm);
    }, 500);
}

function handleFilterChange() {
    const searchTerm = document.getElementById('inputSearch').value.trim();
    buscarVentas(searchTerm);
}

async function listarVentas() {
    buscarVentas('');
}

async function buscarVentas(termino) {
    try {
        mostrarCargando();

        const estadoSelect = document.getElementById('inputFilter');
        let estado = estadoSelect ? estadoSelect.value : '';

        // Normalización
        if (estado === 'Todas' || estado === 'ALL') estado = '';

        let url;

        // 1. PRIORIDAD: Búsqueda por Texto (Cliente o Factura)
        if (termino && termino !== '') {
            // Asumiendo que tu backend tiene este endpoint: /api/sales/search/{term}
            url = `${API_URL}/search/${encodeURIComponent(termino)}`;
        }
        // 2. FILTRO POR ESTADO
        else {
            if (estado !== '') {
                url = `${API_URL}?state=${estado}`;
            } else {
                url = API_URL; // Trae todas
            }
        }

        const response = await fetch(url);
        if (!response.ok) throw new Error("Error al obtener ventas");

        const sales = await response.json();
        renderizarTabla(sales);

    } catch (error) {
        console.error(error);
        mostrarAlerta("Error al cargar ventas: " + error.message, "danger");
        renderizarTabla([]);
    }
}

// --- RENDERIZADO DE TABLA ---

function renderizarTabla(sales) {
    const tbody = document.getElementById('salesTableBody');
    tbody.innerHTML = '';

    if (!sales || sales.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted p-4">No se encontraron ventas.</td></tr>`;
        return;
    }

    let html = '';
    const currencyFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

    sales.forEach(s => {
        // --- 1. LÓGICA DE ESTADO ---
        let estadoClass = 'bg-secondary';
        let estadoText = s.estado;
        if (s.estado === 'FACTURADA') { estadoClass = 'bg-success'; estadoText = 'Facturada'; }
        else if (s.estado === 'ANULADA') { estadoClass = 'bg-warning text-dark'; estadoText = 'Anulada'; }
        else if (s.estado === 'ELIMINADA') { estadoClass = 'bg-danger'; estadoText = 'Eliminada'; }

        // --- 2. LÓGICA DE PAGO ---
        let pagoClass = 'bg-secondary';
        if (s.paymentMethods === 'EFECTIVO') pagoClass = 'bg-success';
        else if (s.paymentMethods === 'TRANSFERENCIA') pagoClass = 'bg-info text-dark';

        // --- 3. DEFINICIÓN DE BOTONES (Usando Backticks ` `) ---

        // A) Botón Ver (Ojo) - SIEMPRE VISIBLE
        const btnVer = `
            <button class="btn btn-sm btn-info text-white me-1"
                    onclick="verDetalleVenta(${s.id})"
                    title="Ver Detalle">
                <i class="bi bi-eye-fill"></i>
            </button>
        `;

        // B) Botón Editar (Lápiz) - SOLO SI ACTIVA
        let btnEditar = ''; // Por defecto vacío
        if (s.estado !== 'ANULADA' && s.estado !== 'ELIMINADA') {
            btnEditar = `
                <a class="btn btn-sm btn-warning me-1" href="/sales/formSales/${s.id}" title="Editar">
                    <i class="bi bi-pencil-square"></i>
                </a>
            `;
        }

        // C) Botón Anular (Basurero) - SOLO SI ACTIVA
        let btnAnular = '';
        if (s.estado !== 'ANULADA' && s.estado !== 'ELIMINADA') {
            btnAnular = `
                <button class="btn btn-sm btn-danger" onclick="anularVenta(${s.id})" title="Anular">
                     <i class="bi bi-trash"></i>
                </button>
            `;
        } else {
            // Si está anulada, mostramos un candado o texto
            btnAnular = `<span class="text-muted small ms-1"><i class="bi bi-lock-fill"></i> Bloqueada</span>`;
        }

        // --- 4. ARMADO DE LA FILA ---
        // Manejo seguro de nulos
        const cliente = s.client ? s.client.fullName : 'Cliente Desconocido';
        const vendedor = s.seller ? s.seller.username : 'N/A';
        const factura = s.invoice ? s.invoice.numberInvoice : 'N/A';

        // ¡AQUÍ ESTABA EL ERROR!
        // Ya no usamos ${classEditar} ni ${iconEditar}. Usamos directamente las variables btn...
        html += `
        <tr>
            <td>${s.id}</td>
            <td>${cliente}</td>
            <td>${currencyFormatter.format(s.total)}</td>
            <td>${formatearFecha(s.createdAt)}</td>
            <td><span class="badge ${pagoClass}">${s.paymentMethods}</span></td>
            <td>${vendedor}</td>
            <td>${factura}</td>
            <td><span class="badge ${estadoClass}">${estadoText}</span></td>
            <td>
                <div class="d-flex align-items-center">
                    ${btnVer}     ${btnEditar}  ${btnAnular}  </div>
            </td>
        </tr>`;
    });

    tbody.innerHTML = html;
}
//  FUNCIÓN PARA VER DETALLES
async function verDetalleVenta(id) {
    try {
        // 1. Mostrar carga visual (opcional) o limpiar datos previos
        document.getElementById('modalTablaProductos').innerHTML = '<tr><td colspan="4" class="text-center">Cargando...</td></tr>';

        // 2. Pedir datos al API
        const response = await fetch(`${API_URL}/${id}`);
        if (!response.ok) throw new Error("No se pudo cargar la venta");

        const sale = await response.json();

        // Formateadores
        const currencyFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });
        const dateOptions = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };

        // 3. Llenar Cabecera
        document.getElementById('modalVentaId').innerText = sale.id;
        document.getElementById('modalCliente').innerText = sale.client ? sale.client.fullName : 'N/A';
        document.getElementById('modalVendedor').innerText = sale.seller ? sale.seller.username : 'N/A';
        document.getElementById('modalFactura').innerText = sale.invoice ? sale.invoice.numberInvoice : '---';
        document.getElementById('modalFecha').innerText = new Date(sale.registrationDate).toLocaleDateString('es-ES', dateOptions);
        document.getElementById('modalPago').innerText = sale.paymentMethods;
        document.getElementById('modalTotal').innerText = currencyFormatter.format(sale.total);

        // Badge de Estado
        const spanEstado = document.getElementById('modalEstado');
        spanEstado.innerText = sale.estado;
        spanEstado.className = 'badge ' + (sale.estado === 'FACTURADA' ? 'bg-success' : 'bg-danger');

        // 4. Llenar Tabla de Productos
        const tbody = document.getElementById('modalTablaProductos');
        tbody.innerHTML = ''; // Limpiar spinner

        // IMPORTANTE: Ajusta 'sale.items' si tu lista se llama diferente en el JSON (ej. sale.detalles)
        if (sale.details && sale.details.length > 0) {
            let htmlProductos = '';

            sale.details.forEach(item => {
                // Cálculo seguro del subtotal por línea
                const precio = item.priceProduct || 0;
                const cantidad = item.quantity || 0;
                const subtotal = precio * cantidad;
                const nombreProd = item.productName ? item.productName : 'Producto eliminado';

                htmlProductos += `
                    <tr>
                        <td>${nombreProd}</td>
                        <td class="text-center">${cantidad}</td>
                        <td class="text-end">${currencyFormatter.format(precio)}</td>
                        <td class="text-end fw-bold">${currencyFormatter.format(subtotal)}</td>
                    </tr>
                `;
            });
            tbody.innerHTML = htmlProductos;
        } else {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">Sin detalles de productos</td></tr>';
        }

        // 5. Mostrar el Modal (Usando API de Bootstrap 5)
        const modalElement = document.getElementById('modalDetalleVenta');
        const modal = new bootstrap.Modal(modalElement);
        modal.show();

    } catch (error) {
        console.error(error);
        Swal.fire('Error', 'No se pudieron cargar los detalles', 'error');
    }
}

// Utilidad para fecha (ajusta según tu preferencia)
function formatearFecha(fechaString) {
    if (!fechaString) return '';
    const fecha = new Date(fechaString);
    return fecha.toLocaleDateString() + ' ' + fecha.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

function mostrarCargando() {
    const tbody = document.getElementById('salesTableBody');
    if(tbody) tbody.innerHTML = `<tr><td class="text-center p-4" colspan="9"><div class="spinner-border text-primary"></div></td></tr>`;
}

function mostrarAlerta(msg, tipo) {
    const container = document.getElementById('alertContainer');
    if(!container) return;
    container.innerHTML = `<div class="alert alert-${tipo} alert-dismissible fade show">${msg}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>`;
}

// --- TU FUNCIÓN ANULAR VENTA (INTACTA / MEJORADA) ---
async function anularVenta(id) {
    const result = await Swal.fire({
        title: '¿Anular venta #' + id + '?',
        text: "Se devolverán los productos al stock. ¡Esta acción no se puede deshacer!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, anular',
        cancelButtonText: 'Cancelar'
    });

    if (!result.isConfirmed) return;

    try {
        const response = await fetch(`/api/sales/${id}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            await Swal.fire('¡Anulada!', 'La venta ha sido anulada.', 'success');
            // Recargamos la tabla sin recargar la página
            listarVentas();
        } else {
            const data = await response.json();
            Swal.fire('Error', data.mensaje || 'No se pudo anular', 'error');
        }
    } catch (error) {
        console.error(error);
        Swal.fire('Error', 'Fallo de conexión con el servidor', 'error');
    }
}