# Dockerfile para frontend Vite (Vue)
FROM node:20.19.0-alpine

WORKDIR /usr/src/app

# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias con npm ci para mayor determinismo
RUN npm ci || npm install

# Copiar todo el código
COPY . .

# Exponer puerto
EXPOSE 5173

# Ejecutar Vite en host 0.0.0.0 para que sea accesible desde fuera del contenedor
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
