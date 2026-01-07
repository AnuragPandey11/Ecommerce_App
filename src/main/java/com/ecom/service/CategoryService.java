package com.ecom.service;


import com.ecom.dto.CategoryRequest;
import com.ecom.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getAllCategories();
}
