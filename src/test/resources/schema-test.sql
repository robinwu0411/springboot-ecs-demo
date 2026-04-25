-- H2 test schema — year/month quoted as H2 reserved keywords
DROP TABLE IF EXISTS metric_breakdown_data;
DROP TABLE IF EXISTS metric_monthly_data;
DROP TABLE IF EXISTS metrics;

CREATE TABLE metrics (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    metric_key    VARCHAR(50)  NOT NULL UNIQUE,
    metric_name   VARCHAR(100) NOT NULL,
    metric_type   VARCHAR(20)  NOT NULL,
    sort_order    INT NOT NULL DEFAULT 0,
    bad_direction VARCHAR(10)  NOT NULL DEFAULT 'down'
);

CREATE TABLE metric_monthly_data (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_id  INT NOT NULL,
    `year`     INT NOT NULL,
    `month`    INT NOT NULL,
    actual     DECIMAL(18,2),
    jbp_goal   DECIMAL(18,2),
    category   VARCHAR(100)
);

CREATE TABLE metric_breakdown_data (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_id       INT NOT NULL,
    `year`          INT NOT NULL,
    `month`         INT NOT NULL,
    dimension_type  VARCHAR(20)  NOT NULL,
    dimension_value VARCHAR(200) NOT NULL,
    product_title   VARCHAR(500),
    actual          DECIMAL(18,2),
    jbp_goal        DECIMAL(18,2)
);

-- Seed: 5 metrics
INSERT INTO metrics (id, metric_key, metric_name, metric_type, sort_order, bad_direction) VALUES
(1, 'revenue',            'Revenue',            'financial', 1, 'down'),
(2, 'net_ppm',            'Net PPM',            'financial', 2, 'down'),
(3, 'deal_ops',           'Deal OPS',           'financial', 3, 'down'),
(4, 'new_customer_count', 'New customer count', 'integer',   4, 'down'),
(5, 'net_ordered_gms',    'Net Ordered GMS',    'financial', 5, 'down');

-- Seed: metric_id=1 (revenue) 2024 all 12 months with categories
INSERT INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal, category) VALUES
(1, 2024,  1, 10000000, 11000000, 'Category1'),
(1, 2024,  2, 11000000, 11000000, 'Category2'),
(1, 2024,  3, 12000000, 11500000, 'Category1'),
(1, 2024,  4, 11500000, 11500000, 'Category2'),
(1, 2024,  5, 10800000, 11000000, 'Category1'),
(1, 2024,  6, 11200000, 11000000, 'Category2'),
(1, 2024,  7, 11800000, 11500000, 'Category1'),
(1, 2024,  8, 12000000, 12000000, 'Category2'),
(1, 2024,  9, 11500000, 11500000, 'Category1'),
(1, 2024, 10, 11800000, 11500000, 'Category2'),
(1, 2024, 11, 12500000, 12000000, 'Category1'),
(1, 2024, 12, 13000000, 12500000, 'Category2');

-- Seed: metric_id=1 2025 months 1-4 with categories
INSERT INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal, category) VALUES
(1, 2025, 1, 11000000, 11500000, 'TestCat'),
(1, 2025, 2, 12000000, 12000000, 'Category1'),
(1, 2025, 3, 13000000, 12500000, 'Category2'),
(1, 2025, 4, 12918000, 10240000, 'TestCat');

-- Seed: metric_id=3 (deal_ops) 2024 months 1-4
INSERT INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal, category) VALUES
(3, 2024, 1, 1200000, 1400000, 'Category1'),
(3, 2024, 2, 1350000, 1450000, 'Category2'),
(3, 2024, 3, 1100000, 1350000, 'Category1'),
(3, 2024, 4,  980000, 1240000, 'Category2');

-- Seed: metric_id=3 2025 months 1-4
INSERT INTO metric_monthly_data (metric_id, `year`, `month`, actual, jbp_goal, category) VALUES
(3, 2025, 1, 1300000, 1400000, 'Category1'),
(3, 2025, 2, 1420000, 1450000, 'Category2'),
(3, 2025, 3, 1250000, 1350000, 'Category1'),
(3, 2025, 4, 1180000, 1240000, 'Category2');

-- Seed: breakdown category metric_id=1, 2025-04
INSERT INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2025, 4, 'category', '1080 Car Navigation', NULL, 3229500, 2560000),
(1, 2025, 4, 'category', '1080 Car Recording',  NULL, 3229500, 2560000),
(1, 2025, 4, 'category', '1200 Car Audio',       NULL, 3229500, 2560000),
(1, 2025, 4, 'category', '1200 Dashboard Cam',   NULL, 3229500, 2560000);

-- Seed: breakdown category metric_id=1, 2025-03 (prev month for MoM CtC)
INSERT INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2025, 3, 'category', '1080 Car Navigation', NULL, 3250000, 3125000),
(1, 2025, 3, 'category', '1080 Car Recording',  NULL, 3250000, 3125000),
(1, 2025, 3, 'category', '1200 Car Audio',       NULL, 3250000, 3125000),
(1, 2025, 3, 'category', '1200 Dashboard Cam',   NULL, 3250000, 3125000);

-- Seed: breakdown category metric_id=1, 2024-04 (last year for YoY CtC)
INSERT INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2024, 4, 'category', '1080 Car Navigation', NULL, 2875000, 2875000),
(1, 2024, 4, 'category', '1080 Car Recording',  NULL, 2875000, 2875000),
(1, 2024, 4, 'category', '1200 Car Audio',       NULL, 2875000, 2875000),
(1, 2024, 4, 'category', '1200 Dashboard Cam',   NULL, 2875000, 2875000);

-- Seed: breakdown asin metric_id=1, 2025-04
INSERT INTO metric_breakdown_data (metric_id, `year`, `month`, dimension_type, dimension_value, product_title, actual, jbp_goal) VALUES
(1, 2025, 4, 'asin', 'B08XYZ1234', 'AutoVision 4K Dashcam Pro',   3229500, 2560000),
(1, 2025, 4, 'asin', 'B08XYZ5678', 'DriveGuard Night Vision Cam', 3229500, 2560000),
(1, 2025, 4, 'asin', 'B09ABC1234', 'SafeRide 360 Camera',          3229500, 2560000),
(1, 2025, 4, 'asin', 'B09ABC5678', 'RoadEye Dual Lens Recorder',   3229500, 2560000);
