# E-commerce API Documentation

This document provides a comprehensive overview of the E-commerce application's API endpoints.

## Table of Contents
- [Authentication](#auth-controller)
- [Products](#product-controller)
- [Categories](#category-controller)
- [Cart](#cart-controller)
- [Orders](#order-controller)
- [Wishlist](#wishlist-controller)
- [Users](#user-controller)
- [Reviews](#review-controller)
- [Discounts](#discount-controller)
- [Images](#image-controller)

---

## Auth Controller (`/api/auth`)

### Register User
- **Endpoint:** `POST /api/auth/register`
- **Description:** Registers a new user.
- **Request Body:**
  ```json
  {
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "1234567890",
    "password": "password123"
  }
  ```
- **Success Response (201 Created):**
  ```json
  {
    "success": true,
    "message": "User registered successfully. Please verify your email.",
    "payload": {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "phone": "1234567890",
      "isVerified": false,
      "roles": [ "ROLE_USER" ],
      "createdAt": "2023-10-27T10:00:00Z",
      "updatedAt": "2023-10-27T10:00:00Z"
    }
  }
  ```

### Verify Email
- **Endpoint:** `GET /api/auth/verify-email`
- **Description:** Verifies a user's email address using a token.
- **Query Parameters:**
  - `token` (string, required): The verification token sent to the user's email.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Email verified successfully",
    "payload": "verified"
  }
  ```

### Login User
- **Endpoint:** `POST /api/auth/login`
- **Description:** Authenticates a user and returns JWT tokens.
- **Request Body:**
  ```json
  {
    "email": "john.doe@example.com",
    "password": "password123"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Logged in successfully",
    "payload": {
      "accessToken": "ey...",
      "refreshToken": "...",
      "tokenType": "Bearer",
      "expiresIn": 3600000,
      "user": {
        "id": 1,
        "name": "John Doe",
        "email": "john.doe@example.com",
        "phone": "1234567890",
        "isVerified": true,
        "roles": [ "ROLE_USER" ],
        "createdAt": "2023-10-27T10:00:00Z",
        "updatedAt": "2023-10-27T10:05:00Z"
      }
    }
  }
  ```

### Refresh Token
- **Endpoint:** `POST /api/auth/refresh`
- **Description:** Refreshes an access token using a refresh token.
- **Request Body:**
  ```json
  {
    "refreshToken": "..."
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Token refreshed successfully",
    "payload": {
      "accessToken": "ey...",
      "refreshToken": "...",
      "tokenType": "Bearer",
      "expiresIn": 3600000
    }
  }
  ```

### Logout User
- **Endpoint:** `POST /api/auth/logout`
- **Description:** Logs out a user by revoking their refresh token.
- **Authentication:** Bearer Token required.
- **Request Body:**
  ```json
  {
    "refreshToken": "..."
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Logged out successfully",
    "payload": "logged_out"
  }
  ```

### Forgot Password
- **Endpoint:** `POST /api/auth/forgot-password`
- **Description:** Sends a password reset email to the user.
- **Request Body:**
  ```json
  {
    "email": "john.doe@example.com"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Password reset email sent successfully",
    "payload": null
  }
  ```

### Reset Password
- **Endpoint:** `POST /api/auth/reset-password`
- **Description:** Resets the user's password using a reset token.
- **Request Body:**
  ```json
  {
    "token": "...",
    "newPassword": "newPassword456"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Password reset successfully",
    "payload": null
  }
  ```

---

## Product Controller (`/api/products`)

### Create Product
- **Endpoint:** `POST /api/products`
- **Description:** Creates a new product. Requires ADMIN or STAFF role.
- **Authentication:** Bearer Token required.
- **Request Body:** `multipart/form-data`
  - `product` (JSON part):
    ```json
    {
      "name": "Laptop Pro",
      "slug": "laptop-pro",
      "priceBefore": 1500.00,
      "priceAfter": 1200.00,
      "inventory": 100,
      "descriptionHtml": "<p>This is a powerful laptop.</p>",
      "isActive": true,
      "categoryIds": [1, 2]
    }
    ```
  - `images` (File part, optional): List of product images.
- **Success Response (201 Created):**
  ```json
  {
    "success": true,
    "message": "Product created successfully",
    "payload": {
      "id": 1,
      "name": "Laptop Pro",
      "slug": "laptop-pro",
      "priceBefore": 1500.00,
      "priceAfter": 1200.00,
      "inventory": 100,
      "averageRating": 0.0,
      "reviewCount": 0,
      "descriptionHtml": "<p>This is a powerful laptop.</p>",
      "isActive": true,
      "categories": [
          { "id": 1, "name": "Electronics", "slug": "electronics" },
          { "id": 2, "name": "Computers", "slug": "computers" }
      ],
      "images": [
          { "id": 1, "imageUrl": "/path/to/image.jpg" }
      ],
      "createdAt": "2023-10-27T10:00:00Z",
      "updatedAt": "2023-10-27T10:00:00Z"
    }
  }
  ```

### Get Products
- **Endpoint:** `GET /api/products`
- **Description:** Gets a paginated list of products with filtering and sorting.
- **Query Parameters:**
  - `page` (int, optional, default: 0): Page number.
  - `size` (int, optional, default: 12): Page size.
  - `sort` (string, optional, default: `createdAt,DESC`): Sort order (e.g., `priceAfter,ASC`).
  - `search` (string, optional): Search term.
  - `categoryId` (long, optional): Category ID to filter by.
  - `minPrice` (BigDecimal, optional): Minimum price.
  - `maxPrice` (BigDecimal, optional): Maximum price.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Products retrieved successfully",
    "payload": {
      "content": [
        {
          "id": 1,
          "name": "Laptop Pro",
          "slug": "laptop-pro",
          "priceAfter": 1200.00,
          "images": [ { "id": 1, "imageUrl": "/path/to/image.jpg" } ],
          "averageRating": 4.5,
          "reviewCount": 10
        }
      ],
      "page": 0,
      "size": 12,
      "totalElements": 1,
      "totalPages": 1,
      "last": true
    }
  }
  ```

### Get Product by Slug
- **Endpoint:** `GET /api/products/{slug}`
- **Description:** Gets a single product by its slug.
- **Path Variables:**
  - `slug` (string, required): The slug of the product.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Product retrieved successfully",
    "payload": {
      "id": 1,
      "name": "Laptop Pro",
      "slug": "laptop-pro",
      "priceBefore": 1500.00,
      "priceAfter": 1200.00,
      "inventory": 100,
      "averageRating": 4.5,
      "reviewCount": 10,
      "descriptionHtml": "<p>This is a powerful laptop.</p>",
      "isActive": true,
      "categories": [
          { "id": 1, "name": "Electronics", "slug": "electronics" },
          { "id": 2, "name": "Computers", "slug": "computers" }
      ],
      "images": [
          { "id": 1, "imageUrl": "/path/to/image.jpg" }
      ],
      "createdAt": "2023-10-27T10:00:00Z",
      "updatedAt": "2023-10-27T10:00:00Z"
    }
  }
  ```

### Update Inventory
- **Endpoint:** `PATCH /api/products/{id}/inventory`
- **Description:** Updates the inventory of a product. Requires ADMIN or STAFF role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `id` (long, required): The ID of the product.
- **Query Parameters:**
  - `inventory` (int, required): The new inventory count.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Inventory updated",
    "payload": {
      "id": 1,
      "name": "Laptop Pro",
      "inventory": 200
    }
  }
  ```

---

## Category Controller (`/api/categories`)

### Create Category
- **Endpoint:** `POST /api/categories`
- **Description:** Creates a new category. Requires ADMIN or STAFF role.
- **Authentication:** Bearer Token required.
- **Request Body:**
  ```json
  {
    "name": "Electronics",
    "slug": "electronics",
    "parentId": null
  }
  ```
- **Success Response (201 Created):**
  ```json
  {
    "success": true,
    "message": "Category created",
    "payload": {
      "id": 1,
      "name": "Electronics",
      "slug": "electronics",
      "parentId": null,
      "subCategories": []
    }
  }
  ```

### Get Categories
- **Endpoint:** `GET /api/categories`
- **Description:** Gets a list of all categories.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Categories retrieved successfully",
    "payload": [
      {
        "id": 1,
        "name": "Electronics",
        "slug": "electronics",
        "parentId": null,
        "subCategories": [
          {
            "id": 2,
            "name": "Computers",
            "slug": "computers",
            "parentId": 1,
            "subCategories": []
          }
        ]
      }
    ]
  }
  ```

---

## Cart Controller (`/api/cart`)

### Add to Cart
- **Endpoint:** `POST /api/cart/add`
- **Description:** Adds an item to the user's cart.
- **Authentication:** Bearer Token required.
- **Request Body:**
  ```json
  {
    "productId": 1,
    "quantity": 1
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Item added to cart",
    "payload": {
      "id": 1,
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Laptop Pro",
          "quantity": 1,
          "price": 1200.00,
          "imageUrl": "/path/to/image.jpg"
        }
      ],
      "subtotal": 1200.00,
      "discount": null,
      "total": 1200.00
    }
  }
  ```

### Get Cart
- **Endpoint:** `GET /api/cart`
- **Description:** Gets the user's cart.
- **Authentication:** Bearer Token required.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Cart retrieved successfully",
    "payload": {
      "id": 1,
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Laptop Pro",
          "quantity": 1,
          "price": 1200.00,
          "imageUrl": "/path/to/image.jpg"
        }
      ],
      "subtotal": 1200.00,
      "discount": null,
      "total": 1200.00
    }
  }
  ```

### Apply Discount
- **Endpoint:** `POST /api/cart/apply-discount`
- **Description:** Applies a discount to the user's cart.
- **Authentication:** Bearer Token required.
- **Request Body:**
  ```json
  {
    "discountCode": "SUMMER10"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Discount applied successfully",
    "payload": {
      "id": 1,
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Laptop Pro",
          "quantity": 1,
          "price": 1200.00,
          "imageUrl": "/path/to/image.jpg"
        }
      ],
      "subtotal": 1200.00,
      "discount": {
        "id": 1,
        "code": "SUMMER10",
        "discountType": "PERCENTAGE",
        "discountValue": 10
      },
      "total": 1080.00
    }
  }
  ```

---

## Order Controller (`/api/orders`)

### Create Order
- **Endpoint:** `POST /api/orders`
- **Description:** Creates a new order from the user's cart. Requires USER role.
- **Authentication:** Bearer Token required.
- **Request Body:**
  ```json
  {
    "cartItems": [
      {
        "productId": 1,
        "quantity": 1
      }
    ],
    "shippingAddress": "123 Main St, Anytown, USA",
    "discountId": null
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "id": 1,
    "orderItems": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Laptop Pro",
        "quantity": 1,
        "price": 1200.00
      }
    ],
    "subtotal": 1200.00,
    "discount": null,
    "totalPrice": 1200.00,
    "status": "PENDING",
    "shippingAddress": "123 Main St, Anytown, USA",
    "createdAt": "2023-10-27T10:00:00Z"
  }
  ```

### Get Orders for User
- **Endpoint:** `GET /api/orders`
- **Description:** Gets a list of all orders for the current user. Requires USER role.
- **Authentication:** Bearer Token required.
- **Success Response (200 OK):**
  ```json
  [
    {
      "id": 1,
      "orderItems": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Laptop Pro",
          "quantity": 1,
          "price": 1200.00
        }
      ],
      "subtotal": 1200.00,
      "discount": null,
      "totalPrice": 1200.00,
      "status": "PENDING",
      "shippingAddress": "123 Main St, Anytown, USA",
      "createdAt": "2023-10-27T10:00:00Z"
    }
  ]
  ```

### Get Order
- **Endpoint:** `GET /api/orders/{orderId}`
- **Description:** Gets a single order by its ID. Requires USER role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `orderId` (long, required): The ID of the order.
- **Success Response (200 OK):**
  ```json
  {
    "id": 1,
    "orderItems": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Laptop Pro",
        "quantity": 1,
        "price": 1200.00
      }
    ],
    "subtotal": 1200.00,
    "discount": null,
    "totalPrice": 1200.00,
    "status": "PENDING",
    "shippingAddress": "123 Main St, Anytown, USA",
    "createdAt": "2023-10-27T10:00:00Z"
  }
  ```

---

## Wishlist Controller (`/api/wishlist`)

### Get Wishlist
- **Endpoint:** `GET /api/wishlist`
- **Description:** Gets the user's wishlist. Requires USER role.
- **Authentication:** Bearer Token required.
- **Success Response (200 OK):**
  ```json
  {
    "id": 1,
    "products": [
      {
        "id": 1,
        "name": "Laptop Pro",
        "slug": "laptop-pro",
        "priceAfter": 1200.00,
        "imageUrl": "/path/to/image.jpg"
      }
    ]
  }
  ```

### Add Product to Wishlist
- **Endpoint:** `POST /api/wishlist`
- **Description:** Adds a product to the user's wishlist. Requires USER role.
- **Authentication:** Bearer Token required.
- **Request Body:**
  ```json
  {
    "productId": 1
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "id": 1,
    "products": [
      {
        "id": 1,
        "name": "Laptop Pro",
        "slug": "laptop-pro",
        "priceAfter": 1200.00,
        "imageUrl": "/path/to/image.jpg"
      }
    ]
  }
  ```

### Remove Product from Wishlist
- **Endpoint:** `DELETE /api/wishlist/{productId}`
- **Description:** Removes a product from the user's wishlist. Requires USER role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `productId` (long, required): The ID of the product to remove.
- **Success Response (204 No Content):** (No response body)

---

## User Controller (`/api/users`)

### Get Current User
- **Endpoint:** `GET /api/users/me`
- **Description:** Gets the profile of the currently authenticated user.
- **Authentication:** Bearer Token required.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "User profile retrieved",
    "payload": {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "phone": "1234567890",
      "isVerified": true,
      "roles": [ "ROLE_USER" ],
      "createdAt": "2023-10-27T10:00:00Z",
      "updatedAt": "2023-10-27T10:05:00Z"
    }
  }
  ```

### Update Profile
- **Endpoint:** `PUT /api/users/me`
- **Description:** Updates the profile of the currently authenticated user.
- **Authentication:** Bearer Token required.
- **Request Body:**
  ```json
  {
    "name": "Johnathan Doe",
    "phone": "0987654321"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Profile updated successfully",
    "payload": {
      "id": 1,
      "name": "Johnathan Doe",
      "email": "john.doe@example.com",
      "phone": "0987654321",
      "isVerified": true,
      "roles": [ "ROLE_USER" ],
      "createdAt": "2023-10-27T10:00:00Z",
      "updatedAt": "2023-10-27T11:00:00Z"
    }
  }
  ```

### Change Password
- **Endpoint:** `PUT /api/users/me/password`
- **Description:** Changes the password of the currently authenticated user.
- **Authentication:** Bearer Token required.
- **Request Body:**
  ```json
  {
    "oldPassword": "password123",
    "newPassword": "newPassword456"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Password changed successfully",
    "payload": null
  }
  ```

### Get All Users (Admin)
- **Endpoint:** `GET /api/users`
- **Description:** Gets a paginated list of all users. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Query Parameters:**
  - `page` (int, optional, default: 0): Page number.
  - `size` (int, optional, default: 10): Page size.
  - `sortBy` (string, optional, default: `id`): Field to sort by.
  - `sortDir` (string, optional, default: `asc`): Sort direction (`asc` or `desc`).
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Users retrieved successfully",
    "payload": {
      "content": [
        {
          "id": 1,
          "name": "John Doe",
          "email": "john.doe@example.com",
          "roles": ["ROLE_USER"]
        }
      ],
      "pageable": { },
      "totalElements": 1,
      "totalPages": 1,
      "last": true
    }
  }
  ```

### Get User by ID (Admin)
- **Endpoint:** `GET /api/users/{id}`
- **Description:** Gets a single user by their ID. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `id` (long, required): The ID of the user.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "User retrieved successfully",
    "payload": {
      "id": 2,
      "name": "Jane Smith",
      "email": "jane.smith@example.com",
      "roles": ["ROLE_USER"]
    }
  }
  ```

### Update User Roles (Admin)
- **Endpoint:** `PUT /api/users/{id}/roles`
- **Description:** Updates the roles of a user. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `id` (long, required): The ID of the user.
- **Request Body:**
  ```json
  {
    "roles": ["ROLE_USER", "ROLE_STAFF"]
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "User roles updated successfully",
    "payload": {
      "id": 2,
      "name": "Jane Smith",
      "email": "jane.smith@example.com",
      "roles": ["ROLE_USER", "ROLE_STAFF"]
    }
  }
  ```

### Delete User (Admin)
- **Endpoint:** `DELETE /api/users/{id}`
- **Description:** Deletes a user. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `id` (long, required): The ID of the user.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "User deleted successfully",
    "payload": null
  }
  ```

---

## Review Controller (`/api`)

### Add Review
- **Endpoint:** `POST /api/products/{productId}/reviews`
- **Description:** Adds a review to a product. Requires USER role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `productId` (long, required): The ID of the product to review.
- **Request Body:**
  ```json
  {
    "rating": 5,
    "comment": "This is a great product!"
  }
  ```
- **Success Response (201 Created):**
  ```json
  {
    "id": 1,
    "rating": 5,
    "comment": "This is a great product!",
    "user": {
      "id": 1,
      "name": "John Doe"
    },
    "createdAt": "2023-10-27T10:00:00Z"
  }
  ```

### Get Reviews for Product
- **Endpoint:** `GET /api/products/{productId}/reviews`
- **Description:** Gets all reviews for a specific product.
- **Path Variables:**
  - `productId` (long, required): The ID of the product.
- **Success Response (200 OK):**
  ```json
  [
    {
      "id": 1,
      "rating": 5,
      "comment": "This is a great product!",
      "user": {
        "id": 1,
        "name": "John Doe"
      },
      "createdAt": "2023-10-27T10:00:00Z"
    }
  ]
  ```

### Delete Review
- **Endpoint:** `DELETE /api/reviews/{reviewId}`
- **Description:** Deletes a review. Requires USER role. The user can only delete their own reviews.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `reviewId` (long, required): The ID of the review to delete.
- **Success Response (204 No Content):** (No response body)

---

## Discount Controller (`/api/discounts`)

### Create Discount
- **Endpoint:** `POST /api/discounts`
- **Description:** Creates a new discount. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Request Body:**
  ```json
  {
    "code": "SUMMER10",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "expiryDate": "2024-12-31T23:59:59",
    "isActive": true,
    "maxUsage": 100
  }
  ```
- **Success Response (201 Created):**
  ```json
  {
    "id": 1,
    "code": "SUMMER10",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "expiryDate": "2024-12-31T23:59:59",
    "isActive": true,
    "maxUsage": 100,
    "usageCount": 0,
    "createdAt": "2023-10-27T10:00:00Z"
  }
  ```

### Get All Discounts
- **Endpoint:** `GET /api/discounts`
- **Description:** Gets a list of all discounts. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Success Response (200 OK):**
  ```json
  [
    {
      "id": 1,
      "code": "SUMMER10",
      "discountType": "PERCENTAGE",
      "discountValue": 10,
      "expiryDate": "2024-12-31T23:59:59",
      "isActive": true,
      "maxUsage": 100,
      "usageCount": 10,
      "createdAt": "2023-10-27T10:00:00Z"
    }
  ]
  ```

### Get Discount by Code
- **Endpoint:** `GET /api/discounts/{code}`
- **Description:** Gets a single discount by its code. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `code` (string, required): The code of the discount.
- **Success Response (200 OK):**
  ```json
  {
    "id": 1,
    "code": "SUMMER10",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "expiryDate": "2024-12-31T23:59:59",
    "isActive": true,
    "maxUsage": 100,
    "usageCount": 10,
    "createdAt": "2023-10-27T10:00:00Z"
  }
  ```

### Delete Discount
- **Endpoint:** `DELETE /api/discounts/{id}`
- **Description:** Deletes a discount. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `id` (long, required): The ID of the discount to delete.
- **Success Response (204 No Content):** (No response body)

---

## Image Controller (`/api/images`)

### Upload Multiple Images
- **Endpoint:** `POST /api/images/upload`
- **Description:** Uploads multiple images. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Request Body:** `multipart/form-data`
  - `images` (File array, required): The image files to upload.
  - `altTexts` (string array, optional): Corresponding alt texts for the images.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Images uploaded successfully",
    "payload": [
      {
        "id": 1,
        "imageUrl": "/api/images/files/image1.jpg",
        "altText": "Image 1"
      },
      {
        "id": 2,
        "imageUrl": "/api/images/files/image2.jpg",
        "altText": "Image 2"
      }
    ]
  }
  ```

### Upload Single Image
- **Endpoint:** `POST /api/images/upload/single`
- **Description:** Uploads a single image. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Request Body:** `multipart/form-data`
  - `image` (File, required): The image file to upload.
  - `altText` (string, optional): Alt text for the image.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Image uploaded successfully",
    "payload": {
      "id": 1,
      "imageUrl": "/api/images/files/image1.jpg",
      "altText": "Image 1"
    }
  }
  ```

### Serve Image File
- **Endpoint:** `GET /api/images/files/{filename:.+}`
- **Description:** Serves an image file.
- **Path Variables:**
  - `filename` (string, required): The name of the image file.
- **Success Response (200 OK):** The image file.

### Delete Image
- **Endpoint:** `DELETE /api/images/{imageId}`
- **Description:** Deletes an image. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `imageId` (long, required): The ID of the image to delete.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Image deleted successfully",
    "payload": null
  }
  ```

### Set Primary Image
- **Endpoint:** `PUT /api/images/products/{productId}/primary-image/{imageId}`
- **Description:** Sets the primary image for a product. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `productId` (long, required): The ID of the product.
  - `imageId` (long, required): The ID of the image to set as primary.
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Primary image set successfully",
    "payload": {
      "id": 1,
      "imageUrl": "/api/images/files/image1.jpg",
      "altText": "Image 1"
    }
  }
  ```

### Reorder Images
- **Endpoint:** `PUT /api/images/products/{productId}/images/reorder`
- **Description:** Reorders the images for a product. Requires ADMIN role.
- **Authentication:** Bearer Token required.
- **Path Variables:**
  - `productId` (long, required): The ID of the product.
- **Request Body:**
  ```json
  [ 2, 1 ]
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Images reordered successfully",
    "payload": [
      {
        "id": 2,
        "imageUrl": "/api/images/files/image2.jpg",
        "altText": "Image 2"
      },
      {
        "id": 1,
        "imageUrl": "/api/images/files/image1.jpg",
        "altText": "Image 1"
      }
    ]
  }
  ```
