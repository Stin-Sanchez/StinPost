package com.stinjoss.springbootmvc.app.domain.entities.responseDTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataDTO {
    private List<String> labels;   // Fechas (Eje X)
    private List<BigDecimal> data; // Totales (Eje Y)
}
