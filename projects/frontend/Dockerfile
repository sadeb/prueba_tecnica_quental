# Build: Angular CLI sobre Node 24 (alpine, multi-arch).
FROM node:24.21.0-alpine AS build
WORKDIR /app
ENV NG_CLI_ANALYTICS=false CI=true

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --no-audit --no-fund

COPY . .
# `ng build` usa la configuracion production por defecto.
RUN npm run build

# Runtime: nginx alpine-slim. nginx.conf completo: 1 worker, SPA fallback, proxy /api.
FROM nginx:1.30.4-alpine-slim
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /app/dist/frontend/browser/ /usr/share/nginx/html/
EXPOSE 80
