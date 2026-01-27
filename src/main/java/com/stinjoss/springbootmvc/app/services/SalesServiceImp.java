package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.*;
import com.stinjoss.springbootmvc.app.domain.entities.enums.PaymentMethods;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesSales;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.SalesDetailsRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.SalesRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.*;
import com.stinjoss.springbootmvc.app.exceptions.BusinessLogicException;
import com.stinjoss.springbootmvc.app.exceptions.ResourceNotFoundException;
import com.stinjoss.springbootmvc.app.repositories.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesServiceImp implements SalesService {

    private final SalesRepository repository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SalesDetailsRepository salesDetailsRepository;

    public SalesServiceImp(SalesRepository repository, InvoiceRepository invoiceRepository, ClientRepository clientRepository, UserRepository userRepository, ProductRepository productRepository, SalesDetailsRepository salesDetailsRepository) {
        this.repository = repository;
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.salesDetailsRepository = salesDetailsRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public SalesResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
    }

    @Transactional
    @Override
    public SalesResponseDTO save(SalesRequestDTO saleRequest, Long idVendedor) {
        if (saleRequest.getDetails() == null || saleRequest.getDetails().isEmpty()) {
            throw new BusinessLogicException("La venta debe tener al menos un producto.");
        }

        User seller = userRepository.findById(idVendedor)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor no encontrado con ID: " + idVendedor));
        
        Clients client = clientRepository.findById(saleRequest.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + saleRequest.getClientId()));

        Sales sale = new Sales();
        sale.setPaymentMethods(saleRequest.getPaymentMethods() != null ? saleRequest.getPaymentMethods() : PaymentMethods.EFECTIVO);
        sale.setState(StatesSales.FACTURADA);
        sale.setActive(true);
        sale.setClient(client);
        sale.setSeller(seller);

        generarFacturaParaVenta(sale);

        BigDecimal totalCalculado = BigDecimal.ZERO;
        for (SalesDetailsRequestDTO itemDto : saleRequest.getDetails()) {
            if (itemDto.getQuantity() <= 0) {
                throw new BusinessLogicException("La cantidad de cada producto debe ser mayor a cero.");
            }

            Products product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + itemDto.getProductId()));

            if (!product.isActive()) {
                throw new BusinessLogicException("El producto '" + product.getNameProducto() + "' no está disponible para la venta.");
            }
            if (product.getStock() < itemDto.getQuantity()) {
                throw new BusinessLogicException("Stock insuficiente para el producto: " + product.getNameProducto());
            }

            SalesDetails detail = new SalesDetails();
            detail.setProducts(product);
            detail.setQuantity(itemDto.getQuantity());
            detail.setPrice(product.getPrice());
            detail.setSale(sale);
            sale.getDetails().add(detail);

            totalCalculado = totalCalculado.add(product.getPrice().multiply(new BigDecimal(itemDto.getQuantity())));
            product.setStock(product.getStock() - itemDto.getQuantity());
        }

        sale.setTotal(totalCalculado);
        Sales savedSale = repository.save(sale);
        return mapToResponse(savedSale);
    }

    @Transactional
    @Override
    public void anularVenta(Long id) {
        Sales sale = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede anular. Venta no encontrada con ID: " + id));

        if (sale.getState() == StatesSales.ANULADA) {
            throw new BusinessLogicException("La venta ya se encuentra anulada.");
        }

        if (sale.getDetails() != null) {
            for (SalesDetails detail : sale.getDetails()) {
                Products product = detail.getProducts();
                if (product != null) {
                    product.setStock(product.getStock() + detail.getQuantity());
                }
            }
        }

        sale.setActive(false);
        sale.setState(StatesSales.ANULADA);
        repository.save(sale);
    }
    
    @Transactional(readOnly = true)
    @Override
    public List<SalesResponseDTO> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<SalesResponseDTO> findByState(String state) {
        if (state == null || state.isEmpty() || state.equalsIgnoreCase("ALL")) {
            return findAll();
        }
        try {
            StatesSales estadoEnum = StatesSales.valueOf(state.toUpperCase());
            return repository.findByState(estadoEnum).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<SalesResponseDTO> findByTerm(String term) {
        if (term == null || term.isBlank()) {
            return new ArrayList<>();
        }
        return repository.findByClientOrInvoice(term).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    @Override
    public DashboardStatsDTO getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.toLocalDate().withDayOfMonth(now.toLocalDate().lengthOfMonth()).atTime(LocalTime.MAX);

        BigDecimal totalToday = repository.sumTotalSalesByDateRange(startOfDay, endOfDay);
        if (totalToday == null) totalToday = BigDecimal.ZERO;
        Long countToday = repository.countSalesByDateRange(startOfDay, endOfDay);
        if (countToday == null) countToday = 0L;

        BigDecimal totalMonth = repository.sumTotalSalesByDateRange(startOfMonth, endOfMonth);
        if (totalMonth == null) totalMonth = BigDecimal.ZERO;

        Long lowStockCount = productRepository.countLowStockProducts();
        if (lowStockCount == null) lowStockCount = 0L;
        
        List<SalesResponseDTO> recentSales = repository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());

        List<TopProductDTO> topProducts = salesDetailsRepository.findTopSellingProducts(PageRequest.of(0, 5));
        
        LocalDateTime sevenDaysAgo = now.minusDays(7).toLocalDate().atStartOfDay();
        List<Object[]> chartRawData = repository.findDailySalesSum(sevenDaysAgo);
        
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        
        for (Object[] row : chartRawData) {
            labels.add(row[0].toString());
            data.add((BigDecimal) row[1]);
        }
        
        ChartDataDTO chartData = new ChartDataDTO(labels, data);

        return new DashboardStatsDTO(totalToday, countToday, totalMonth, lowStockCount, recentSales, topProducts, chartData);
    }

    private void generarFacturaParaVenta(Sales sale) {
        Invoices newInvoice = new Invoices();
        newInvoice.setPathInvoice("C:/sistema_pos/facturas/temp/pending.pdf");
        newInvoice.setNumberInvoice(generarSiguienteNumeroFactura());
        invoiceRepository.save(newInvoice);
        sale.setInvoice(newInvoice);
    }

    private String generarSiguienteNumeroFactura() {
        Invoices ultimaFactura = invoiceRepository.findTopByOrderByIdDesc();
        if (ultimaFactura == null) {
            return "FAC-000001";
        }
        String ultimoNumero = ultimaFactura.getNumberInvoice();
        String[] partes = ultimoNumero.split("-");
        int numeroInt = Integer.parseInt(partes[1]);
        numeroInt++;
        return "FAC-" + String.format("%06d", numeroInt);
    }

    private SalesResponseDTO mapToResponse(Sales entity) {
        SalesResponseDTO dto = new SalesResponseDTO();
        dto.setId(entity.getId());
        dto.setTotal(entity.getTotal());
        dto.setEstado(entity.getState());
        if (entity.getPaymentMethods() != null) {
            dto.setPaymentMethods(entity.getPaymentMethods());
        }
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getClient() != null) {
            ClientResponseDTO clientDto = new ClientResponseDTO();
            clientDto.setId(entity.getClient().getId());
            clientDto.setName(entity.getClient().getName());
            clientDto.setLastname(entity.getClient().getLastname());
            clientDto.setDni(entity.getClient().getDni());
            clientDto.setEmail(entity.getClient().getEmail());
            clientDto.setCellPhone(entity.getClient().getCellPhone());
            clientDto.setDirection(entity.getClient().getDirection());
            clientDto.setActive(entity.getClient().isActive());
            clientDto.setCreatedAt(entity.getClient().getCreatedAt());
            clientDto.setFullName(entity.getClient().getNombreCompleto());
            dto.setClient(clientDto);
        }

        if (entity.getSeller() != null) {
            UserResponseDTO sellerDto = new UserResponseDTO();
            sellerDto.setId(entity.getSeller().getId());
            sellerDto.setName(entity.getSeller().getName());
            sellerDto.setLastname(entity.getSeller().getLastname());
            sellerDto.setEmail(entity.getSeller().getEmail());
            sellerDto.setUsername(entity.getSeller().getUsername());
            sellerDto.setCreatedAt(entity.getSeller().getCreatedAt());
            dto.setSeller(sellerDto);
        }

        if (entity.getInvoice() != null) {
            InvoiceResponseDTO invDto = new InvoiceResponseDTO();
            invDto.setId(entity.getInvoice().getId());
            invDto.setNumberInvoice(entity.getInvoice().getNumberInvoice());
            invDto.setIssueDate(entity.getInvoice().getIssueDate());
            dto.setInvoice(invDto);
        }

        if (entity.getDetails() != null) {
            List<SalesDetailsResponseDTO> detailsDtoList = entity.getDetails().stream().map(d -> {
                SalesDetailsResponseDTO dDto = new SalesDetailsResponseDTO();
                if (d.getProducts() != null) {
                    dDto.setProductId(d.getProducts().getId());
                    dDto.setProductName(d.getProducts().getNameProducto());
                    dDto.setPriceProduct(d.getPrice());
                } else {
                    dDto.setProductId(0L);
                    dDto.setProductName("Producto no disponible");
                }
                dDto.setQuantity(d.getQuantity());
                return dDto;
            }).collect(Collectors.toList());
            dto.setDetails(detailsDtoList);
        } else {
            dto.setDetails(new ArrayList<>());
        }
        return dto;
    }
}
