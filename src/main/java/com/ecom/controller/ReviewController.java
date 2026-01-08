package com.ecom.controller;

import com.ecom.dto.ReviewRequest;
import com.ecom.dto.ReviewResponse;
import com.ecom.security.CurrentUser;
import com.ecom.security.UserPrincipal;
import com.ecom.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> addReview(@PathVariable Long productId,
                                                    @Valid @RequestBody ReviewRequest reviewRequest,
                                                    @CurrentUser UserPrincipal currentUser) {
        ReviewResponse reviewResponse = reviewService.addReview(productId, reviewRequest, currentUser);
        return new ResponseEntity<>(reviewResponse, HttpStatus.CREATED);
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviewsForProduct(@PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsForProduct(productId);
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                           @CurrentUser UserPrincipal currentUser) {
        reviewService.deleteReview(reviewId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
