package com.microsservicos.productapi.modules.sales.dto;


import com.microsservicos.productapi.modules.sales.enums.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesConfirmationDTO {

    private String salesId;
    private SaleStatus status;
}
