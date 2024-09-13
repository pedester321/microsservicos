package com.microsservicos.productapi.modules.supplier.service;

import com.microsservicos.productapi.config.exception.SuccessResponse;
import com.microsservicos.productapi.config.exception.ValidationException;
import com.microsservicos.productapi.modules.product.service.ProductService;
import com.microsservicos.productapi.modules.supplier.dto.SupplierRequest;
import com.microsservicos.productapi.modules.supplier.dto.SupplierResponse;
import com.microsservicos.productapi.modules.supplier.model.Supplier;
import com.microsservicos.productapi.modules.supplier.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductService productService;

    public List<SupplierResponse> findAll() {
        return supplierRepository
                .findAll()
                .stream()
//              .map(category -> CategoryResponse.of(category)) SE DER RUIM
                .map(SupplierResponse::of)
                .collect(Collectors.toList());
    }

    public List<SupplierResponse> findByName(String name) {
        if (isEmpty(name)) {
            throw new ValidationException("The supplier name must be informed.");
        }
        return supplierRepository
                .findByNameIgnoreCaseContaining(name)
                .stream()
//              .map(category -> CategoryResponse.of(category)) SE DER RUIM
                .map(SupplierResponse::of)
                .collect(Collectors.toList());
    }

    public SupplierResponse findByIdResponse(Integer id) {
        return SupplierResponse.of(findById(id));
    }

    public Supplier findById(Integer id) {
        validateInformedSupplierId(id);
        return supplierRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException("There's no supplier for the given ID."));
    }

    public SupplierResponse save(SupplierRequest request) {
        validateInformedSupplierName(request);
        var supplier = supplierRepository.save(Supplier.of(request));
        return SupplierResponse.of(supplier);
    }

    public SupplierResponse update(SupplierRequest request,
                                   Integer id) {
        validateInformedSupplierName(request);
        validateInformedSupplierId(id);
        var supplier = Supplier.of(request);
        supplier.setId(id);
        supplierRepository.save(supplier);
        return SupplierResponse.of(supplier);
    }

    public SuccessResponse delete(Integer id) {
        validateInformedSupplierId(id);
        if (productService.existsBySupplierId(id)) {
            throw new ValidationException("Cannot delete. Supplier is used by a product.");
        }
        supplierRepository.deleteById(id);
        return SuccessResponse.create("The supplier has been deleted.");
    }

    private void validateInformedSupplierName(SupplierRequest request) {
        if (isEmpty(request.getName())) {
            throw new ValidationException("Supplier name was not informed.");
        }
    }

    private void validateInformedSupplierId(Integer supplierId) {
        if (isEmpty(supplierId)) {
            throw new ValidationException("Supplier ID was not informed.");
        }
    }
}
