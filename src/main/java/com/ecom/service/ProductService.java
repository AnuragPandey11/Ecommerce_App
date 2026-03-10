package com.ecom.service;


import com.ecom.dto.PagedResponse;
import com.ecom.dto.ProductFiltersResponse;
import com.ecom.dto.ProductRequest;
import com.ecom.dto.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request, List<MultipartFile> images);

    ProductResponse getProductBySlug(String slug);

    PagedResponse<ProductResponse> getProducts(Pageable pageable, String search, List<Long> categoryIds,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock, Double minRating);

    ProductFiltersResponse getProductFilters();

    ProductResponse updateInventory(Long productId, Integer inventory);

    ProductResponse addProductImages(Long productId, List<MultipartFile> files);

    void deleteProduct(Long id);
}

