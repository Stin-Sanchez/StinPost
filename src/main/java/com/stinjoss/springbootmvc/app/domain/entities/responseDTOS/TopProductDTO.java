package com.stinjoss.springbootmvc.app.domain.entities.responseDTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductDTO {
    private String productName;
    private Long totalSold;
}
