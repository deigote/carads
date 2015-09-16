DROP DATABASE IF EXISTS carads;
CREATE DATABASE carads DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON carads.* TO 'carads'@'%' IDENTIFIED BY 'c4r4d5';
GRANT ALL PRIVILEGES ON carads.* TO 'carads'@'localhost' IDENTIFIED BY 'c4r4d5';
use carads;
source schema.sql