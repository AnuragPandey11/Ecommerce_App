# Deployment Environment Check

All hardcoded values and defaults have been addressed. The application is now configured to use environment variables for all deployment-specific settings.

Please refer to the following checklist to configure the environment variables in your deployment environment (e.g., Railway, .env file).

---

## 📋 Environment Variables Checklist

Configure these in Railway (Variables tab) or your `.env` file.

### Application URLs
| Variable Key | Description | Example Value |
| :--- | :--- | :--- |
| `APP_FRONTEND_BASE_URL` | URL of the frontend app (used in emails) | `https://my-shop.vercel.app` |
| `APP_BACKEND_BASE_URL` | URL of this backend app (used in emails) | `https://api-production.up.railway.app` |
| `APP_CORS_ALLOWED_ORIGINS` | Allowed frontend origins (comma-separated) | `https://my-shop.vercel.app` |

### Security
| Variable Key | Description | Example Value |
| :--- | :--- | :--- |
| `APP_JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token lifetime in ms | `86400000` (24h) |
| `JWT_SECRET` | Secret key for signing JWTs | `long_random_string` |
| `APP_OAUTH2_REDIRECT_URI` | OAuth2 Redirect URI | `http://localhost:3000/oauth2/redirect` |

### File Uploads
| Variable Key | Description | Example Value |
| :--- | :--- | :--- |
| `APP_UPLOAD_DIRECTORY` | Directory for file uploads | `uploads` |

### Mail Configuration
| Variable Key | Description |
| :--- | :--- |
| `SPRING_MAIL_HOST` | SMTP Server (e.g., `smtp.gmail.com`) |
| `SPRING_MAIL_PORT` | SMTP Port (e.g., `587`) |
| `SPRING_MAIL_USERNAME` | SMTP Username/Email |
| `SPRING_MAIL_PASSWORD` | SMTP Password/App Password |

### Cloudflare R2 Storage (If using R2)
| Variable Key | Description |
| :--- | :--- |
| `APP_R2_ACCESS_KEY` | R2 Access Key ID |
| `APP_R2_SECRET_KEY` | R2 Secret Access Key |
| `APP_R2_ACCOUNT_ID` | Cloudflare Account ID |
| `APP_R2_BUCKET_NAME` | Bucket Name |
| `APP_R2_PUBLIC_URL` | Public Domain for images |

### Google OAuth2
| Variable Key | Description |
| :--- | :--- |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Client Secret |

### Database (Railway MySQL)
*   Railway automatically provides `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`.
*   Ensure `SPRING_DATASOURCE_URL` is constructed using these if not auto-configured.
