package com.ecom.service;

import com.ecom.dto.ReviewRequest;
import com.ecom.dto.ReviewResponse;
import com.ecom.security.UserPrincipal;

import java.util.List;

public interface ReviewService {
    ReviewResponse addReview(Long productId, ReviewRequest reviewRequest, UserPrincipal currentUser);
    List<ReviewResponse> getReviewsForProduct(Long productId);
    void deleteReview(Long reviewId, UserPrincipal currentUser);
}
