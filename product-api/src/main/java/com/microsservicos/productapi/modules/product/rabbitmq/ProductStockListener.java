package com.microsservicos.productapi.modules.product.rabbitmq;


import com.microsservicos.productapi.modules.product.dto.ProductStockDTO;
import com.microsservicos.productapi.modules.product.service.ProductService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductStockListener {

    @Autowired
    private ProductService productService;

    @RabbitListener(queues = "${app-config.rabbit.queue.product-stock}")
    public void receiveProductStockMessage(ProductStockDTO product){
        productService.updateProductStock(product);
    }
}
