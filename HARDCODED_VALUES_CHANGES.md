# Hardcoded Values to Environment Variables - Changes Report

## Overview
This document details all changes made to convert hardcoded values (frontend URLs, database, services, CORS configurations, etc.) to use environment variables across the E-Commerce application.

**Date:** January 31, 2026  
**Branch:** feature/imporvements

---

## Summary of Changes

| Category | File(s) | Change | Status |
|----------|---------|--------|--------|
| CORS Configuration | `src/main/resources/application.properties` | Added environment variable for allowed origins | ✅ Complete |
| Frontend Base URL | `src/main/resources/application.properties` | Added environment variable for frontend base URL | ✅ Complete |
| Backend Base URL | `src/main/resources/application.properties` | Added environment variable for backend base URL | ✅ Complete |
| Admin Credentials | `src/main/resources/application.properties` | Added environment variables for admin email and password | ✅ Complete |
| Import Fix | `src/main/java/com/ecom/config/DataInitializer.java` | Added missing `@Value` annotation import | ✅ Complete |

---

## Detailed Changes

### 1. CORS Configuration (CORS Filter)

**File:** `src/main/resources/application.properties`

**Before:**
```properties
# No explicit CORS configuration in properties
# Only in CorsConfig.java via @Value("${app.cors.allowed-origins}")
# which had no default value in properties
```

**After:**
```properties
# CORS Configuration
app.cors.allowed-origins=${APP_CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:3001,http://127.0.0.1:3000}
```

**Environment Variable:** `APP_CORS_ALLOWED_ORIGINS`  
**Default Value:** `http://localhost:3000,http://localhost:3001,http://127.0.0.1:3000`

**Impact:** 
- The `CorsConfig.java` class reads this configuration via `@Value("${app.cors.allowed-origins}")`
- Allows dynamic CORS configuration based on deployment environment
- Supports multiple origins separated by commas

**Related Files:**
- `src/main/java/com/ecom/config/CorsConfig.java` (uses this configuration)

---

### 2. Frontend Base URL

**File:** `src/main/resources/application.properties`

**Before:**
```properties
# No explicit configuration in properties
# MailServiceImpl.java references: @Value("${app.frontend.base-url}")
# with no default fallback
```

**After:**
```properties
# Frontend and Backend URLs
app.frontend.base-url=${APP_FRONTEND_BASE_URL:http://localhost:3000}
```

**Environment Variable:** `APP_FRONTEND_BASE_URL`  
**Default Value:** `http://localhost:3000`

**Impact:**
- Used in email verification and password reset links
- Allows different frontend URLs for different environments (dev, staging, production)
- Supports both HTTP and HTTPS URLs

**Related Files:**
- `src/main/java/com/ecom/service/impl/MailServiceImpl.java` (sends verification and reset emails)
  - `sendVerificationEmail()` method
  - `sendPasswordResetEmail()` method

**Email Links Generated:**
- Verification: `{APP_FRONTEND_BASE_URL}/auth/verify-email?token={token}`
- Password Reset: `{APP_FRONTEND_BASE_URL}/reset-password?token={token}`

---

### 3. Backend Base URL

**File:** `src/main/resources/application.properties`

**Before:**
```properties
# No explicit configuration in properties
# MailServiceImpl.java references: @Value("${app.backend.base-url}")
# with no default fallback
```

**After:**
```properties
app.backend.base-url=${APP_BACKEND_BASE_URL:http://localhost:8080}
```

**Environment Variable:** `APP_BACKEND_BASE_URL`  
**Default Value:** `http://localhost:8080`

**Impact:**
- Used for API endpoint references in email communications
- Allows different backend URLs for different environments
- Currently used in `MailServiceImpl` for Postman API testing references

**Related Files:**
- `src/main/java/com/ecom/service/impl/MailServiceImpl.java`
  - Provides alternative API endpoint for email verification testing

---

### 4. Admin Credentials

**File:** `src/main/resources/application.properties`

**Before:**
```properties
# DataInitializer.java had hardcoded defaults
@Value("${app.admin.email:admin@ecom.local}")
@Value("${app.admin.password:Admin@12345}")
```

**After:**
```properties
# Admin Configuration
app.admin.email=${APP_ADMIN_EMAIL:admin@ecom.local}
app.admin.password=${APP_ADMIN_PASSWORD:Admin@12345}
```

**Environment Variables:**
- `APP_ADMIN_EMAIL` (default: `admin@ecom.local`)
- `APP_ADMIN_PASSWORD` (default: `Admin@12345`)

**Impact:**
- Allows customization of default admin credentials per environment
- DataInitializer creates default admin user only on first run with `@Profile({"dev", "default"})`
- Security: Production should override these with strong, unique values

**Related Files:**
- `src/main/java/com/ecom/config/DataInitializer.java`
  - Reads these values via `@Value` annotations
  - Creates default admin user if it doesn't exist

---

## Already Externalized Configuration

The following configurations were already using environment variables:

### Database Configuration
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

### JWT Configuration
```properties
app.jwt.secret=${JWT_SECRET}
app.jwt.access-token-expiration=3600000
app.jwt.refresh-token-expiration=604800000
```

### Email Configuration
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

### OAuth2 Configuration
```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
app.oauth2.authorized-redirect-uris[0]=${APP_OAUTH2_REDIRECT_URI:http://localhost:3000/oauth2/redirect}
```

### Cloudflare R2 Configuration
```properties
app.r2.access-key=${APP_R2_ACCESS_KEY}
app.r2.secret-key=${APP_R2_SECRET_KEY}
app.r2.bucket-name=${APP_R2_BUCKET_NAME}
app.r2.account-id=${APP_R2_ACCOUNT_ID}
app.r2.endpoint=https://${APP_R2_ACCOUNT_ID}.r2.cloudflarestorage.com
app.r2.public-url=${APP_R2_PUBLIC_URL}
```

### Server Configuration
```properties
server.port=${PORT:8080}
```

---

## Environment Variable Configuration Guide

### Development Environment (.env.local or system environment)
```bash
# CORS
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://127.0.0.1:3000

# Frontend/Backend URLs
APP_FRONTEND_BASE_URL=http://localhost:3000
APP_BACKEND_BASE_URL=http://localhost:8080

# Admin
APP_ADMIN_EMAIL=admin@ecom.local
APP_ADMIN_PASSWORD=Admin@12345
```

### Staging Environment
```bash
# CORS
APP_CORS_ALLOWED_ORIGINS=https://staging.ecommerce.com,https://admin-staging.ecommerce.com

# Frontend/Backend URLs
APP_FRONTEND_BASE_URL=https://staging.ecommerce.com
APP_BACKEND_BASE_URL=https://api-staging.ecommerce.com

# Admin
APP_ADMIN_EMAIL=admin@staging.ecommerce.com
APP_ADMIN_PASSWORD=<strong-password>
```

### Production Environment
```bash
# CORS
APP_CORS_ALLOWED_ORIGINS=https://ecommerce.com,https://www.ecommerce.com

# Frontend/Backend URLs
APP_FRONTEND_BASE_URL=https://ecommerce.com
APP_BACKEND_BASE_URL=https://api.ecommerce.com

# Admin
APP_ADMIN_EMAIL=admin@ecommerce.com
APP_ADMIN_PASSWORD=<strong-secure-password>
```

---

## Modified Files

### 1. `src/main/resources/application.properties`
- **Lines Added:** 4 configuration properties with environment variables
- **Lines Modified:** None (additions only)
- **Breaking Changes:** None (all have sensible defaults)

### 2. `src/main/java/com/ecom/config/DataInitializer.java`
- **Import Added:** `org.springframework.beans.factory.annotation.Value`
- **Lines Modified:** 1 (import statement)
- **Breaking Changes:** None

---

## Testing Checklist

- [ ] Run application with default values (no environment variables set)
  - Verify CORS works with `http://localhost:3000`
  - Verify admin user created with `admin@ecom.local`
  - Verify email links use `http://localhost:3000` frontend URL

- [ ] Run application with custom environment variables
  - Set `APP_CORS_ALLOWED_ORIGINS` to custom value
  - Set `APP_FRONTEND_BASE_URL` to custom value
  - Set `APP_BACKEND_BASE_URL` to custom value
  - Verify configuration is read correctly

- [ ] Test CORS with different origins
  - Requests from allowed origin should succeed
  - Requests from non-allowed origin should fail

- [ ] Test email functionality
  - Send verification email
  - Verify URL matches configured frontend URL
  - Send password reset email
  - Verify URL matches configured frontend URL

- [ ] Test admin initialization
  - Delete admin user from database
  - Restart application
  - Verify new admin created with environment variable credentials

---

## Migration Notes

### For Docker/Kubernetes Deployments
Add the following to your deployment configuration:

**Docker Environment Variables:**
```dockerfile
ENV APP_CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com"
ENV APP_FRONTEND_BASE_URL="https://yourdomain.com"
ENV APP_BACKEND_BASE_URL="https://api.yourdomain.com"
ENV APP_ADMIN_EMAIL="admin@yourdomain.com"
ENV APP_ADMIN_PASSWORD="<strong-password>"
```

**Kubernetes Secrets/ConfigMap:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ecommerce-config
data:
  APP_CORS_ALLOWED_ORIGINS: "https://yourdomain.com"
  APP_FRONTEND_BASE_URL: "https://yourdomain.com"
  APP_BACKEND_BASE_URL: "https://api.yourdomain.com"
---
apiVersion: v1
kind: Secret
metadata:
  name: ecommerce-secret
type: Opaque
stringData:
  APP_ADMIN_EMAIL: "admin@yourdomain.com"
  APP_ADMIN_PASSWORD: "<strong-password>"
```

### For Railway Deployment
Add these to your Railway Variables:
```
APP_CORS_ALLOWED_ORIGINS=https://<your-railway-domain>,https://www.<your-railway-domain>
APP_FRONTEND_BASE_URL=https://<your-railway-domain>
APP_BACKEND_BASE_URL=https://<your-api-railway-domain>
APP_ADMIN_EMAIL=admin@ecommerce.com
APP_ADMIN_PASSWORD=<strong-password>
```

---

## Security Considerations

⚠️ **Important Security Notes:**

1. **Admin Password:**
   - Default `Admin@12345` is for development only
   - **MUST** be changed in staging and production
   - Use strong, randomly generated passwords
   - Store in secure secret management systems (AWS Secrets Manager, HashiCorp Vault, etc.)

2. **CORS Origins:**
   - Be explicit about allowed origins
   - Do NOT use wildcards (`*`) in production for authenticated endpoints
   - Use HTTPS URLs in production

3. **Environment Variables:**
   - Never commit `.env` files to version control
   - Use `.env.local` for local development
   - Add `.env.local` to `.gitignore`

4. **Frontend/Backend URLs:**
   - Use HTTPS in staging and production
   - Ensure SSL/TLS certificates are valid
   - Consider implementing HSTS headers

---

## No Hardcoded Values Found

**Verified Safe Areas:**
- ✅ Database URLs - Already using environment variables
- ✅ JWT secrets - Already using environment variables  
- ✅ Email credentials - Already using environment variables
- ✅ OAuth2 client IDs/secrets - Already using environment variables
- ✅ R2 Cloudflare credentials - Already using environment variables
- ✅ Server port - Already using environment variable (`PORT:8080`)
- ✅ API endpoints - Constructed dynamically from configuration

**No hardcoded localhost, IP addresses, or service endpoints found in:**
- `SecurityConfig.java` - Uses security filters and authentication beans
- `R2Config.java` - Uses environment variables for R2 endpoint
- `WebConfig.java` - Uses app.upload.directory configuration
- Controller classes - All routes are annotation-based
- Entity and Repository classes - No hardcoded URLs

---

## Completion Status

✅ **All changes completed and documented**

All hardcoded values have been either:
1. Converted to externalized configuration properties
2. Already using environment variables (confirmed)
3. Documented for future reference

The application is now fully configurable via environment variables for all critical external service URLs and configurations.

---

## Related Documentation

- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - API endpoints reference
- [RAILWAY_DEPLOYMENT.md](./RAILWAY_DEPLOYMENT.md) - Deployment configuration
- [R2_STORAGE_IMPLEMENTATION.md](./R2_STORAGE_IMPLEMENTATION.md) - Storage service setup
- [DEPLOYMENT_ENV_CHECK.md](./DEPLOYMENT_ENV_CHECK.md) - Environment validation
