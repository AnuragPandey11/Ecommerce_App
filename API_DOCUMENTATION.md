# E-commerce Application API Documentation

This document provides a comprehensive overview of the RESTful APIs exposed by the E-commerce Application. It details the available endpoints, their functionalities, expected request payloads, and response structures.

All API responses are wrapped in a standardized `ApiResponse` object.

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
```

---

## Database Relationships Overview

This section describes key database relationships relevant to the API for better understanding of data structures and operations.

### User and Authentication Related Relationships

*   **User to Role:** Many-to-Many relationship. A `User` can have multiple `Roles` (e.g., ADMIN, USER, STAFF), and a `Role` can be assigned to many `Users`. This is managed via a `user_roles` join table.
*   **User to RefreshToken:** One-to-Many relationship. A `User` can have multiple `RefreshTokens` (for different sessions/devices).
*   **User to AuditLog:** One-to-Many relationship. `AuditLog` records track actions performed by a specific `User`.
*   **LoginAttempt:** This entity tracks failed login attempts based on IP address and email. It does not have a direct foreign key relationship to the `User` entity but is logically associated through the `email` field.

### Product and Category Related Relationships

*   **Product to Category:** Many-to-Many relationship. A `Product` can belong to multiple `Categories`, and a `Category` can contain multiple `Products`. This is managed via a `product_categories` join table.
*   **Category to Category (Self-referencing):** Many-to-One relationship. A `Category` can have an optional `parent` category, allowing for a hierarchical (nested) category structure.
*   **Product to ProductImage:** One-to-Many relationship. A `Product` can have multiple `ProductImage` records, each representing an image associated with the product.
*   **Image (Polymorphic Association):** The `Image` entity itself uses `entityType` (an enum: PRODUCT, CATEGORY, USER, BRAND) and `entityId` to establish a polymorphic association with various other entities. This allows a single `Image` table to store images for different types of entities without needing separate join tables for each.

### Cart and Order Related Relationships

*   **User to Cart:** One-to-One relationship. Each `User` has one `Cart`.
*   **Cart to CartItem:** One-to-Many relationship. A `Cart` can contain multiple `CartItems`.
*   **CartItem to Product:** Many-to-One relationship. Each `CartItem` refers to a single `Product`.
*   **Cart to Discount:** Many-to-One relationship. A `Cart` can have one `Discount` applied.
*   **User to Order:** One-to-Many relationship. A `User` can place multiple `Orders`.
*   **Order to OrderItem:** One-to-Many relationship. An `Order` can contain multiple `OrderItems`.
*   **OrderItem to Product:** Many-to-One relationship. Each `OrderItem` refers to a single `Product`.
*   **Order to Discount:** Many-to-One relationship. An `Order` can have one `Discount` applied.

### Review and Wishlist Related Relationships

*   **Product to Review:** One-to-Many relationship. A `Product` can have many `Reviews`.
*   **User to Review:** One-to-Many relationship. A `User` can write many `Reviews`.
*   **User to Wishlist:** One-to-One relationship. Each `User` has one `Wishlist`.
*   **Wishlist to Product:** Many-to-Many relationship. A `Wishlist` can contain multiple `Products`, and a `Product` can be in multiple `Wishlists`. This is managed via a `wishlist_products` join table.

---

## Authentication API (`/api/auth`)

Handles user registration, login, token refresh, and password management.

### 1. Register a New User

- **Endpoint:** `POST /api/auth/register`
- **Description:** Registers a new user account. Upon successful registration, a verification email containing a unique token is sent to the provided email address. The user will not be able to log in until their email is verified.
- **Request Body:** `RegisterRequest`
  ```java
  public class RegisterRequest {
      @NotBlank(message = "Name is required")
      @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
      private String name;

      @NotBlank(message = "Email is required")
      @Email(message = "Email should be valid")
      private String email;

      @NotBlank(message = "Password is required")
      @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
      @Pattern(
          regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
          message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
      )
      private String password;
  }
  ```
- **Response Body:** `ApiResponse<UserResponse>`
  ```java
  public class UserResponse {
      private Long id;
      private String name;
      private String email;
      private String profilePictureUrl;
      private List<String> roles;
  }
  ```

### 2. Verify Email

- **Endpoint:** `GET /api/auth/verifyEmail`
- **Description:** Verifies a user's email address using a token sent during registration. A successful verification activates the user's account, allowing them to log in.
- **Request Parameters:** `token` (String) - The unique verification token received via email.
- **Response Body:** `ApiResponse<String>` (Success message, e.g., "Email verified successfully")

### 3. User Login

- **Endpoint:** `POST /api/auth/login`
- **Description:** Authenticates a user and returns JWT and refresh tokens. **Note: Only email-verified users can successfully log in.**
- **Request Body:** `LoginRequest`
  ```java
  public class LoginRequest {
      @NotBlank(message = "Email is required")
      @Email(message = "Email should be valid")
      private String email;

      @NotBlank(message = "Password is required")
      private String password;
  }
  ```
- **Response Body:** `ApiResponse<JwtAuthResponse>`
  ```java
  public class JwtAuthResponse {
      private String accessToken;
      private String refreshToken;
      private String tokenType = "Bearer";
  }
  ```

### 4. Refresh Token

- **Endpoint:** `POST /api/auth/refreshToken`
- **Description:** Refreshes an expired access token using a refresh token.
- **Request Body:** `RefreshTokenRequest`
  ```java
  public class RefreshTokenRequest {
      @NotBlank(message = "Refresh token is required")
      private String refreshToken;
  }
  ```
- **Response Body:** `ApiResponse<RefreshTokenResponse>`
  ```java
  public class RefreshTokenResponse {
      private String accessToken;
      private String refreshToken;
      private String tokenType = "Bearer";
  }
  ```

### 5. Logout

- **Endpoint:** `POST /api/auth/logout`
- **Description:** Invalidates the user's refresh token, effectively logging them out.
- **Request Headers:** `Authorization: Bearer <accessToken>`
- **Response Body:** `ApiResponse<String>` (Success message)

### 6. Forgot Password

- **Endpoint:** `POST /api/auth/forgotPassword`
- **Description:** Sends a password reset link to the user's email.
- **Request Body:** `ForgotPasswordRequest`
  ```java
  public class ForgotPasswordRequest {
      @NotBlank(message = "Email is required")
      @Email(message = "Email should be valid")
      private String email;
  }
  ```
- **Response Body:** `ApiResponse<String>` (Success message)

### 7. Reset Password

- **Endpoint:** `POST /api/auth/resetPassword`
- **Description:** Resets the user's password using a valid reset token.
- **Request Body:** `ResetPasswordRequest`
  ```java
  public class ResetPasswordRequest {
      @NotBlank(message = "Token is required")
      private String token;

      @NotBlank(message = "New password is required")
      @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
      @Pattern(
          regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
          message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
      )
      private String newPassword;
  }
  ```
- **Response Body:** `ApiResponse<String>` (Success message)

---

## Cart API (`/api/cart`)

Manages shopping cart operations for authenticated users.

### 1. Add Item to Cart

- **Endpoint:** `POST /api/cart/add`
- **Description:** Adds a product to the authenticated user's shopping cart.
- **Authentication:** Required (User role)
- **Request Body:** `CartItemRequest`
  ```java
  public class CartItemRequest {
      @NotNull(message = "Product ID is required")
      private Long productId;

      @NotNull(message = "Quantity is required")
      @Min(value = 1, message = "Quantity must be at least 1")
      private Integer quantity;
  }
  ```
- **Response Body:** `ApiResponse<CartResponse>`
  ```java
  public class CartResponse {
      private Long id;
      private List<CartItemResponse> items;
      private BigDecimal subtotal;
      private DiscountResponse discount;
      private BigDecimal totalAmount;
      private Integer totalItems;
  }

  public class CartItemResponse {
      private Long id;
      private Long productId;
      private String productName;
      private String productSlug;
      private BigDecimal price;
      private Integer quantity;
      private BigDecimal subtotal;
      private String primaryImageUrl;
  }

  public class DiscountResponse {
      private Long id;
      private String code;
      private DiscountType discountType; // Enum: PERCENTAGE, FIXED_AMOUNT
      private BigDecimal discountValue;
      private LocalDateTime expiryDate;
      private Boolean isActive;
      private Integer maxUsage;
      private Integer usageCount;
      private LocalDateTime createdAt;
  }
  ```

### 2. Get User's Cart

- **Endpoint:** `GET /api/cart`
- **Description:** Retrieves the authenticated user's shopping cart details.
- **Authentication:** Required (User role)
- **Response Body:** `ApiResponse<CartResponse>` (Same as above)

### 3. Apply Discount to Cart

- **Endpoint:** `POST /api/cart/apply-discount`
- **Description:** Applies a discount code to the authenticated user's cart.
- **Authentication:** Required (User role)
- **Request Body:** `ApplyDiscountRequest`
  ```java
  public class ApplyDiscountRequest {
      @NotBlank
      private String discountCode;
  }
  ```
- **Response Body:** `ApiResponse<CartResponse>` (Same as above)

---

## Category API (`/api/categories`)

Manages product categories.

### 1. Create Category

- **Endpoint:** `POST /api/categories`
- **Description:** Creates a new product category.
- **Authentication:** Required (ADMIN or STAFF role)
- **Request Body:** `CategoryRequest`
  ```java
  public class CategoryRequest {
      @NotBlank(message = "Category name is required")
      @Size(max = 100, message = "Category name cannot exceed 100 characters")
      private String name;
      private String description;
      private Long parentId; // Optional, for nested categories
  }
  ```
- **Response Body:** `ApiResponse<CategoryResponse>`
  ```java
  public class CategoryResponse {
      private Long id;
      private String name;
      private String slug; // URL-friendly version of the name
      private String description;
      private Long parentId;
      private LocalDateTime createdAt;
  }
  ```

### 2. Get All Categories

- **Endpoint:** `GET /api/categories`
- **Description:** Retrieves a list of all product categories.
- **Response Body:** `ApiResponse<List<CategoryResponse>>`

---

## Discount API (`/api/discounts`)

Manages discount codes.

### 1. Create Discount

- **Endpoint:** `POST /api/discounts`
- **Description:** Creates a new discount code.
- **Authentication:** Required (ADMIN role)
- **Request Body:** `DiscountRequest`
  ```java
  public class DiscountRequest {
      @NotBlank
      private String code;
      @NotNull
      private DiscountType discountType; // Enum: PERCENTAGE, FIXED_AMOUNT
      @NotNull
      @DecimalMin("0.01")
      private BigDecimal discountValue;
      @Future // Date must be in the future
      private LocalDateTime expiryDate;
      @NotNull
      private Boolean isActive;
      private Integer maxUsage; // Optional, null for unlimited
  }
  ```
- **Response Body:** `DiscountResponse` (Same as in Cart API)

### 2. Get All Discounts

- **Endpoint:** `GET /api/discounts`
- **Description:** Retrieves a list of all discount codes.
- **Authentication:** Required (ADMIN role)
- **Response Body:** `List<DiscountResponse>`

### 3. Get Discount by Code

- **Endpoint:** `GET /api/discounts/{code}`
- **Description:** Retrieves a specific discount code by its code.
- **Authentication:** Required (ADMIN role)
- **Path Parameters:** `code` (String) - The discount code
- **Response Body:** `DiscountResponse`

### 4. Delete Discount

- **Endpoint:** `DELETE /api/discounts/{id}`
- **Description:** Deletes a discount code by its ID.
- **Authentication:** Required (ADMIN role)
- **Path Parameters:** `id` (Long) - The ID of the discount to delete
- **Response Body:** `ResponseEntity<Void>` (No content on success)

---

## Image API (`/api/images`)

Handles image uploads, serving, deletion, and management for products.

**Physical Storage Note:** Images are typically stored in a designated file system directory (e.g., `uploads/images/`) or an external object storage service (like AWS S3). The `imageUrl` in `ImageResponse` will be the accessible URL to retrieve the image. The database stores metadata about the images, not the images themselves.

### 1. Upload Multiple Images

- **Endpoint:** `POST /api/images/upload`
- **Description:** Uploads multiple images.
- **Authentication:** Required (ADMIN role)
- **Content Type:** `multipart/form-data`
- **Request Parts:**
    - `images` (MultipartFile[]): Array of image files.
    - `altTexts` (List<String>, optional): List of alternative texts for the images, in the same order as `images`.
- **Response Body:** `ApiResponse<List<ImageResponse>>`
  ```java
  public class ImageResponse {
      private Long id;
      private String imageUrl;
      private String altText;
      private ImageEntityType entityType; // Enum: PRODUCT, CATEGORY, USER_PROFILE etc., indicating what type of entity this image is associated with.
      private Long entityId; // ID of the associated entity (e.g., if entityType is PRODUCT, this is the productId).
  }
  ```

### 2. Upload Single Image

- **Endpoint:** `POST /api/images/upload/single`
- **Description:** Uploads a single image.
- **Authentication:** Required (ADMIN role)
- **Content Type:** `multipart/form-data`
- **Request Parts:**
    - `image` (MultipartFile): The image file.
    - `altText` (String, optional): Alternative text for the image.
- **Response Body:** `ApiResponse<ImageResponse>`

### 3. Serve Image File

- **Endpoint:** `GET /api/images/files/{filename}`
- **Description:** Serves an image file.
- **Path Parameters:** `filename` (String) - The name of the image file.
- **Response Body:** `Resource` (Image file directly)

### 4. Delete Image

- **Endpoint:** `DELETE /api/images/{imageId}`
- **Description:** Deletes an image by its ID.
- **Authentication:** Required (ADMIN role)
- **Path Parameters:** `imageId` (Long) - The ID of the image to delete.
- **Response Body:** `ApiResponse<String>` (Success message)

### 5. Set Primary Image for Product

- **Endpoint:** `PUT /api/images/products/{productId}/primary-image/{imageId}`
- **Description:** Sets a specific image as the primary image for a product.
- **Authentication:** Required (ADMIN role)
- **Path Parameters:**
    - `productId` (Long): The ID of the product.
    - `imageId` (Long): The ID of the image to set as primary.
- **Response Body:** `ApiResponse<ImageResponse>`

### 6. Reorder Product Images

- **Endpoint:** `PUT /api/images/products/{productId}/images/reorder`
- **Description:** Reorders images for a specific product.
- **Authentication:** Required (ADMIN role)
- **Path Parameters:** `productId` (Long) - The ID of the product.
- **Request Body:** `List<Long>` - A list of image IDs in the desired order.
- **Response Body:** `ApiResponse<List<ImageResponse>>`

---

## Order API (`/api/orders`)

Handles order creation and retrieval for authenticated users.

### 1. Create Order

- **Endpoint:** `POST /api/orders`
- **Description:** Creates a new order from the user's cart.
- **Authentication:** Required (USER role)
- **Request Body:** `OrderRequest`
  ```java
  public class OrderRequest {
      private List<CartItemRequest> cartItems; // List of items to order, can be from cart or direct
      private String shippingAddress;
      private Long discountId; // Optional, if a discount was applied
  }
  ```
- **Response Body:** `OrderResponse`
  ```java
  public class OrderResponse {
      private Long id;
      private List<OrderItemResponse> orderItems;
      private BigDecimal subtotal;
      private DiscountResponse discount;
      private BigDecimal totalPrice;
      private OrderStatus status; // Enum: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
      private String shippingAddress;
      private LocalDateTime createdAt;
  }

  public class OrderItemResponse {
      private Long id;
      private Long productId;
      private String productName;
      private Integer quantity;
      private BigDecimal price;
  }
  ```

### 2. Get Orders for Current User

- **Endpoint:** `GET /api/orders`
- **Description:** Retrieves all orders for the authenticated user.
- **Authentication:** Required (USER role)
- **Response Body:** `List<OrderResponse>`

### 3. Get Order by ID

- **Endpoint:** `GET /api/orders/{orderId}`
- **Description:** Retrieves a specific order by its ID for the authenticated user.
- **Authentication:** Required (USER role)
- **Path Parameters:** `orderId` (Long) - The ID of the order.
- **Response Body:** `OrderResponse`

---

## Product API (`/api/products`)

Manages product information, including creation, retrieval, and inventory updates.

### 1. Create Product (JSON Request)

- **Endpoint:** `POST /api/products`
- **Description:** Creates a new product.
- **Authentication:** Required (ADMIN or STAFF role)
- **Content Type:** `application/json`
- **Request Body:** `ProductRequest`
  ```java
  public class ProductRequest {
      @NotBlank(message = "Product name is required")
      @Size(max = 255, message = "Product name cannot exceed 255 characters")
      private String name;

      @NotNull(message = "Price before discount is required")
      @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
      private BigDecimal priceBefore;

      @NotNull(message = "Price after discount is required")
      @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
      private BigDecimal priceAfter;

      @NotNull(message = "Inventory is required")
      @Min(value = 0, message = "Inventory cannot be negative")
      private Integer inventory;

      private String descriptionHtml; // HTML content for product description
      private Set<Long> categoryIds; // IDs of categories this product belongs to. These IDs are used to create entries in the `product_categories` join table, establishing the many-to-many relationship.
  }
  ```
- **Response Body:** `ApiResponse<ProductResponse>`
  ```java
  public class ProductResponse {
      private Long id;
      private String name;
      private String slug;
      private BigDecimal priceBefore;
      private BigDecimal priceAfter;
      private Integer inventory;
      private String descriptionHtml;
      private Boolean isActive;
      private Set<CategoryResponse> categories; // Associated categories
      private List<ProductImageResponse> images; // Associated images. Each entry corresponds to an `Image` record in the database.
      private LocalDateTime createdAt;
      private LocalDateTime updatedAt;
  }

  public class ProductImageResponse {
      private Long id;
      private String imageUrl;
      private Integer displayOrder;
      private Boolean isPrimary;
  }
  ```

### 2. Create Product (Multipart Form Data - with Images)

- **Endpoint:** `POST /api/products`
- **Description:** Creates a new product and uploads associated images simultaneously.
- **Authentication:** Required (ADMIN or STAFF role)
- **Content Type:** `multipart/form-data`
- **Request Parts:**
    - `product` (application/json): `ProductRequest` object as JSON.
    - `images` (MultipartFile[], optional): Array of image files to associate with the product.
- **Response Body:** `ApiResponse<ProductResponse>`

### 3. Get Products (Paginated and Filtered)

- **Endpoint:** `GET /api/products`
- **Description:** Retrieves a paginated list of products, with optional search and filtering.
- **Request Parameters:**
    - `page` (int, default: 0): Page number (0-indexed).
    - `size` (int, default: 12): Number of items per page (max 50).
    - `sort` (String, default: "createdAt,DESC"): Sort field and direction (e.g., "name,ASC", "priceAfter,DESC").
    - `search` (String, optional): Keyword to search in product name or description.
    - `categoryId` (Long, optional): Filter by category ID.
    - `minPrice` (BigDecimal, optional): Filter by minimum price.
    - `maxPrice` (BigDecimal, optional): Filter by maximum price.
- **Response Body:** `ApiResponse<PagedResponse<ProductResponse>>`
  ```java
  public class PagedResponse<T> {
      private List<T> content;
      private int pageNumber;
      private int pageSize;
      private long totalElements;
      private int totalPages;
      private boolean last;
  }
  ```

### 4. Get Product by Slug

- **Endpoint:** `GET /api/products/{slug}`
- **Description:** Retrieves a single product by its unique slug.
- **Path Parameters:** `slug` (String) - The URL-friendly identifier of the product.
- **Response Body:** `ApiResponse<ProductResponse>`

### 5. Update Product Inventory

- **Endpoint:** `PATCH /api/products/{id}/inventory`
- **Description:** Updates the inventory quantity for a specific product.
- **Authentication:** Required (ADMIN or STAFF role)
- **Path Parameters:** `id` (Long) - The ID of the product.
- **Request Parameters:** `inventory` (Integer) - The new inventory quantity.
- **Response Body:** `ApiResponse<ProductResponse>`

---

## Review API (`/api/reviews`)

Manages product reviews.

### 1. Add Review to Product

- **Endpoint:** `POST /api/products/{productId}/reviews`
- **Description:** Adds a new review to a specific product by the authenticated user.
- **Authentication:** Required (USER role)
- **Path Parameters:** `productId` (Long) - The ID of the product to review.
- **Request Body:** `ReviewRequest`
  ```java
  public class ReviewRequest {
      @Min(1)
      @Max(5)
      private int rating; // Rating from 1 to 5
      private String comment; // Optional review comment
  }
  ```
- **Response Body:** `ReviewResponse`
  ```java
  public class ReviewResponse {
      private Long id;
      private Long productId;
      private Long userId;
      private String userName;
      private int rating;
      private String comment;
      private LocalDateTime createdAt;
  }
  ```

### 2. Get Reviews for Product

- **Endpoint:** `GET /api/products/{productId}/reviews`
- **Description:** Retrieves all reviews for a specific product.
- **Path Parameters:** `productId` (Long) - The ID of the product.
- **Response Body:** `List<ReviewResponse>`

### 3. Delete Review

- **Endpoint:** `DELETE /api/reviews/{reviewId}`
- **Description:** Deletes a review by its ID. Only the user who created the review or an ADMIN can delete it.
- **Authentication:** Required (USER role)
- **Path Parameters:** `reviewId` (Long) - The ID of the review to delete.
- **Response Body:** `ResponseEntity<Void>` (No content on success)

---

## User API (`/api/users`)

Manages user profiles and administrative user operations.

### 1. Get Current User Profile

- **Endpoint:** `GET /api/users/me`
- **Description:** Retrieves the profile of the currently authenticated user.
- **Authentication:** Required
- **Response Body:** `ApiResponse<UserResponse>` (Same as in Auth API)

### 2. Update Current User Profile

- **Endpoint:** `PUT /api/users/me`
- **Description:** Updates the profile of the currently authenticated user.
- **Authentication:** Required
- **Request Body:** `ProfileUpdateRequest`
  ```java
  public class ProfileUpdateRequest {
      @NotBlank(message = "Name is required")
      @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
      private String name;

      @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
      private String phone; // Optional phone number

      private String profilePictureUrl; // Optional URL to a new profile picture
  }
  ```
- **Response Body:** `ApiResponse<UserResponse>`

### 3. Change Password

- **Endpoint:** `PUT /api/users/me/password`
- **Description:** Allows the authenticated user to change their password.
- **Authentication:** Required
- **Request Body:** `ChangePasswordRequest`
  ```java
  public class ChangePasswordRequest {
      @NotBlank(message = "Current password is required")
      private String currentPassword;

      @NotBlank(message = "New password is required")
      @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
      @Pattern(
          regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
          message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
      )
      private String newPassword;

      @NotBlank(message = "Password confirmation is required")
      private String confirmPassword; // Must match newPassword
  }
  ```
- **Response Body:** `ApiResponse<String>` (Success message)

### 4. Get All Users (Admin Only)

- **Endpoint:** `GET /api/users`
- **Description:** Retrieves a paginated list of all users.
- **Authentication:** Required (ADMIN role)
- **Request Parameters:**
    - `page` (int, default: 0): Page number (0-indexed).
    - `size` (int, default: 10): Number of items per page.
    - `sortBy` (String, default: "id"): Field to sort by.
    - `sortDir` (String, default: "asc"): Sort direction ("asc" or "desc").
- **Response Body:** `ApiResponse<Page<UserResponse>>`

### 5. Get User by ID (Admin Only)

- **Endpoint:** `GET /api/users/{id}`
- **Description:** Retrieves a specific user by their ID.
- **Authentication:** Required (ADMIN role)
- **Path Parameters:** `id` (Long) - The ID of the user.
- **Response Body:** `ApiResponse<UserResponse>`

### 6. Update User Roles (Admin Only)

- **Endpoint:** `PUT /api/users/{id}/roles`
- **Description:** Updates the roles of a specific user.
- **Authentication:** Required (ADMIN role)
- **Path Parameters:** `id` (Long) - The ID of the user.
- **Request Body:** Map where key is "roles" and value is List<String> of role names (e.g., `{"roles": ["USER", "STAFF"]}`)
- **Response Body:** `ApiResponse<UserResponse>`

### 7. Delete User (Admin Only)

- **Endpoint:** `DELETE /api/users/{id}`
- **Description:** Deletes a user by their ID.
- **Authentication:** Required (ADMIN role)
- **Path Parameters:** `id` (Long) - The ID of the user to delete.
- **Response Body:** `ApiResponse<String>` (Success message)

---

## Wishlist API (`/api/wishlist`)

Manages product wishlists for authenticated users.

### 1. Get User's Wishlist

- **Endpoint:** `GET /api/wishlist`
- **Description:** Retrieves the authenticated user's wishlist.
- **Authentication:** Required (USER role)
- **Response Body:** `WishlistResponse`
  ```java
  public class WishlistResponse {
      private Long id;
      private List<WishlistProductResponse> products; // List of products in the wishlist
  }

  public class WishlistProductResponse {
      private Long id;
      private String name;
      private String slug;
      private BigDecimal priceAfter;
      private String imageUrl; // Primary image URL
  }
  ```

### 2. Add Product to Wishlist

- **Endpoint:** `POST /api/wishlist`
- **Description:** Adds a product to the authenticated user's wishlist.
- **Authentication:** Required (USER role)
- **Request Body:** `WishlistRequest`
  ```java
  public class WishlistRequest {
      @NotNull
      private Long productId; // The ID of the product to add
  }
  ```
- **Response Body:** `WishlistResponse`

### 3. Remove Product from Wishlist

- **Endpoint:** `DELETE /api/wishlist/{productId}`
- **Description:** Removes a product from the authenticated user's wishlist.
- **Authentication:** Required (USER role)
- **Path Parameters:** `productId` (Long) - The ID of the product to remove.
- **Response Body:** `ResponseEntity<Void>` (No content on success)
