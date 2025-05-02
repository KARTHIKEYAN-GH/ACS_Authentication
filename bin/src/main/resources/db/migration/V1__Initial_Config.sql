CREATE TABLE IF NOT EXISTS `configuration` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `api_key` VARCHAR(255) DEFAULT NULL,
  `secret_key` VARCHAR(255) DEFAULT NULL,
  `base_url` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO configuration(base_url) value('http://10.30.11.31:8080/client/api/?');
