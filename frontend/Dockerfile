FROM node:lts-alpine3.18 as build

# делаем каталог 'app' текущим рабочим каталогом
WORKDIR /app

# копируем оба 'package.json' и 'package-lock.json' (если есть)
COPY package*.json ./
COPY yarn.lock ./

# устанавливаем зависимости проекта
RUN yarn install --pure-lockfile

# копируем файлы и каталоги проекта в текущий рабочий каталог (т.е. в каталог 'app')
COPY . .

# собираем приложение для production с минификацией
RUN yarn build

FROM node:lts-alpine3.18
COPY --from=build /app/.output /app/.output

EXPOSE 3000
CMD [ "node", "/app/.output/server/index.mjs" ]