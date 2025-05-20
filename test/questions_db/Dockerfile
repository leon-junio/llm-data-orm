FROM mysql:latest

ENV MYSQL_ROOT_PASSWORD=""
ENV MYSQL_DATABASE=questions_db

COPY questions_db.sql /docker-entrypoint-initdb.d/

EXPOSE 3306
