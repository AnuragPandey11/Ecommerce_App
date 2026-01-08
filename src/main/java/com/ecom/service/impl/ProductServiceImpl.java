package com.ecom.service.impl;


import com.ecom.dto.ProductRequest;
import com.ecom.dto.*;
import com.ecom.entity.*;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.repository.*;
import com.ecom.service.ProductService;
import com.ecom.security.HtmlSanitizerUtils;
import com.ecom.security.SlugUtils;
import com.ecom.security.FileUploadUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SlugUtils slugUtils;
    private final HtmlSanitizerUtils htmlSanitizerUtils;
    private final FileUploadUtils fileUploadUtils;

    @Override
    public ProductResponse createProduct(ProductRequest request, List<MultipartFile> images) {
        String baseSlug = slugUtils.toSlug(request.getName());
        String slug = baseSlug;
        long suffix = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = slugUtils.generateUniqueSlug(baseSlug, suffix++);
        }

        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            categories = new HashSet<>(
                    categoryRepository.findAllById(request.getCategoryIds())
            );
        }

        String sanitizedHtml = htmlSanitizerUtils.sanitizeQuillHtml(request.getDescriptionHtml());

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .priceBefore(request.getPriceBefore())
                .priceAfter(request.getPriceAfter())
                .inventory(request.getInventory())
                .descriptionHtml(sanitizedHtml)
                .categories(categories)
                .build();

        Product saved = productRepository.save(product);

        if (images != null && !images.isEmpty()) {
            int order = 0;
            for (MultipartFile file : images) {
                String url = fileUploadUtils.storeFile(file);
                ProductImage image = ProductImage.builder()
                        .product(saved)
                        .imageUrl(url)
                        .displayOrder(order++)
                        .isPrimary(order == 1)
                        .build();
                saved.addImage(image);
            }
        }

        return mapToProductResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<ProductResponse> getProducts(Pageable pageable, Long categoryId, String search) {
        Page<Product> page;
        if (categoryId != null) {
            page = productRepository.findByCategoryIdAndActive(categoryId, pageable);
        } else if (search != null && !search.isBlank()) {
            page = productRepository.searchByNameAndActive(search, pageable);
        } else {
            page = productRepository.findAllActive(pageable);
        }

        List<ProductResponse> content = page.getContent()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public ProductResponse updateInventory(Long productId, Integer inventory) {
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product", "id", productId)
                );
        product.setInventory(inventory);
        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product", "id", id)
                );
        productRepository.delete(product);
    }

    private ProductResponse mapToProductResponse(Product product) {
        List<ProductImageResponse> imageResponses = product.getImages().stream()
                .sorted(Comparator.comparing(
                        img -> Optional.ofNullable(img.getDisplayOrder()).orElse(0)
                ))
                .map(img -> ProductImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .displayOrder(img.getDisplayOrder())
                        .isPrimary(img.getIsPrimary())
                        .build()
                ).collect(Collectors.toList());

        Set<CategoryResponse> categoryResponses = product.getCategories().stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .slug(cat.getSlug())
                        .description(cat.getDescription())
                        .parentId(cat.getParent() != null ? cat.getParent().getId() : null)
                        .createdAt(cat.getCreatedAt())
                        .build()
                ).collect(Collectors.toSet());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .priceBefore(product.getPriceBefore())
                .priceAfter(product.getPriceAfter())
                .inventory(product.getInventory())
                .descriptionHtml(product.getDescriptionHtml())
                .isActive(product.getIsActive())
                .categories(categoryResponses)
                .images(imageResponses)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
