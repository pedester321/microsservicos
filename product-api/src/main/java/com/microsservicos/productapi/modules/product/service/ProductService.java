package com.microsservicos.productapi.modules.product.service;

import com.microsservicos.productapi.config.exception.SuccessResponse;
import com.microsservicos.productapi.config.exception.ValidationException;
import com.microsservicos.productapi.modules.category.service.CategoryService;
import com.microsservicos.productapi.modules.product.dto.*;
import com.microsservicos.productapi.modules.product.model.Product;
import com.microsservicos.productapi.modules.product.repository.ProductRepository;
import com.microsservicos.productapi.modules.sales.client.SalesClient;
import com.microsservicos.productapi.modules.sales.dto.SalesConfirmationDTO;
import com.microsservicos.productapi.modules.sales.enums.SaleStatus;
import com.microsservicos.productapi.modules.sales.rabbitmq.SalesConfirmationSender;
import com.microsservicos.productapi.modules.supplier.service.SupplierService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class ProductService {

    private static final Integer ZERO = 0;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    @Lazy
    private SupplierService supplierService;
    @Autowired
    @Lazy
    private CategoryService categoryService;
    @Autowired
    private SalesConfirmationSender salesConfirmationSender;
    @Autowired
    private SalesClient salesClient;

    public ProductResponse save(ProductRequest request) {
        validateInformedProductData(request);
        validateInformedCategoryAndSupplierId(request);
        var category = categoryService.findById(request.getCategoryId());
        var supplier = supplierService.findById(request.getSupplierId());
        var product = productRepository.save(Product.of(request, supplier, category));
        return ProductResponse.of(product);
    }

    public ProductResponse update(ProductRequest request,
                                  Integer id) {
        validateInformedProductData(request);
        validateInformedProductId(id);
        validateInformedCategoryAndSupplierId(request);
        var category = categoryService.findById(request.getCategoryId());
        var supplier = supplierService.findById(request.getSupplierId());
        var product = Product.of(request, supplier, category);
        product.setId(id);
        productRepository.save(product);
        return ProductResponse.of(product);
    }

    public List<ProductResponse> findAll() {
        return productRepository
                .findAll()
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findByName(String name) {
        if (isEmpty(name)) {
            throw new ValidationException("The product name must be informed.");
        }
        return productRepository
                .findByNameIgnoreCaseContaining(name)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findByCategoryId(Integer categoryId) {
        if (isEmpty(categoryId)) {
            throw new ValidationException("The product category ID must be informed.");
        }
        return productRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findBySupplierId(Integer supplierId) {
        if (isEmpty(supplierId)) {
            throw new ValidationException("The product supplier ID must be informed.");
        }
        return productRepository
                .findBySupplierId(supplierId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public ProductResponse findByIdResponse(Integer id) {
        return ProductResponse.of(findById(id));
    }

    public Product findById(Integer id) {
        if (isEmpty(id)) {
            throw new ValidationException("The product ID must be informed.");
        }
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException("There's no product for the given ID."));
    }

    public Boolean existsByCategoryId(Integer categoryId) {
        return productRepository.existsByCategoryId(categoryId);
    }

    public Boolean existsBySupplierId(Integer supplierId) {
        return productRepository.existsBySupplierId(supplierId);
    }

    public SuccessResponse delete(Integer id) {
        validateInformedProductId(id);
        productRepository.deleteById(id);
        return SuccessResponse.create("The product has been deleted");
    }

    private void validateInformedProductData(ProductRequest request) {
        if (isEmpty(request.getName())) {
            throw new ValidationException("Product name was not informed.");
        }
        if (isEmpty(request.getQuantityAvailable())) {
            throw new ValidationException("Product quantity was not informed.");
        }
        if (request.getQuantityAvailable() <= ZERO) {
            throw new ValidationException("Product quantity should be bigger than zero.");
        }
    }

    @Transactional
    public void updateProductStock(ProductStockDTO product) {

        var productsForUpdate = new ArrayList<Product>();
        try {
            validateStockUpdateData(product);
            product.getProducts().forEach(salesProduct -> {
                var existingProduct = findById(salesProduct.getProductId());
                if(salesProduct.getQuantity() > existingProduct.getQuantityAvailable()){
                    throw new ValidationException(String.format("The product %s is out of stock.", existingProduct.getId()));
                }
                existingProduct.updateStock(salesProduct.getQuantity());
                productsForUpdate.add(existingProduct);
            });
            if(!isEmpty(productsForUpdate)){
                productRepository.saveAll(productsForUpdate);
                var approvedMessage = new SalesConfirmationDTO(product.getSalesId(), SaleStatus.APPROVED);
                salesConfirmationSender.sendSalesConfirmationMessage(approvedMessage);
            }
        } catch (Exception e) {
            log.error("Error while trying to update stock for message with error: {}", e.getMessage(), e);
            var rejectedMessage = new SalesConfirmationDTO(product.getSalesId(), SaleStatus.REJECTED );
            salesConfirmationSender.sendSalesConfirmationMessage(rejectedMessage);
        }
    }

    public ProductSalesResponse findProductSales(Integer id){
        var product = findById(id);
        try{
            var sales = salesClient
                    .findSalesByProductId(product.getId())
                    .orElseThrow(() ->new ValidationException("The sales was not found by this product."));
            return ProductSalesResponse.of(product,sales.getSalesIds());
        } catch (Exception e) {
            throw new ValidationException("There was an error trying to get the product sales.");
        }
    }

    public SuccessResponse checkProductsStock(ProductCheckStockRequest request){
        if(isEmpty(request) || isEmpty(request.getProducts())){
            throw new ValidationException("The request data and products must be informed.");
        }
        request
                .getProducts()
                .forEach(this::validateStock);
        return SuccessResponse.create("The stock is ok!");
    }


    //VALIDATION
    private void validateInformedCategoryAndSupplierId(ProductRequest request) {
        if (isEmpty(request.getCategoryId())) {
            throw new ValidationException("Category ID was not informed.");
        }
        if (isEmpty(request.getSupplierId())) {
            throw new ValidationException("Supplier ID was not informed.");
        }
    }
    private void validateStock(ProductQuantityDTO productQuantity){
        if(isEmpty(productQuantity.getProductId())||isEmpty(productQuantity.getQuantity())){
            throw new ValidationException("Product ID and quantity must be informed.");
        }
        var product = findById(productQuantity.getProductId());
        if(productQuantity.getQuantity() > product.getQuantityAvailable()){
            throw new ValidationException(String.format("Product %s is out of stock.", product.getId()));
        }
    }
    private void validateInformedProductId(Integer id) {
        if (isEmpty(id)) {
            throw new ValidationException("The product ID must be informed.");
        }
    }

    private void validateStockUpdateData(ProductStockDTO product) {
        if (isEmpty(product) || isEmpty(product.getSalesId())) {
            throw new ValidationException("Product data and sales ID must be informed.");
        }
        if (isEmpty(product.getProducts())) {
            throw new ValidationException("The sale products must be informed.");
        }
        product
                .getProducts()
                .forEach(salesProduct -> {
                    if (isEmpty(salesProduct.getQuantity()) || isEmpty(salesProduct.getProductId())) {
                        throw new ValidationException("Product  must be informed.");
                    }
                });
    }
}
