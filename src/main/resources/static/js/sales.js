document.addEventListener('DOMContentLoaded', function() {
    // --- LÓGICA PARA LA PÁGINA DE LISTADO DE VENTAS ---
    const salesTableBody = document.getElementById('salesTableBody');
    if (salesTableBody) {
        cargarVentas();

        const inputSearch = document.getElementById('inputSearch');
        if (inputSearch) {
            inputSearch.addEventListener('input', debounce(function (e) {
                const termino = e.target.value.trim();
                if (termino.length > 0) {
                    buscarVentas(termino);
                } else {
                    cargarVentas();
                }
            }, 300));
        }

        const inputFilter = document.getElementById('inputFilter');
        if (inputFilter) {
            inputFilter.addEventListener('change', function (e) {
                const estado = e.target.value;
                if (estado === 'ALL') {
                    cargarVentas();
                } else {
                    filtrarPorEstado(estado);
                }
            });
        }
    }

    // --- LÓGICA PARA EL FORMULARIO DE NUEVA VENTA ---
    const formSales = document.getElementById('searchClient'); // Un elemento que solo existe en el form
    if (formSales) {
        initializeFormSales();
    }
});

// --- FUNCIONES PARA EL LISTADO DE VENTAS ---
async function cargarVentas() {
    try {
        const response = await fetch('/api/sales');
        if (response.ok) {
            const ventas = await response.json();
            renderizarTablaVentas(ventas);
        }
    } catch (error) { console.error(error); }
}

async function buscarVentas(termino) {
    try {
        const response = await fetch(`/api/sales/search/${termino}`);
        if (response.ok) {
            const ventas = await response.json();
            renderizarTablaVentas(ventas);
        }
    } catch (error) { console.error(error); }
}

async function filtrarPorEstado(estado) {
    try {
        const response = await fetch(`/api/sales?state=${estado}`);
        if (response.ok) {
            const ventas = await response.json();
            renderizarTablaVentas(ventas);
        }
    } catch (error) { console.error(error); }
}

async function anularVenta(id) {
    const result = await Swal.fire({
        title: '¿Anular venta?',
        text: "Esta acción devolverá el stock de los productos.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, anular',
        cancelButtonText: 'Cancelar'
    });

    if (result.isConfirmed) {
        try {
            const response = await fetch(`/api/sales/${id}`, { method: 'DELETE' });
            if (response.ok) {
                Swal.fire('Anulada!', 'La venta ha sido anulada.', 'success');
                cargarVentas();
            } else {
                Swal.fire('Error', 'No se pudo anular la venta.', 'error');
            }
        } catch (error) {
            console.error(error);
            Swal.fire('Error', 'Error de conexión.', 'error');
        }
    }
}

async function verDetalle(id) {
    try {
        const response = await fetch(`/api/sales/${id}`);
        if (response.ok) {
            const venta = await response.json();
            llenarModalDetalle(venta);
            const modal = new bootstrap.Modal(document.getElementById('modalDetalleVenta'));
            modal.show();
        }
    } catch (error) { console.error(error); }
}

function llenarModalDetalle(venta) {
    document.getElementById('modalVentaId').innerText = venta.id;
    document.getElementById('modalCliente').innerText = venta.client ? venta.client.fullName : 'N/A';
    document.getElementById('modalVendedor').innerText = venta.seller ? venta.seller.username : 'N/A';
    document.getElementById('modalFactura').innerText = venta.invoice ? venta.invoice.numberInvoice : 'N/A';
    document.getElementById('modalFecha').innerText = new Date(venta.createdAt).toLocaleString();
    document.getElementById('modalPago').innerText = venta.paymentMethods;
    document.getElementById('modalEstado').innerText = venta.estado;

    const tbody = document.getElementById('modalTablaProductos');
    tbody.innerHTML = '';

    if (venta.details) {
        venta.details.forEach(d => {
            const subtotal = d.quantity * d.priceProduct;
            tbody.innerHTML += `
                <tr>
                    <td>${d.productName}</td>
                    <td class="text-center">${d.quantity}</td>
                    <td class="text-end">$${d.priceProduct.toFixed(2)}</td>
                    <td class="text-end">$${subtotal.toFixed(2)}</td>
                </tr>
            `;
        });
    }
    document.getElementById('modalTotal').innerText = '$' + venta.total.toFixed(2);
}

function renderizarTablaVentas(ventas) {
    const tbody = document.getElementById('salesTableBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (ventas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted">No se encontraron ventas</td></tr>`;
        return;
    }

    ventas.forEach(v => {
        let badgeClass = 'bg-secondary';
        if (v.estado === 'FACTURADA') badgeClass = 'bg-success';
        else if (v.estado === 'ANULADA') badgeClass = 'bg-danger';

        const row = `
            <tr>
                <td>${v.id}</td>
                <td>${v.client ? v.client.fullName : 'N/A'}</td>
                <td>$${v.total.toFixed(2)}</td>
                <td>${new Date(v.createdAt).toLocaleDateString()}</td>
                <td>${v.paymentMethods}</td>
                <td>${v.seller ? v.seller.username : 'N/A'}</td>
                <td>${v.invoice ? v.invoice.numberInvoice : 'N/A'}</td>
                <td><span class="badge ${badgeClass}">${v.estado}</span></td>
                <td>
                    <button onclick="verDetalle(${v.id})" class="btn btn-sm btn-info text-white" title="Ver Detalle"><i class="bi bi-eye"></i></button>
                    ${v.estado !== 'ANULADA' ? `<button onclick="anularVenta(${v.id})" class="btn btn-sm btn-danger" title="Anular"><i class="bi bi-x-circle"></i></button>` : ''}
                </td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}


// --- FUNCIONES PARA EL FORMULARIO DE NUEVA VENTA ---
function initializeFormSales() {
    let cart = [];
    let productoSeleccionadoTemp = null;
    const moneyFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

    const fechaHoyElement = document.getElementById('fechaHoy');
    const inputClient = document.getElementById('searchClient');
    const listClient = document.getElementById('clientSuggestions');
    const btnResetClient = document.getElementById('btnResetClient');
    const inputProduct = document.getElementById('searchProduct');
    const listProduct = document.getElementById('productSuggestions');
    const btnAgregar = document.getElementById('btnAgregarLinea');
    const btnGuardar = document.getElementById('btnGuardarVenta');

    if (fechaHoyElement) fechaHoyElement.innerText = new Date().toLocaleDateString();
    if (inputClient) inputClient.addEventListener('input', debounce(handleClientSearch, 300));
    if (btnResetClient) btnResetClient.addEventListener('click', resetCliente);
    if (inputProduct) inputProduct.addEventListener('input', debounce(handleProductSearch, 300));
    if (btnAgregar) btnAgregar.addEventListener('click', agregarLinea);
    if (btnGuardar) btnGuardar.addEventListener('click', guardarVenta);

    document.addEventListener('click', (e) => {
        if (listClient && e.target.id !== 'searchClient') listClient.style.display = 'none';
        if (listProduct && e.target.id !== 'searchProduct') listProduct.style.display = 'none';
    });

    async function handleClientSearch(e) {
        const term = e.target.value.trim();
        if (term.length < 2) { listClient.style.display = 'none'; return; }
        try {
            const response = await fetch(`/api/clients/search/${term}`);
            if (response.ok) renderClientSuggestions(await response.json());
        } catch (error) { console.error("Error buscando clientes", error); }
    }

    function renderClientSuggestions(clients) {
        listClient.innerHTML = '';
        if (clients.length === 0) { listClient.style.display = 'none'; return; }
        clients.forEach(c => {
            const li = document.createElement('li');
            li.className = 'list-group-item list-group-item-action';
            li.style.cursor = 'pointer';
            li.innerHTML = `<div><strong>${c.name} ${c.lastname}</strong></div><small class="text-muted">DNI: ${c.dni}</small>`;
            li.addEventListener('click', () => seleccionarCliente(c));
            listClient.appendChild(li);
        });
        listClient.style.display = 'block';
    }

    function seleccionarCliente(c) {
        document.getElementById('clientId').value = c.id;
        document.getElementById('clientNameDisplay').innerText = `${c.name} ${c.lastname}`;
        btnResetClient.classList.remove('d-none');
        inputClient.value = '';
        listClient.style.display = 'none';
    }

    function resetCliente() {
        document.getElementById('clientId').value = '';
        document.getElementById('clientNameDisplay').innerText = 'Consumidor Final';
        btnResetClient.classList.add('d-none');
    }

    async function handleProductSearch(e) {
        const term = e.target.value.trim();
        if (term.length < 2) { listProduct.style.display = 'none'; return; }
        try {
            const response = await fetch(`/api/products/search/${term}?size=5`);
            if (response.ok) renderProductSuggestions((await response.json()).content);
        } catch (error) { console.error("Error buscando productos", error); }
    }

    function renderProductSuggestions(products) {
        listProduct.innerHTML = '';
        if (products.length === 0) { listProduct.style.display = 'none'; return; }
        products.forEach(p => {
            const li = document.createElement('li');
            li.className = 'list-group-item list-group-item-action';
            li.style.cursor = 'pointer';
            let stockBadge = p.stock > 0 ? `<span class="badge bg-success">${p.stock} un.</span>` : `<span class="badge bg-danger">Agotado</span>`;
            li.innerHTML = `<div class="d-flex justify-content-between align-items-center"><span>${p.nameProducto}</span>${stockBadge}</div><small class="text-muted">Código: ${p.code} | Precio: $${p.price}</small>`;
            if (p.stock > 0) li.addEventListener('click', () => seleccionarProducto(p));
            listProduct.appendChild(li);
        });
        listProduct.style.display = 'block';
    }

    function seleccionarProducto(p) {
        document.getElementById('productId').value = p.id;
        document.getElementById('searchProduct').value = p.nameProducto;
        document.getElementById('productPrice').value = p.price;
        document.getElementById('productQuantity').value = 1;
        document.getElementById('productQuantity').focus();
        productoSeleccionadoTemp = p;
        listProduct.style.display = 'none';
    }

    function agregarLinea() {
        const id = document.getElementById('productId').value;
        const name = document.getElementById('searchProduct').value;
        const price = parseFloat(document.getElementById('productPrice').value);
        const quantity = parseInt(document.getElementById('productQuantity').value);

        if (!id) { alert("Debes buscar y seleccionar un producto de la lista."); return; }
        if (isNaN(quantity) || quantity <= 0) { alert("La cantidad debe ser mayor a 0"); return; }
        if (productoSeleccionadoTemp && quantity > productoSeleccionadoTemp.stock) {
            alert(`Stock insuficiente. Solo quedan ${productoSeleccionadoTemp.stock} unidades.`);
            return;
        }

        const indexExistente = cart.findIndex(item => item.productId == id);
        if (indexExistente !== -1) {
            if (productoSeleccionadoTemp && (cart[indexExistente].quantity + quantity) > productoSeleccionadoTemp.stock) {
                alert("No puedes agregar más de este producto, superas el stock disponible.");
                return;
            }
            cart[indexExistente].quantity += quantity;
            cart[indexExistente].subtotal = cart[indexExistente].quantity * price;
        } else {
            cart.push({ productId: id, name: name, price: price, quantity: quantity, subtotal: price * quantity });
        }
        renderizarTablaCarrito();
        limpiarInputsProducto();
    }

    function renderizarTablaCarrito() {
        const tbody = document.getElementById('detailsTableBody');
        tbody.innerHTML = '';
        let totalGeneral = 0;

        if (cart.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted p-4">El carrito está vacío.</td></tr>`;
            document.getElementById('granTotal').innerText = "$0.00";
            return;
        }

        cart.forEach((item, index) => {
            totalGeneral += item.subtotal;
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.name}</td>
                <td class="text-center">$${item.price.toFixed(2)}</td>
                <td class="text-center">${item.quantity}</td>
                <td class="text-end">$${item.subtotal.toFixed(2)}</td>
                <td class="text-center"></td>`;
            const deleteButton = document.createElement('button');
            deleteButton.className = 'btn btn-sm btn-outline-danger';
            deleteButton.innerHTML = '<i class="bi bi-trash"></i>';
            deleteButton.addEventListener('click', () => eliminarDelCarrito(index));
            row.cells[4].appendChild(deleteButton);
            tbody.appendChild(row);
        });
        document.getElementById('granTotal').innerText = moneyFormatter.format(totalGeneral);
    }

    function eliminarDelCarrito(index) {
        cart.splice(index, 1);
        renderizarTablaCarrito();
    }

    function limpiarInputsProducto() {
        document.getElementById('productId').value = '';
        document.getElementById('searchProduct').value = '';
        document.getElementById('productPrice').value = '';
        document.getElementById('productQuantity').value = 1;
        productoSeleccionadoTemp = null;
    }

    async function guardarVenta() {
        if (cart.length === 0) { alert("No puedes realizar una venta vacía."); return; }

        const originalText = btnGuardar.innerHTML;
        btnGuardar.disabled = true;
        btnGuardar.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Procesando...`;

        const ventaPayload = {
            clientId: document.getElementById('clientId').value ? parseInt(document.getElementById('clientId').value) : null,
            paymentMethods: "EFECTIVO",
            details: cart.map(item => ({ productId: parseInt(item.productId), quantity: item.quantity }))
        };

        try {
            const response = await fetch('/api/sales', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(ventaPayload)
            });

            if (response.ok) {
                const ventaCreada = await response.json();
                alert(`✅ Venta #${ventaCreada.id} realizada con éxito!`);
                cart = [];
                renderizarTablaCarrito();
                resetCliente();
            } else {
                const errorData = await response.json();
                alert(`❌ Error al guardar venta: ${errorData.message || 'Error desconocido'}`);
            }
        } catch (error) {
            console.error(error);
            alert("❌ Error de conexión con el servidor");
        } finally {
            btnGuardar.disabled = false;
            btnGuardar.innerHTML = originalText;
        }
    }
}

const debounce = (func, wait) => {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
};
