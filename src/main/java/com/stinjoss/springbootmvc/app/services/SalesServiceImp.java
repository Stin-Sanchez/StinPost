package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.*;
import com.stinjoss.springbootmvc.app.domain.entities.enums.PaymentMethods;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesSales;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.SalesDetailsRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.SalesRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.*;
import com.stinjoss.springbootmvc.app.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SalesServiceImp implements SalesService {

    private final SalesRepository repository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public SalesServiceImp(SalesRepository repository, InvoiceRepository invoiceRepository, ClientRepository clientRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.repository = repository;
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    @Override
    public List<SalesResponseDTO> findAll() {
        // Convertimos uno por uno usando stream y map.
        List<Sales> entities = repository.findAll();
        return entities.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    @Override
    public Optional<SalesResponseDTO> findById(Long id) {
        // Buscamos entidad, si existe, la convertimos a DTO
        return repository.findById(id).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SalesResponseDTO> findByState(String state) {
        List<Sales> lista;

        // 1. Si viene vacío, nulo o dice "Todos", devolvemos todo
        if (state == null || state.isEmpty() || state.equals("Todas")) {
            lista = repository.findAll();
        } else {
            try {
                // 2. Convertimos el String (ej: "AGOTADO") al Enum real
                StatesSales estadoEnum = StatesSales.valueOf(state);
                lista = repository.findByState(estadoEnum);
            } catch (IllegalArgumentException e) {
                // Si mandan un estado que no existe, devolvemos lista vacía
                return Collections.emptyList();
            }
        }

        // 3. Mapeamos a DTO
        return lista.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public SalesResponseDTO save(SalesRequestDTO saleRequest, Long idVendedor) {

        Sales saleDB = new Sales();

        // Validar si viene método de pago, sino por defecto EFECTIVO
        if (saleRequest.getPaymentMethods() != null) {
            saleDB.setPaymentMethods(saleRequest.getPaymentMethods());
        } else {
            saleDB.setPaymentMethods(PaymentMethods.EFECTIVO);
        }

        saleDB.setState(StatesSales.FACTURADA);
        saleDB.setActive(true);

        // Generamos factura
        generarFacturaParaVenta(saleDB);

        // 2. ASIGNAR CLIENTE
        Long idCliente = (saleRequest.getClientId() != null) ? saleRequest.getClientId() : 1L;
        Clients client = clientRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        saleDB.setClient(client);

        // 3. ASIGNAR VENDEDOR
        // Usamos el idVendedor que viene del parámetro
        User seller = userRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado (ID: " + idVendedor + ")"));
        saleDB.setSeller(seller);


        // 4. PROCESAR ITEMS Y DESCONTAR STOCK
        BigDecimal totalCalculado = BigDecimal.ZERO;

        for (SalesDetailsRequestDTO itemDto : saleRequest.getDetails()) {
            Products product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + itemDto.getProductId()));

            // Validar Stock
            if (product.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para: " + product.getNameProducto());
            }

            // Crear detalle
            SalesDetails detail = new SalesDetails();
            detail.setProducts(product);
            detail.setQuantity(itemDto.getQuantity());
            detail.setPrice(product.getPrice());
            detail.setSale(saleDB);

            saleDB.getDetails().add(detail);

            // Sumar Total
            totalCalculado = totalCalculado.add(product.getPrice().multiply(new BigDecimal(itemDto.getQuantity())));

            // DESCONTAR STOCK
            product.setStock(product.getStock() - itemDto.getQuantity());
            productRepository.save(product);
        }

        saleDB.setTotal(totalCalculado);

        // Guardamos y retornamos
        repository.save(saleDB);
        return mapToResponse(saleDB);
    }

    @Transactional
    @Override
    public List<SalesResponseDTO> findByTerm(String term) {
        if (term == null || term.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findByClientOrInvoice(term).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Optional<SalesResponseDTO> delete(Long id) {
        Optional<Sales> salesOptional = repository.findById(id);
        if (salesOptional.isPresent()) {
            // Convertimos la entidad borrada a DTO para mostrar qué se borró
            Sales sale = salesOptional.get();
            sale.setActive(false);
            sale.setState(StatesSales.ANULADA);
            repository.save(sale);
            return salesOptional.map(this::mapToResponse);
        }
        return Optional.empty();
    }

    @Transactional
    @Override
    public Optional<SalesResponseDTO> anularVenta(Long id) {
        // 1. Buscar la venta
        return repository.findById(id).map(sale -> {
            // 2. Verificar que no esté ya anulada
            if (!sale.isActive() || sale.getState() == StatesSales.ANULADA) {
                throw new RuntimeException("La venta ya está anulada.");// Ya está borrada, no hacemos nada
            }
            // 3. DEVOLVER STOCK (Si la venta tiene detalles guardados)
            // Es importante verificar si getDetails() no es null ni está vacío
            if (sale.getDetails() != null) {
                for (SalesDetails detail : sale.getDetails()) {

                    //Si tenemos datos basura continuamos
                    if (detail == null) {
                        continue;
                    }
                    Products product = detail.getProducts();
                    if (product != null) {
                        //// Solo si el producto existe, sumamos la cantidad vendida de nuevo al stock actual
                        product.setStock(product.getStock() + detail.getQuantity());
                        productRepository.save(product);
                    } else {
                        System.err.println(" ALERTA: Venta " + id + " tiene un detalle sin producto asociado. Se omitió devolución de stock.");
                    }
                }
            }

            // 4. BORRADO LÓGICO
            sale.setActive(false);
            sale.setState(StatesSales.ANULADA);
            //Poner el total en 0 para que no sume

            // 4. Guardamos y convertimos a DTO
            Sales savedSale = repository.save(sale);
            return mapToResponse(savedSale);
        });
    }


    //  MAPPER convierte : Entidad -> ResponseDTO)
    private SalesResponseDTO mapToResponse(Sales entity) {
        SalesResponseDTO dto = new SalesResponseDTO();
        dto.setId(entity.getId());
        dto.setTotal(entity.getTotal());
        dto.setEstado(entity.getState());

        // Validamos que paymentMethods no sea null (por si acaso)
        if (entity.getPaymentMethods() != null) {
            dto.setPaymentMethods(entity.getPaymentMethods());
        }

        dto.setCreatedAt(entity.getCreatedAt());


        // --- CLIENTE (Llenamos todo) ---
        if (entity.getClient() != null) {
            ClientResponseDTO clientDto = new ClientResponseDTO();
            clientDto.setId(entity.getClient().getId());
            clientDto.setName(entity.getClient().getName());
            clientDto.setLastname(entity.getClient().getLastname()); // <--- Nuevo
            clientDto.setDni(entity.getClient().getDni());
            clientDto.setEmail(entity.getClient().getEmail());       // <--- Nuevo
            clientDto.setCellPhone(entity.getClient().getCellPhone()); // <--- Nuevo
            clientDto.setDirection(entity.getClient().getDirection()); // <--- Nuevo
            clientDto.setActive(entity.getClient().isActive());
            clientDto.setCreatedAt(entity.getClient().getCreatedAt());
            clientDto.setFullName(entity.getClient().getNombreCompleto());

            dto.setClient(clientDto);
        }

        // --- VENDEDOR (Llenamos todo MENOS password) ---
        if (entity.getSeller() != null) {
            UserResponseDTO sellerDto = new UserResponseDTO();
            sellerDto.setId(entity.getSeller().getId());
            sellerDto.setName(entity.getSeller().getName());
            sellerDto.setLastname(entity.getSeller().getLastname()); // <--- Nuevo
            sellerDto.setEmail(entity.getSeller().getEmail());       // <--- Nuevo
            sellerDto.setUsername(entity.getSeller().getUsername());
            // sellerDto.setRole(entity.getSeller().getRole().toString()); // <--- Descomenta si tienes roles
            sellerDto.setCreatedAt(entity.getSeller().getCreatedAt()); // <--- Nuevo

            dto.setSeller(sellerDto);
        }

        // --- FACTURA ---
        if (entity.getInvoice() != null) {
            InvoiceResponseDTO invDto = new InvoiceResponseDTO();
            invDto.setId(entity.getInvoice().getId());
            invDto.setNumberInvoice(entity.getInvoice().getNumberInvoice());
            invDto.setIssueDate(entity.getInvoice().getIssueDate());

            // Lógica para URL de descarga (opcional)
            // invDto.setDownloadUrl("/api/invoices/download/" + entity.getInvoice().getNumberInvoice());

            dto.setInvoice(invDto);
        }

        // 1. Verificamos si la lista de detalles existe
        if (entity.getDetails() != null) {
            List<SalesDetailsResponseDTO> detailsDtoList = entity.getDetails().stream().map(d -> {
                SalesDetailsResponseDTO dDto = new SalesDetailsResponseDTO();

                // 2. PROTECCIÓN CONTRA PRODUCTO NULL
                if (d.getProducts() != null) {
                    dDto.setProductId(d.getProducts().getId());
                    dDto.setProductName(d.getProducts().getNameProducto());
                    dDto.setPriceProduct(d.getPrice());
                } else {
                    // Si Hibernate ocultó el producto (por el @Where), ponemos valores por defecto
                    dDto.setProductId(0L);
                    dDto.setProductName("Producto no disponible (Eliminado o Inactivo)");
                }

                dDto.setQuantity(d.getQuantity());


                return dDto;
            }).collect(Collectors.toList());

            dto.setDetails(detailsDtoList);
        } else {
            // Si no hay detalles, devolvemos lista vacía para no romper el frontend
            dto.setDetails(new ArrayList<>());
        }

        return dto;
    }

    @Transactional
    @Override
    public Long count() {
        return repository.count();
    }

    // --- MÉTODO AUXILIAR PARA CALCULAR EL NÚMERO "FAC-000001" ---
    private String generarSiguienteNumeroFactura() {
        Invoices ultimaFactura = invoiceRepository.findTopByOrderByIdDesc();

        if (ultimaFactura == null) {
            // Si es la primera venta de la historia
            return "FAC-000001";
        }

        // Si ya existen, tomamos el último (ej: FAC-000015)
        String ultimoNumero = ultimaFactura.getNumberInvoice();

        // Separamos "FAC" de "000015"
        String[] partes = ultimoNumero.split("-");
        String numeroStr = partes[1]; // "000015"

        // Convertimos a entero y sumamos 1
        int numeroInt = Integer.parseInt(numeroStr);
        numeroInt++;

        // Volvemos a formatear con ceros a la izquierda (ej: 000016)
        return "FAC-" + String.format("%06d", numeroInt);
    }

    // Método auxiliar para la factura (extraído para limpieza)
    private void generarFacturaParaVenta(Sales sale) {
        Invoices newInvoice = new Invoices();
        newInvoice.setPathInvoice("C:/sistema_pos/facturas/temp/pending.pdf");
        // Lógica de número secuencial
        newInvoice.setNumberInvoice(generarSiguienteNumeroFactura());
        invoiceRepository.save(newInvoice);
        sale.setInvoice(newInvoice);
    }
}
