package com.ecom.service;

import com.ecom.dto.DiscountRequest;
import com.ecom.dto.DiscountResponse;

import java.util.List;

public interface DiscountService {
    DiscountResponse createDiscount(DiscountRequest discountRequest);
    List<DiscountResponse> getAllDiscounts();
    DiscountResponse getDiscountByCode(String code);
    void deleteDiscount(Long id);
}
