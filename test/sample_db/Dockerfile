FROM mysql:latest

ENV MYSQL_ROOT_PASSWORD=""
ENV MYSQL_DATABASE=tcc_test

COPY test_db.sql /docker-entrypoint-initdb.d/

EXPOSE 3306
