package com.microsservicos.productapi.modules.category.service;

import com.microsservicos.productapi.config.exception.SuccessResponse;
import com.microsservicos.productapi.config.exception.ValidationException;
import com.microsservicos.productapi.modules.category.dto.CategoryRequest;
import com.microsservicos.productapi.modules.category.dto.CategoryResponse;
import com.microsservicos.productapi.modules.category.model.Category;
import com.microsservicos.productapi.modules.category.repository.CategoryRepository;
import com.microsservicos.productapi.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;


@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductService productService;

    public List<CategoryResponse> findAll() {
        return categoryRepository
                .findAll()
                .stream()
//                .map(category -> CategoryResponse.of(category)) SE DER RUIM
                .map(CategoryResponse::of)
                .collect(Collectors.toList());
    }

    public CategoryResponse findByIdResponse(Integer id) {
        return CategoryResponse.of(findById(id));
    }

    public List<CategoryResponse> findByDescription(String description) {
        if (isEmpty(description)) {
            throw new ValidationException("The category description must be informed.");
        }
        return categoryRepository
                .findByDescriptionIgnoreCaseContaining(description)
                .stream()
//               .map(category -> CategoryResponse.of(category)) SE DER RUIM
                .map(CategoryResponse::of)
                .collect(Collectors.toList());
    }

    public Category findById(Integer id) {
        validateInformedCategoryId(id);
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException("There's no category for the given ID."));
    }

    public CategoryResponse save(CategoryRequest request) {
        validateInformedCategoryDescription(request);
        var category = categoryRepository.save(Category.of(request));
        return CategoryResponse.of(category);
    }

    public CategoryResponse update(CategoryRequest request,
                                   Integer id) {
        validateInformedCategoryDescription(request);
        validateInformedCategoryId(id);
        var category = Category.of(request);
        category.setId(id);
        categoryRepository.save(category);
        return CategoryResponse.of(category);
    }

    public SuccessResponse delete(Integer id) {
        validateInformedCategoryId(id);
        if (productService.existsByCategoryId(id)) {
            throw new ValidationException("Cannot delete. Category is used by a product.");
        }
        categoryRepository.deleteById(id);
        return SuccessResponse.create("The category has been deleted.");
    }

    private void validateInformedCategoryDescription(CategoryRequest request) {
        if (isEmpty(request.getDescription())) {
            throw new ValidationException("Category description was not informed.");
        }
    }

    private void validateInformedCategoryId(Integer categoryId) {
        if (isEmpty(categoryId)) {
            throw new ValidationException("A category ID must be informed.");
        }
    }
}
