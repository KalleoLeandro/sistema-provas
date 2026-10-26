FROM node:24-alpine AS compilacao
WORKDIR /aplicacao
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build && npm run build-storybook

FROM nginx:stable-alpine AS execucao
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=compilacao /aplicacao/dist/front-end/browser /usr/share/nginx/html
COPY --from=compilacao /aplicacao/dist/storybook /usr/share/nginx/storybook
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1
