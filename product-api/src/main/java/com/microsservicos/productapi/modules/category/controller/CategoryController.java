package com.microsservicos.productapi.modules.category.controller;

import com.microsservicos.productapi.config.exception.SuccessResponse;
import com.microsservicos.productapi.modules.category.dto.CategoryRequest;
import com.microsservicos.productapi.modules.category.dto.CategoryResponse;
import com.microsservicos.productapi.modules.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public CategoryResponse save(@RequestBody CategoryRequest request) {
        return categoryService.save(request);
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("{id}")
    public CategoryResponse findById(@PathVariable Integer id) {
        return categoryService.findByIdResponse(id);
    }

    @GetMapping("description/{description}")
    public List<CategoryResponse> findByDescription(@PathVariable String description) {
        return categoryService.findByDescription(description);
    }

    @DeleteMapping("{id}")
    public SuccessResponse delete(@PathVariable Integer id) {
        return categoryService.delete(id);
    }

    @PutMapping("{id}")
    public CategoryResponse update(@RequestBody CategoryRequest request,
                                   @PathVariable Integer id) {
        return categoryService.update(request, id);
    }
}
