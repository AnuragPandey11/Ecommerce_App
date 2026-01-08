package com.ecom.service.impl;

import com.ecom.dto.DiscountRequest;
import com.ecom.dto.DiscountResponse;
import com.ecom.entity.Discount;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.repository.DiscountRepository;
import com.ecom.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    @Override
    public DiscountResponse createDiscount(DiscountRequest discountRequest) {
        Discount discount = Discount.builder()
                .code(discountRequest.getCode())
                .discountType(discountRequest.getDiscountType())
                .discountValue(discountRequest.getDiscountValue())
                .expiryDate(discountRequest.getExpiryDate())
                .isActive(discountRequest.getIsActive())
                .maxUsage(discountRequest.getMaxUsage())
                .build();
        Discount savedDiscount = discountRepository.save(discount);
        return mapDiscountToDiscountResponse(savedDiscount);
    }

    @Override
    public List<DiscountResponse> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(this::mapDiscountToDiscountResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DiscountResponse getDiscountByCode(String code) {
        Discount discount = discountRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with code: " + code));
        return mapDiscountToDiscountResponse(discount);
    }

    @Override
    public void deleteDiscount(Long id) {
        discountRepository.deleteById(id);
    }

    private DiscountResponse mapDiscountToDiscountResponse(Discount discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .expiryDate(discount.getExpiryDate())
                .isActive(discount.getIsActive())
                .maxUsage(discount.getMaxUsage())
                .usageCount(discount.getUsageCount())
                .createdAt(discount.getCreatedAt())
                .build();
    }
}
