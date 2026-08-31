#!/bin/sh

# Strict error handling: immediate script exit if any command fails
set -e

# Environment variable and file path configurations
CONFIG_DIR="/usr/share/nginx/html/assets"
CONFIG_FILE="${CONFIG_DIR}/urlconfig.json"

# Create target directory if it does not already exist
mkdir -p "${CONFIG_DIR}"

# Dynamic configuration injection during container startup
# Check for the presence of the API_URL environment variable
if [ -n "$API_URL" ]; then
    echo "[INFO] Injecting remote URL ($API_URL) into backend configuration..."
    
    # Dynamically generate urlconfig.json using environment variables
    cat <<EOF > "$CONFIG_FILE"
{
  "apiUrl": "${API_URL}"
}
EOF

    # Apply global read permissions to the generated file
    chmod 644 "$CONFIG_FILE"

else
    echo "[WARN] No API_URL variable provided; retaining default configuration"
fi

# Process control handoff
# Replaces the main shell with the command passed as arguments (Dockerfile CMD, e.g., Nginx)
exec "$@"