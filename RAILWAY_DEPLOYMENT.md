# Deploying Spring Boot on Railway

This guide outlines the steps to deploy the E-commerce Spring Boot application to Railway using Docker.

## 1. Project Configuration

### Dockerfile
Create a file named `Dockerfile` in the root of your project. I have added a permission fix (`chmod +x`) to your provided content to ensure the Maven wrapper runs correctly in the container.

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
# Ensure the maven wrapper is executable
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Run (The Production Image)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Optimized entrypoint for low-memory environments
ENTRYPOINT ["java", "-Xms256m", "-Xmx350m", "-jar", "app.jar"]
```

### Port Configuration
Railway assigns a dynamic port via the `PORT` environment variable. Spring Boot must be configured to listen on this port.

Add the following line to your `src/main/resources/application.properties`:

```properties
server.port=${PORT:8080}
```

## 2. Maven Dependencies (`pom.xml`)

Ensure your `pom.xml` includes the following essential dependencies. Based on your application's features (R2 Storage, Mail, JPA), you will need:

```xml
<dependencies>
    <!-- Web Starter (Required for REST API) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Data JPA (Required for Database) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Database Driver (e.g., MySQL) -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Mail Support (Required for MailService) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- AWS SDK for R2 Storage (Required for R2StorageService) -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>s3</artifactId>
        <version>2.21.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Spring Boot Maven Plugin (Crucial for Docker build) -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

## 3. Deployment Steps

1.  **Push to GitHub**: Ensure your project (including the `Dockerfile`) is pushed to a GitHub repository.
2.  **Create Railway Project**:
    *   Log in to Railway.
    *   Click **"New Project"** > **"Deploy from GitHub repo"**.
    *   Select your repository.
3.  **Configure Environment Variables**:
    *   Go to the **Variables** tab in your Railway project.
    *   Add the variables required by your application (e.g., `SPRING_DATASOURCE_URL`, `APP_R2_ACCESS_KEY`, `SPRING_MAIL_PASSWORD`, etc.).
4.  **Deploy**: Railway will automatically detect the Dockerfile and start the build.
5.  **Generate Domain**: Go to **Settings** > **Networking** and click **"Generate Domain"** to get a public URL for your API.