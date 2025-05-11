FROM mysql:latest

ENV MYSQL_ROOT_PASSWORD=""
ENV MYSQL_DATABASE=spotify_db

COPY soccer_db.sql /docker-entrypoint-initdb.d/

EXPOSE 3306
