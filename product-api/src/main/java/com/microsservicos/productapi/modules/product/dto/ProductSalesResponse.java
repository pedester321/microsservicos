package com.microsservicos.productapi.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsservicos.productapi.modules.category.dto.CategoryResponse;
import com.microsservicos.productapi.modules.product.model.Product;
import com.microsservicos.productapi.modules.supplier.dto.SupplierResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSalesResponse {

    private Integer id;
    private String name;
    private SupplierResponse supplier;
    private CategoryResponse category;
    private Integer quantityAvailable;
    @JsonProperty("created_at")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    private List<String> sales;

    public static ProductSalesResponse of(Product product,
                                     List<String> sales){
        return ProductSalesResponse
                .builder()
                .id(product.getId())
                .name(product.getName())
                .supplier(SupplierResponse.of(product.getSupplier()))
                .category(CategoryResponse.of(product.getCategory()))
                .quantityAvailable(product.getQuantityAvailable())
                .createdAt(product.getCreatedAt())
                .sales(sales)
                .build();
    }
}
