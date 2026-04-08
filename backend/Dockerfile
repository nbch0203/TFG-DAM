FROM node:20.19.0

WORKDIR /usr/src/app

# Docker CLI para poder leer métricas desde el contenedor backend
ARG DOCKER_CLI_VERSION=26.1.4
RUN apt-get update \
	&& apt-get install -y curl ca-certificates \
	&& rm -rf /var/lib/apt/lists/* \
	&& curl -fsSL "https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKER_CLI_VERSION}.tgz" -o /tmp/docker.tgz \
	&& tar -xzf /tmp/docker.tgz -C /tmp \
	&& mv /tmp/docker/* /usr/bin/ \
	&& rm -rf /tmp/docker /tmp/docker.tgz

COPY package*.json ./
RUN npm install
COPY . .

EXPOSE 3000
CMD ["node", "server.js"]
