package com.stinjoss.springbootmvc.app.domain.entities.responseDTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private BigDecimal totalSalesToday;
    private Long salesCountToday;
    private BigDecimal totalSalesMonth;
    private Long lowStockCount;
    
    // Listas
    private List<SalesResponseDTO> recentSales;
    private List<TopProductDTO> topProducts;
    
    // Gráfico
    private ChartDataDTO salesChart;
}
