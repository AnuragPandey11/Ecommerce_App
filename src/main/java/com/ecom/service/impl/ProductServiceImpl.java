package com.ecom.service.impl;


import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.dto.CategoryResponse;
import com.ecom.dto.PagedResponse;
import com.ecom.dto.ProductImageResponse;
import com.ecom.dto.ProductRequest;
import com.ecom.dto.ProductResponse;
import com.ecom.entity.Category;
import com.ecom.entity.Product;
import com.ecom.entity.ProductImage;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.repository.CategoryRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.util.FileUploadUtil;
import com.ecom.service.ProductService;
import com.ecom.util.HtmlSanitizerUtils;
import com.ecom.util.SlugUtils;
import com.ecom.specification.ProductSpecification;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SlugUtils slugUtils;
    private final HtmlSanitizerUtils htmlSanitizerUtils;
    private final FileUploadUtil fileUploadUtil;
    private final ProductSpecification productSpecification;

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
                // ✅ FIXED: Changed from storeFile to uploadFile
                String url = fileUploadUtil.uploadFile(file, "products");
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
    public PagedResponse<ProductResponse> getProducts(Pageable pageable, String search, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<Product> spec = Specification.where(productSpecification.isActive())
                .and(productSpecification.hasNameOrDescription(search))
                .and(productSpecification.inCategory(categoryId))
                .and(productSpecification.hasPriceBetween(minPrice, maxPrice));

        Page<Product> page = productRepository.findAll(spec, pageable);

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
