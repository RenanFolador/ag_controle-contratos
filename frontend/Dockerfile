FROM node:22-alpine AS build
WORKDIR /workspace
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG KEYCLOAK_URL=http://localhost:8081
ARG KEYCLOAK_REALM=contract-manager
ARG KEYCLOAK_CLIENT_ID=contract-manager-web
RUN npm run build -- --configuration docker \
    --define "DOCKER_KEYCLOAK_URL='${KEYCLOAK_URL}'" \
    --define "DOCKER_KEYCLOAK_REALM='${KEYCLOAK_REALM}'" \
    --define "DOCKER_KEYCLOAK_CLIENT_ID='${KEYCLOAK_CLIENT_ID}'"

FROM nginx:1.28.3-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/contract-manager-web/browser /usr/share/nginx/html
EXPOSE 80
