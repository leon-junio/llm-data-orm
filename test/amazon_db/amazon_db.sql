USE `amazon_db`;

CREATE TABLE amazon_products_table (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(510) NOT NULL,
    value float NOT NULL,
    reviews_total INT,
    reviews_avg float,
    delivery_fee float
);
