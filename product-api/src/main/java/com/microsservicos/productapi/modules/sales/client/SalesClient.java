package com.microsservicos.productapi.modules.sales.client;

import com.microsservicos.productapi.modules.sales.dto.SalesProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(
        name = "salesClient",
        contextId = "salesClient",
        url = "${app-config.services.sales}"
)
public interface SalesClient {

    @GetMapping("/api/orders/product/{productId}")
    Optional<SalesProductResponse> getSalesByProductId(@PathVariable Integer productId);
}
