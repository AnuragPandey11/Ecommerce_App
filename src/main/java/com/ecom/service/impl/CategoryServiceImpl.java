package com.ecom.service.impl;



import com.ecom.dto.CategoryRequest;
import com.ecom.dto.CategoryResponse;
import com.ecom.entity.Category;
import com.ecom.exception.BadRequestException;
import com.ecom.repository.CategoryRepository;
import com.ecom.service.CategoryService;
import com.ecom.security.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
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

        return CategoryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .slug(saved.getSlug())
                .description(saved.getDescription())
                .parentId(saved.getParent() != null ? saved.getParent().getId() : null)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category cat : categories) {
            responses.add(
                    CategoryResponse.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .slug(cat.getSlug())
                            .description(cat.getDescription())
                            .parentId(cat.getParent() != null ? cat.getParent().getId() : null)
                            .createdAt(cat.getCreatedAt())
                            .build()
            );
        }
        return responses;
    }
}
