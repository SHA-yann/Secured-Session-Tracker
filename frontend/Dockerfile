# STEP 1: Build the Angular application
FROM node:22-alpine AS build

# Set the working directory inside the build container
WORKDIR /app

# Isolate and copy dependency files to optimize layer caching
COPY frontend/package*.json ./

# Install project dependencies
RUN npm install

# Copy the entire frontend source code
COPY frontend/ .

# Build the Angular application for production
RUN npm run build


# STEP 2: Production Server (Nginx)
FROM nginx:1.27-alpine

# Create a dedicated system group and non-root user for enhanced security
RUN addgroup -S angulargroup && adduser -S angularguser -G angulargroup

# Copy compiled build artifacts from the build stage to the Nginx root directory
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html


# Copy custom entrypoint script for startup initialization management
COPY frontend/docker-entryPoint.sh /docker-entryPoint.sh

# Grant execution permissions to the entrypoint script
RUN chmod +x /docker-entryPoint.sh

# Override default Nginx configuration (supports Angular SPA client-side routing)
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Expose the standard container HTTP port
EXPOSE 80

# Execute the initialization script upon container startup
ENTRYPOINT ["/docker-entryPoint.sh"]

# Default command passed to the ENTRYPOINT to start Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]