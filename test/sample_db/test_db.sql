USE `tcc_test`;

CREATE TABLE
    `tcc_test`.`test` (
        `id` INT NOT NULL AUTO_INCREMENT,
        `title` VARCHAR(120) NOT NULL,
        `description` TEXT NULL,
        `checked` BOOLEAN NOT NULL,
        PRIMARY KEY (`id`)
    );