# Build: Avec node
FROM node:22-alpine AS build
WORKDIR /app

# Copie des fichiers de configuration pour optimiser le cache des layers Docker
COPY package*.json ./
RUN npm install

# Copie de tout le code source et compilation en mode production
COPY . .
RUN npm run build

# Serve: Avec Nginx
FROM nginx:1.27-alpine

# Copie des fichiers compilés depuis l'étape de build
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copie d'une configuration Nginx personnalisée pour gérer le routage Angular (RouterLink)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]