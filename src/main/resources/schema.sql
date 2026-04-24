CREATE DATABASE IF NOT EXISTS demo_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE demo_db;

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50) NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (username, email, phone) VALUES
('alice', 'alice@example.com', '13800138001'),
('bob',   'bob@example.com',   '13800138002');
