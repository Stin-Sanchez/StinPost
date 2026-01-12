  // Direccion del API
  const API_URL = '/api/products';

  let searchTimeout = null;

  document.addEventListener("DOMContentLoaded", () => {
      listarProductos();

      const inputSearch = document.getElementById('inputSearch');
      const inputFilter = document.getElementById('inputFilter'); // Asegúrate que tu HTML tenga este ID

      if (inputSearch) inputSearch.addEventListener('input', handleSearch);
      // Cambiamos 'change' para que reaccione al instante
      if (inputFilter) inputFilter.addEventListener('change', handleFilterChange);
  });

  // Función unificada para manejar la carga inicial y el filtrado
  function listarProductos() {
      // Al iniciar, llamamos a buscarProductos sin término,
      // ella sola se encargará de leer el select si está seleccionado algo.
      buscarProductos('');
  }

  function handleSearch(event) {
      const searchTerm = event.target.value.trim();
      if (searchTimeout) clearTimeout(searchTimeout);
      searchTimeout = setTimeout(() => {
          buscarProductos(searchTerm);
      }, 500);
  }

  function handleFilterChange() {
      // Cuando cambian el select, simplemente volvemos a buscar.
      // Pasamos el término actual del buscador para no perder lo que el usuario escribió.
      const searchTerm = document.getElementById('inputSearch').value.trim();
      buscarProductos(searchTerm);
  }

  async function buscarProductos(termino) {
      try {
          mostrarCargando();

          // 1. OBTENEMOS EL VALOR DEL SELECT
          const estadoSelect = document.getElementById('inputFilter');
          let estado = estadoSelect ? estadoSelect.value : '';

          if (estado === 'Todos' || estado === 'ALL') {
                      estado = '';
                  }

          let url;

          // 2. LÓGICA DE URL (Backend)
          if (termino && termino !== '') {
              // Caso A: Búsqueda por Texto (Tu endpoint /search/{term})
              // Nota: Tu endpoint de búsqueda actual no soporta filtro de estado combinado,
              // así que busca en todo.
              url = `${API_URL}/search/${encodeURIComponent(termino)}`;
          } else {
              // Caso B: Listado Normal o Filtrado por Estado
              // Si hay estado seleccionado (y no es 'Todos' o vacio), lo mandamos al backend
              if (estado !== '') {
                  url = `${API_URL}?state=${estado}`; // Consumo del @RequestParam
              } else {
                  url = API_URL; // Trae todos
              }
          }

          const response = await fetch(url);
          if (!response.ok) throw new Error("Error al obtener datos");

          const products = await response.json();

          // 3. RENDERIZADO DIRECTO (Sin filtrar en JS)
          renderizarTabla(products);

      } catch (error) {
          console.error(error);
          mostrarAlerta("Error: " + error.message, "danger");
          renderizarTabla([]); // Limpia la tabla si hay error
      }
  }

  function renderizarTabla(products) {
      const tbody = document.getElementById('productTableBody');
      tbody.innerHTML = '';

      if (!products || products.length === 0) {
          tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted p-4">No se encontraron productos.</td></tr>`;
          return;
      }

      let html = '';
      const currencyFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

      products.forEach(p => {
          const estadoInfo = obtenerInfoEstado(p.state);

          const imagenHtml = (p.image && p.image.trim() !== '')
              ? `<img src="${p.image}" class="img-thumbnail" style="width: 50px; height: 50px; object-fit: cover;" alt="Prod">`
              : `<span class="badge bg-secondary">Sin imagen</span>`;

          // Usamos la clase del estado para el stock también (coherencia visual)
          const stockClass = estadoInfo.clase;

          html += `
          <tr>
              <td>${p.id}</td>
              <td>${imagenHtml}</td>
              <td>${p.code}</td>
              <td>${p.nameProducto}</td>
              <td>${p.marca}</td>
              <td><span class="fw-bold text-success">${currencyFormatter.format(p.price)}</span></td>
              <td><span class="badge ${stockClass}">${p.stock}</span></td>
              <td><span class="badge ${estadoInfo.clase}">
                  <i class="bi ${estadoInfo.icon} me-1"></i> ${estadoInfo.texto}
              </span></td>
              <td>
                  <a class="btn btn-sm btn-warning me-1" href="/products/formProducts/${p.id}" title="Editar">
                      <i class="bi bi-pencil-square"></i>
                  </a>
                  <button class="btn btn-sm btn-danger" onclick="eliminarProducto(${p.id})" title="Eliminar">
                      <i class="bi bi-trash"></i>
                  </button>
              </td>
          </tr>`;
      });
      tbody.innerHTML = html;
  }

  // Mantenemos tu función de colores intacta
  function obtenerInfoEstado(estadoEnum) {
      switch (estadoEnum) {
          case 'DISPONIBLE': return { clase: 'bg-success', texto: 'Disponible', icon: 'bi-check-circle' };
          case 'CASI_AGOTADO': return { clase: 'bg-orange text-white', texto: 'Casi Agotado', icon: 'bi-hourglass-split' }; // Ajusté texto blanco
          case 'CON_STOCK_MINIMO': return { clase: 'bg-warning text-dark', texto: 'Stock Mínimo', icon: 'bi-exclamation-triangle' };
          case 'AGOTADO': return { clase: 'bg-danger', texto: 'Agotado', icon: 'bi-x-circle' };
          default: return { clase: 'bg-secondary', texto: estadoEnum, icon: 'bi-question-circle' };
      }
  }

  // ... Resto de funciones (eliminarProducto, mostrarAlerta, mostrarCargando) iguales ...
  async function eliminarProducto(id) {
      if (!confirm('¿Estás seguro de eliminar este producto?')) return;
      try {
          const response = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
          if (response.ok) {
              mostrarAlerta("Producto eliminado", "success");
              listarProductos();
          } else {
              mostrarAlerta("Error al eliminar", "danger");
          }
      } catch (e) { mostrarAlerta("Error de conexión", "danger"); }
  }

  function mostrarAlerta(msg, tipo) {
      // Tu lógica de alerta existente
      const container = document.getElementById('alertContainer');
      if(!container) return; // Protección por si no existe
      container.innerHTML = `<div class="alert alert-${tipo} alert-dismissible fade show">${msg}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>`;
      setTimeout(() => {
          const alert = bootstrap.Alert.getOrCreateInstance(container.querySelector('.alert'));
          if(alert) alert.close();
      }, 3000);
  }

  function mostrarCargando() {
      const tbody = document.getElementById('productTableBody');
      if(tbody) tbody.innerHTML = `<tr><td class="text-center p-4" colspan="9"><div class="spinner-border text-primary"></div></td></tr>`;
  }