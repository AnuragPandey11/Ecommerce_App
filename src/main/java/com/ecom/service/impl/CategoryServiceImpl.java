package com.ecom.service.impl;



import com.ecom.dto.CategoryRequest;
import com.ecom.dto.CategoryResponse;
import com.ecom.entity.Category;
import com.ecom.exception.BadRequestException;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.repository.CategoryRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.service.CategoryService;
import com.ecom.util.SlugUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SlugUtils slugUtils;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category name already exists");
        }

        String slug = slugUtils.toSlug(request.getName());
        long suffix = 1;
        while (categoryRepository.existsBySlug(slug)) {
            slug = slugUtils.generateUniqueSlug(request.getName(), suffix++);
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BadRequestException("Invalid parent category"));
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .parent(parent)
                .build();

        Category saved = categoryRepository.save(category);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category cat : categories) {
            responses.add(toResponse(cat));
        }
        return responses;
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getName().equals(request.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new BadRequestException("Category name already exists");
            }
            String slug = slugUtils.toSlug(request.getName());
            long suffix = 1;
            while (categoryRepository.existsBySlug(slug)) {
                slug = slugUtils.generateUniqueSlug(request.getName(), suffix++);
            }
            category.setName(request.getName());
            category.setSlug(slug);
        }

        category.setDescription(request.getDescription());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BadRequestException("Invalid parent category"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category saved = categoryRepository.save(category);

        return toResponse(saved);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (productRepository.existsByCategoriesId(id)) {
            throw new BadRequestException("Cannot delete category because it has associated products. Remove or reassign those products first.");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category cat) {
        List<String> images = productRepository.findFirstProductImageByCategoryId(
                cat.getId(), PageRequest.of(0, 1));
        String imageUrl = images.isEmpty() ? null : images.get(0);

        return CategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .description(cat.getDescription())
                .parentId(cat.getParent() != null ? cat.getParent().getId() : null)
                .imageUrl(imageUrl)
                .createdAt(cat.getCreatedAt())
                .build();
    }
}
