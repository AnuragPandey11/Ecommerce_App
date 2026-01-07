package com.ecom.service;


import com.ecom.dto.ProductRequest;
import com.ecom.dto.PagedResponse;
import com.ecom.dto.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request, List<MultipartFile> images);

    ProductResponse getProductBySlug(String slug);

    PagedResponse<ProductResponse> getProducts(Pageable pageable, Long categoryId, String search);

    ProductResponse updateInventory(Long productId, Integer inventory);

    void deleteProduct(Long id);
}

