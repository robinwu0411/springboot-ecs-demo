CREATE DATABASE IF NOT EXISTS test1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE test1;

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

-- ===================== Scorecard tables =====================

CREATE TABLE IF NOT EXISTS metrics (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    metric_key    VARCHAR(50)  NOT NULL UNIQUE,
    metric_name   VARCHAR(100) NOT NULL,
    metric_type   ENUM('financial', 'percentage', 'integer') NOT NULL,
    sort_order    INT NOT NULL DEFAULT 0,
    bad_direction ENUM('up', 'down') NOT NULL DEFAULT 'down'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS metric_monthly_data (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_id  INT NOT NULL,
    `year`     INT NOT NULL,
    `month`    INT NOT NULL,
    actual     DECIMAL(18,2),
    jbp_goal   DECIMAL(18,2),
    UNIQUE KEY uq_metric_ym (metric_id, `year`, `month`),
    INDEX idx_metric_year (metric_id, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS metric_breakdown_data (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_id       INT NOT NULL,
    `year`          INT NOT NULL,
    `month`         INT NOT NULL,
    dimension_type  VARCHAR(20)  NOT NULL,
    dimension_value VARCHAR(200) NOT NULL,
    product_title   VARCHAR(500),
    actual          DECIMAL(18,2),
    jbp_goal        DECIMAL(18,2),
    INDEX idx_breakdown_lookup (metric_id, `year`, `month`, dimension_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed: 5 fixed metrics
INSERT IGNORE INTO metrics (id, metric_key, metric_name, metric_type, sort_order, bad_direction) VALUES
(1, 'revenue',            'Revenue',            'financial', 1, 'down'),
(2, 'net_ppm',            'Net PPM',            'financial', 2, 'down'),
(3, 'deal_ops',           'Deal OPS',           'financial', 3, 'down'),
(4, 'new_customer_count', 'New customer count', 'integer',   4, 'down'),
(5, 'net_ordered_gms',    'Net Ordered GMS',    'financial', 5, 'down');

-- Seed: Revenue (metric_id=1) 2024 all 12 months
INSERT IGNORE INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal) VALUES
(1, 2024,  1, 10000000, 11000000),
(1, 2024,  2, 11000000, 11000000),
(1, 2024,  3, 12000000, 11500000),
(1, 2024,  4, 11500000, 11500000),
(1, 2024,  5, 10800000, 11000000),
(1, 2024,  6, 11200000, 11000000),
(1, 2024,  7, 11800000, 11500000),
(1, 2024,  8, 12000000, 12000000),
(1, 2024,  9, 11500000, 11500000),
(1, 2024, 10, 11800000, 11500000),
(1, 2024, 11, 12500000, 12000000),
(1, 2024, 12, 13000000, 12500000);

-- Seed: Revenue 2025 months 1-4
INSERT IGNORE INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal) VALUES
(1, 2025, 1, 11000000, 11500000),
(1, 2025, 2, 12000000, 12000000),
(1, 2025, 3, 13000000, 12500000),
(1, 2025, 4, 12918000, 10240000);

-- Seed: Net PPM (metric_id=2) 2024-2025
INSERT IGNORE INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal) VALUES
(2, 2024,  1, 850000, 900000), (2, 2024,  2, 870000, 900000),
(2, 2024,  3, 920000, 920000), (2, 2024,  4, 880000, 900000),
(2, 2024,  5, 860000, 880000), (2, 2024,  6, 910000, 890000),
(2, 2024,  7, 940000, 920000), (2, 2024,  8, 960000, 940000),
(2, 2024,  9, 930000, 930000), (2, 2024, 10, 950000, 940000),
(2, 2024, 11, 980000, 960000), (2, 2024, 12, 1020000, 1000000),
(2, 2025, 1, 900000, 950000),  (2, 2025, 2, 950000, 970000),
(2, 2025, 3, 1010000, 990000), (2, 2025, 4, 980000, 960000);

-- Seed: Deal OPS (metric_id=3) 2024-2025
INSERT IGNORE INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal) VALUES
(3, 2024, 1, 1200000, 1400000), (3, 2024, 2, 1350000, 1450000),
(3, 2024, 3, 1100000, 1350000), (3, 2024, 4,  980000, 1240000),
(3, 2024, 5, 1050000, 1200000), (3, 2024, 6, 1150000, 1300000),
(3, 2024, 7, 1280000, 1350000), (3, 2024, 8, 1320000, 1400000),
(3, 2024, 9, 1200000, 1300000), (3, 2024, 10, 1350000, 1380000),
(3, 2024, 11, 1420000, 1450000), (3, 2024, 12, 1500000, 1500000),
(3, 2025, 1, 1300000, 1400000), (3, 2025, 2, 1420000, 1450000),
(3, 2025, 3, 1250000, 1350000), (3, 2025, 4, 1180000, 1240000);

-- Seed: New Customer Count (metric_id=4) 2024-2025
INSERT IGNORE INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal) VALUES
(4, 2024,  1, 3200, 3500), (4, 2024,  2, 3400, 3500),
(4, 2024,  3, 3800, 3800), (4, 2024,  4, 3600, 3700),
(4, 2024,  5, 3300, 3600), (4, 2024,  6, 3500, 3600),
(4, 2024,  7, 3700, 3800), (4, 2024,  8, 4000, 4000),
(4, 2024,  9, 3800, 3900), (4, 2024, 10, 4100, 4000),
(4, 2024, 11, 4500, 4200), (4, 2024, 12, 4800, 4500),
(4, 2025, 1, 3500, 3800),  (4, 2025, 2, 3900, 4000),
(4, 2025, 3, 4200, 4200),  (4, 2025, 4, 4100, 4000);

-- Seed: Net Ordered GMS (metric_id=5) 2024-2025
INSERT IGNORE INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal) VALUES
(5, 2024,  1, 8500000, 9000000),  (5, 2024,  2, 9000000, 9200000),
(5, 2024,  3, 9800000, 9500000),  (5, 2024,  4, 9200000, 9300000),
(5, 2024,  5, 8800000, 9000000),  (5, 2024,  6, 9100000, 9100000),
(5, 2024,  7, 9500000, 9400000),  (5, 2024,  8, 9800000, 9700000),
(5, 2024,  9, 9300000, 9300000),  (5, 2024, 10, 9600000, 9500000),
(5, 2024, 11, 10200000, 10000000), (5, 2024, 12, 10800000, 10500000),
(5, 2025, 1, 9200000, 9500000),   (5, 2025, 2, 9800000, 10000000),
(5, 2025, 3, 10500000, 10200000), (5, 2025, 4, 10200000, 9800000);

-- Seed: Breakdown data for Revenue (metric_id=1) 2025-04 category
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2025, 4, 'category', '1080 Car Navigation', NULL, 3229500, 2560000),
(1, 2025, 4, 'category', '1080 Car Recording',  NULL, 3229500, 2560000),
(1, 2025, 4, 'category', '1200 Car Audio',       NULL, 3229500, 2560000),
(1, 2025, 4, 'category', '1200 Dashboard Cam',   NULL, 3229500, 2560000);

-- Seed: Breakdown data for Revenue 2025-03 category (MoM reference)
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2025, 3, 'category', '1080 Car Navigation', NULL, 3250000, 3125000),
(1, 2025, 3, 'category', '1080 Car Recording',  NULL, 3250000, 3125000),
(1, 2025, 3, 'category', '1200 Car Audio',       NULL, 3250000, 3125000),
(1, 2025, 3, 'category', '1200 Dashboard Cam',   NULL, 3250000, 3125000);

-- Seed: Breakdown data for Revenue 2024-04 category (YoY reference)
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2024, 4, 'category', '1080 Car Navigation', NULL, 2875000, 2875000),
(1, 2024, 4, 'category', '1080 Car Recording',  NULL, 2875000, 2875000),
(1, 2024, 4, 'category', '1200 Car Audio',       NULL, 2875000, 2875000),
(1, 2024, 4, 'category', '1200 Dashboard Cam',   NULL, 2875000, 2875000);

-- Seed: Breakdown data for Revenue 2025-04 asin
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2025, 4, 'asin', 'B08XYZ1234', 'AutoVision 4K Dashcam Pro',   3229500, 2560000),
(1, 2025, 4, 'asin', 'B08XYZ5678', 'DriveGuard Night Vision Cam', 3229500, 2560000),
(1, 2025, 4, 'asin', 'B09ABC1234', 'SafeRide 360 Camera',          3229500, 2560000),
(1, 2025, 4, 'asin', 'B09ABC5678', 'RoadEye Dual Lens Recorder',   3229500, 2560000);

-- Seed: Breakdown data for Revenue 2025-04 subcategory
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2025, 4, 'subcategory', '1080P Dashcams', NULL, 1800000, 1500000),
(1, 2025, 4, 'subcategory', '720P Dashcams',  NULL, 900000,  800000),
(1, 2025, 4, 'subcategory', 'Dual Lens',      NULL, 529500,  260000);

-- Seed: Breakdown data for Revenue 2025-04 brand
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2025, 4, 'brand', 'AutoVision', NULL, 1000000, 800000),
(1, 2025, 4, 'brand', 'DriveGuard', NULL, 900000,  900000),
(1, 2025, 4, 'brand', 'SafeRide',   NULL, 1329500, 860000);

-- Seed: Breakdown data for Net PPM (metric_id=2) 2025-04 category
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(2, 2025, 4, 'category', '1080 Car Navigation', NULL, 245000, 240000),
(2, 2025, 4, 'category', '1080 Car Recording',  NULL, 245000, 240000),
(2, 2025, 4, 'category', '1200 Car Audio',       NULL, 245000, 240000),
(2, 2025, 4, 'category', '1200 Dashboard Cam',   NULL, 245000, 240000);

-- Seed: Breakdown data for Net PPM 2025-04 subcategory
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(2, 2025, 4, 'subcategory', '1080P Dashcams', NULL, 140000, 140000),
(2, 2025, 4, 'subcategory', '720P Dashcams',  NULL, 70000,  70000),
(2, 2025, 4, 'subcategory', 'Dual Lens',      NULL, 35000,  30000);

-- Seed: Breakdown data for Net PPM 2025-04 brand
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(2, 2025, 4, 'brand', 'AutoVision', NULL, 80000,  80000),
(2, 2025, 4, 'brand', 'DriveGuard', NULL, 90000,  90000),
(2, 2025, 4, 'brand', 'SafeRide',   NULL, 75000,  70000);

-- Seed: Breakdown data for Deal OPS (metric_id=3) 2025-04 category
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(3, 2025, 4, 'category', '1080 Car Navigation', NULL, 420000, 450000),
(3, 2025, 4, 'category', '1080 Car Recording',  NULL, 420000, 450000),
(3, 2025, 4, 'category', '1200 Car Audio',       NULL, 420000, 450000),
(3, 2025, 4, 'category', '1200 Dashboard Cam',   NULL, 420000, 450000);

-- Seed: Breakdown data for Deal OPS 2025-04 subcategory
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(3, 2025, 4, 'subcategory', '1080P Dashcams', NULL, 250000, 270000),
(3, 2025, 4, 'subcategory', '720P Dashcams',  NULL, 120000, 120000),
(3, 2025, 4, 'subcategory', 'Dual Lens',      NULL, 50000,  60000);

-- Seed: Breakdown data for Deal OPS 2025-04 brand
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(3, 2025, 4, 'brand', 'AutoVision', NULL, 150000, 160000),
(3, 2025, 4, 'brand', 'DriveGuard', NULL, 170000, 170000),
(3, 2025, 4, 'brand', 'SafeRide',   NULL, 100000, 120000);

-- Seed: Breakdown data for New Customer Count (metric_id=4) 2025-04 category
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(4, 2025, 4, 'category', '1080 Car Navigation', NULL, 1100, 1200),
(4, 2025, 4, 'category', '1080 Car Recording',  NULL, 1100, 1200),
(4, 2025, 4, 'category', '1200 Car Audio',       NULL, 1100, 1200),
(4, 2025, 4, 'category', '1200 Dashboard Cam',   NULL, 1100, 1200);

-- Seed: Breakdown data for New Customer Count 2025-04 subcategory
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(4, 2025, 4, 'subcategory', '1080P Dashcams', NULL, 600,  650),
(4, 2025, 4, 'subcategory', '720P Dashcams',  NULL, 300,  300),
(4, 2025, 4, 'subcategory', 'Dual Lens',      NULL, 200,  250);

-- Seed: Breakdown data for New Customer Count 2025-04 brand
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(4, 2025, 4, 'brand', 'AutoVision', NULL, 300,  350),
(4, 2025, 4, 'brand', 'DriveGuard', NULL, 400,  400),
(4, 2025, 4, 'brand', 'SafeRide',   NULL, 400,  450);

-- Seed: Breakdown data for Net Ordered GMS (metric_id=5) 2025-04 category
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(5, 2025, 4, 'category', '1080 Car Navigation', NULL, 2550000, 2400000),
(5, 2025, 4, 'category', '1080 Car Recording',  NULL, 2550000, 2400000),
(5, 2025, 4, 'category', '1200 Car Audio',       NULL, 2550000, 2400000),
(5, 2025, 4, 'category', '1200 Dashboard Cam',   NULL, 2550000, 2400000);

-- Seed: Breakdown data for Net Ordered GMS 2025-04 subcategory
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(5, 2025, 4, 'subcategory', '1080P Dashcams', NULL, 1500000, 1400000),
(5, 2025, 4, 'subcategory', '720P Dashcams',  NULL, 600000,  600000),
(5, 2025, 4, 'subcategory', 'Dual Lens',      NULL, 450000,  400000);

-- Seed: Breakdown data for Net Ordered GMS 2025-04 brand
INSERT IGNORE INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(5, 2025, 4, 'brand', 'AutoVision', NULL, 800000,  800000),
(5, 2025, 4, 'brand', 'DriveGuard', NULL, 900000,  900000),
(5, 2025, 4, 'brand', 'SafeRide',   NULL, 850000,  700000);
