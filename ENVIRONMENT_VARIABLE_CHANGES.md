# Environment Variable Changes

This file documents the changes made to the codebase to replace hardcoded values with environment variables.

## Changes Made

| File | Hardcoded Value Replaced | Environment Variable |
| --- | --- | --- |
| `src/main/java/com/ecom/config/CorsConfig.java` | `http://localhost:4200` | `APP_CORS_ALLOWED_ORIGINS` |
| `src/main/java/com/ecom/service/impl/MailServiceImpl.java` | `http://localhost:4200` (for `app.frontend.base-url`) | `APP_FRONTEND_BASE_URL` |
| `src/main/java/com/ecom/service/impl/MailServiceImpl.java` | `http://localhost:8080` (for `app.backend.base-url`) | `APP_BACKEND_BASE_URL` |
| `src/main/resources/application.properties` | `uploads` | `APP_UPLOAD_DIRECTORY` |
| `src/main/resources/application.properties` | `yourSecretKeyMustBeAtLeast256BitsLongForHS256AlgorithmToWorkProperly` | `JWT_SECRET` |
| `src/main/resources/application.properties` | `http://localhost:3000/oauth2/redirect` | `APP_OAUTH2_REDIRECT_URI` |
| `src/main/resources/application.properties` | Hardcoded R2 access key | `APP_R2_ACCESS_KEY` |
| `src/main/resources/application.properties` | Hardcoded R2 secret key | `APP_R2_SECRET_KEY` |
| `src/main/resources/application.properties` | Hardcoded R2 bucket name | `APP_R2_BUCKET_NAME` |
| `src/main/resources/application.properties` | Hardcoded R2 public url | `APP_R2_PUBLIC_URL` |
| `src/main/java/com/ecom/config/R2Config.java` | Hardcoded R2 endpoint URL | `APP_R2_ENDPOINT` |
| `src/main/resources/application.properties` | Hardcoded R2 endpoint | `APP_R2_ENDPOINT` & `APP_R2_ACCOUNT_ID` |
| `src/main/resources/application.properties` | `your-client-id` | `GOOGLE_CLIENT_ID` |
| `src/main/resources/application.properties` | `your-client-secret` | `GOOGLE_CLIENT_SECRET` |
