FROM mysql:latest

ENV MYSQL_ROOT_PASSWORD=""
ENV MYSQL_DATABASE=soccer_test

COPY soccer_db.sql /docker-entrypoint-initdb.d/

EXPOSE 3306
