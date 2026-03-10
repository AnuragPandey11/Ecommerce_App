package com.ecom.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.dto.CategoryResponse;
import com.ecom.dto.PagedResponse;
import com.ecom.dto.ProductFiltersResponse;
import com.ecom.dto.ProductImageResponse;
import com.ecom.dto.ProductRequest;
import com.ecom.dto.ProductResponse;
import com.ecom.entity.Category;
import com.ecom.entity.Product;
import com.ecom.entity.ProductImage;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.repository.CategoryRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.service.ProductService;
import com.ecom.service.R2StorageService;
import com.ecom.specification.ProductSpecification;
import com.ecom.util.HtmlSanitizerUtils;
import com.ecom.util.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SlugUtils slugUtils;
    private final HtmlSanitizerUtils htmlSanitizerUtils;
    private final R2StorageService r2StorageService;
    private final ProductSpecification productSpecification;

    // Allowed MIME types for images
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", 
        "image/png", 
        "image/jpg", 
        "image/webp"
    );

    @Override
    public ProductResponse createProduct(ProductRequest request, List<MultipartFile> images) {
        // 1. Business Logic Validation
        validateProductRequest(request);

        // 2. Image File Validation
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                validateImageFile(file);
            }
        }

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
            if (categories.size() != request.getCategoryIds().size()) {
                throw new ResourceNotFoundException("One or more Category IDs not found");
            }
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
                .isActive(true)
                .build();

        Product saved = productRepository.save(product);

        // Upload images after product is successfully saved
        if (images != null && !images.isEmpty()) {
            int order = 0;
            for (MultipartFile file : images) {
                String url = r2StorageService.uploadFile(file);
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

    @Override
    public ProductResponse addProductImages(Long productId, List<MultipartFile> files) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (files != null && !files.isEmpty()) {
            // Validate all files before uploading any of them
            for (MultipartFile file : files) {
                validateImageFile(file);
            }

            int currentOrder = product.getImages() != null ? product.getImages().size() : 0;

            for (MultipartFile file : files) {
                String url = r2StorageService.uploadFile(file);
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .displayOrder(currentOrder++)
                        .isPrimary(currentOrder == 1)
                        .build();
                product.addImage(image);
            }
            Product saved = productRepository.save(product);
            return mapToProductResponse(saved);
        }
        return mapToProductResponse(product);
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
    public PagedResponse<ProductResponse> getProducts(Pageable pageable, String search, List<Long> categoryIds,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock, Double minRating) {
        Specification<Product> spec = Specification.where(productSpecification.isActive())
                .and(productSpecification.hasNameOrDescription(search))
                .and(productSpecification.inCategories(categoryIds))
                .and(productSpecification.hasPriceBetween(minPrice, maxPrice))
                .and(Boolean.TRUE.equals(inStock) ? productSpecification.isInStock() : Specification.where(null))
                .and(productSpecification.hasMinRating(minRating));

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

    @Transactional(readOnly = true)
    @Override
    public ProductFiltersResponse getProductFilters() {
        List<CategoryResponse> categories = categoryRepository.findAll().stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .slug(cat.getSlug())
                        .description(cat.getDescription())
                        .parentId(cat.getParent() != null ? cat.getParent().getId() : null)
                        .createdAt(cat.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ProductFiltersResponse.builder()
                .categories(categories)
                .minPrice(productRepository.findMinPrice())
                .maxPrice(productRepository.findMaxPrice())
                .build();
    }

    @Override
    public ProductResponse updateInventory(Long productId, Integer inventory) {
        if (inventory == null || inventory < 0) {
            throw new IllegalArgumentException("Inventory count cannot be negative or null");
        }

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

    // =================================================================
    // PRIVATE HELPER METHODS
    // =================================================================

    /**
     * Validates that the uploaded file is indeed an image.
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP images are allowed. Received: " + contentType);
        }
    }

    private void validateProductRequest(ProductRequest request) {
        if (request.getInventory() == null || request.getInventory() < 0) {
            throw new IllegalArgumentException("Inventory cannot be negative.");
        }
        if (request.getPriceAfter() == null || request.getPriceAfter().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Selling price (priceAfter) must be greater than zero.");
        }
        if (request.getPriceBefore() != null) {
            if (request.getPriceBefore().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Original price (priceBefore) must be greater than zero.");
            }
            if (request.getPriceAfter().compareTo(request.getPriceBefore()) > 0) {
                throw new IllegalArgumentException("Selling price cannot be higher than the original price.");
            }
        }
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
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .descriptionHtml(product.getDescriptionHtml())
                .isActive(product.getIsActive())
                .categories(categoryResponses)
                .images(imageResponses)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}