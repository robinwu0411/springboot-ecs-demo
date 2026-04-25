# Backend Design Doc

## 技术栈
- Java 17 + Spring Boot 3.2
- MyBatis 3.0（XML SQL）
- MySQL 8.0
- 统一返回 `Result<T>`，前缀 `/api`

---

## 数据库表结构

### 1. `metrics`（指标定义表）

```sql
CREATE TABLE metrics (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    metric_key  VARCHAR(50)  NOT NULL UNIQUE COMMENT 'revenue / net_ppm / deal_ops / new_customer_count / net_ordered_gms',
    metric_name VARCHAR(100) NOT NULL COMMENT '展示名称',
    metric_type ENUM('financial', 'percentage', 'integer') NOT NULL COMMENT '数值类型',
    sort_order  INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    bad_direction ENUM('up', 'down') NOT NULL DEFAULT 'down' COMMENT '恶化方向，down=下降为红，up=上升为红'
);
```

### 2. `metric_monthly_data`（月度指标数据）

```sql
CREATE TABLE metric_monthly_data (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_id  INT    NOT NULL,
    year       INT    NOT NULL,
    month      INT    NOT NULL COMMENT '1-12',
    actual     DECIMAL(18, 2) COMMENT '实际值',
    jbp_goal   DECIMAL(18, 2) COMMENT 'JBP目标',
    INDEX idx_metric_ym (metric_id, year, month)
);
```

### 3. `metric_breakdown_data`（维度分解数据）

```sql
CREATE TABLE metric_breakdown_data (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_id       INT    NOT NULL,
    year            INT    NOT NULL,
    month           INT    NOT NULL,
    dimension_type  ENUM('category', 'subcategory', 'brand', 'asin') NOT NULL,
    dimension_value VARCHAR(200) NOT NULL COMMENT '维度值（类目名/品牌名/ASIN）',
    product_title   VARCHAR(500) COMMENT '仅 asin 类型使用',
    actual          DECIMAL(18, 2),
    jbp_goal        DECIMAL(18, 2),
    INDEX idx_breakdown (metric_id, year, month, dimension_type)
);
```

### 初始数据（Seed）
- `metrics` 插入5条固定记录
- `metric_monthly_data` 插入2024全年 + 2025年至今月度数据（mock）
- `metric_breakdown_data` 插入 category/subcategory/brand/asin 各维度 mock 数据

---

## API 接口设计

### 接口1：GET `/api/metric/overview`

**描述**：返回5个固定指标的总览数据，供 Overview 页面展示。

**请求参数（Query）**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | int | 否 | 默认当前年 |
| month | int | 否 | 默认最新有数据的月份 |

**请求示例**：
```
GET /api/metric/overview?year=2025&month=4
```

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "period": "April 2025",
    "year": 2025,
    "month": 4,
    "metrics": [
      {
        "metricId": 1,
        "metricKey": "revenue",
        "metricName": "Revenue",
        "metricType": "financial",
        "badDirection": "down",
        "current": {
          "actual": 12918000,
          "jbpGoal": 10240000,
          "vsJbpGoalPct": 26.25,
          "mom": 19871716,
          "momPct": 14.0,
          "yoy": 2000615,
          "yoyPct": 12.0
        },
        "ytd": {
          "actual": 16840000,
          "jbpGoal": 26840000,
          "vsJbpGoalPct": -37.26,
          "mom": 4240000,
          "momPct": 12.0,
          "yoy": 9240000,
          "yoyPct": 12.0
        }
      }
    ]
  }
}
```

**计算逻辑**：
- `vsJbpGoalPct` = (actual - jbpGoal) / jbpGoal × 100
- `mom` = 当期 actual - 上期 actual
- `momPct` = mom / 上期 actual × 100
- `yoy` = 当期 actual - 去年同期 actual
- `yoyPct` = yoy / 去年同期 actual × 100
- YTD = 当年1月 ~ 选定月份的累计值（actual 累加，goal 累加，百分比类指标取加权平均）

---

### 接口2：GET `/api/metric/breakdown`

**描述**：返回指定指标的详情数据，根据 `type` 参数返回趋势数据或维度分解数据，供详情弹窗使用。

**请求参数（Query）**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| metricId | int | 是 | 指标ID |
| type | string | 是 | `trend` / `category` / `subcategory` / `brand` / `asin` |
| viewBy | string | 否 | 仅 type=trend 时有效，`weekly` / `monthly`（默认）/ `quarterly` |
| year | int | 否 | 默认当前年 |
| month | int | 否 | 默认最新有数据的月份 |

**请求示例**：
```
GET /api/metric/breakdown?metricId=3&type=trend&viewBy=monthly&year=2025&month=4
GET /api/metric/breakdown?metricId=3&type=category&year=2025&month=4
```

---

#### type=trend 响应示例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "type": "trend",
    "metricId": 3,
    "metricName": "Deal OPS",
    "metricType": "financial",
    "viewBy": "monthly",
    "chart": {
      "categories": ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
      "series": [
        {
          "name": "2024 Actual",
          "data": [1200000, 1350000, 1100000, 980000, null, null, null, null, null, null, null, null]
        },
        {
          "name": "2025 Actual",
          "data": [1300000, 1420000, 1250000, 1180000, null, null, null, null, null, null, null, null]
        },
        {
          "name": "2025 Target",
          "data": [1400000, 1450000, 1350000, 1240000, 1300000, 1280000, 1320000, 1360000, 1300000, 1280000, 1350000, 1400000]
        }
      ]
    },
    "ytdTable": {
      "columns": ["Year-to-date", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
      "rows": [
        {
          "label": "2025 Financial",
          "values": [5150000, 1300000, 1420000, 1250000, 1180000, null, null, null, null, null, null, null, null]
        },
        {
          "label": "2025 JBP Goal",
          "values": [5390000, 1400000, 1450000, 1350000, 1240000, null, null, null, null, null, null, null, null]
        },
        {
          "label": "+/- JBP Goal (Dollar)",
          "values": [-240000, -100000, -30000, -100000, -60000, null, null, null, null, null, null, null, null]
        },
        {
          "label": "2025 YoY",
          "values": [520000, 100000, 70000, 150000, 200000, null, null, null, null, null, null, null, null]
        },
        {
          "label": "2024 Financial",
          "values": [4630000, 1200000, 1350000, 1100000, 980000, null, null, null, null, null, null, null, null]
        }
      ]
    }
  }
}
```

---

#### type=category（及 subcategory / brand / asin）响应示例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "type": "category",
    "metricId": 3,
    "metricName": "Deal OPS",
    "metricType": "financial",
    "period": "April 2025",
    "rows": [
      {
        "dimensionValue": "All",
        "productTitle": null,
        "isTotal": true,
        "current": {
          "actual": 1180000,
          "jbpGoal": 1240000,
          "vsJbpGoalPct": -4.84,
          "momCtcBps": -120,
          "yoyCtcBps": 200
        },
        "ytd": {
          "actual": 5150000,
          "jbpGoal": 5390000,
          "vsJbpGoalPct": -4.45,
          "momCtcBps": -80,
          "yoyCtcBps": 150
        }
      },
      {
        "dimensionValue": "1080 Car Navigation",
        "productTitle": null,
        "isTotal": false,
        "current": {
          "actual": 420000,
          "jbpGoal": 450000,
          "vsJbpGoalPct": -6.67,
          "momCtcBps": -50,
          "yoyCtcBps": 80
        },
        "ytd": {
          "actual": 1850000,
          "jbpGoal": 1950000,
          "vsJbpGoalPct": -5.13,
          "momCtcBps": -30,
          "yoyCtcBps": 60
        }
      }
    ]
  }
}
```

> `asin` 类型时，`dimensionValue` 为 ASIN 编号，`productTitle` 为商品标题，其余字段结构相同。

---

## 项目结构
```
src/main/java/com/demo/crud/
├── controller/
│   └── MetricController.java     GET /api/metric/overview, /api/metric/breakdown
├── service/
│   └── MetricService.java        计算 MoM / YoY / YTD / CtC 逻辑
├── mapper/
│   ├── MetricMapper.java
│   └── MetricMonthlyDataMapper.java
│   └── MetricBreakdownDataMapper.java
├── model/
│   ├── Metric.java
│   ├── MetricMonthlyData.java
│   ├── MetricBreakdownData.java
│   └── dto/                      请求/响应 DTO
└── resources/mapper/             SQL XML
```

---

## 数据流
```
前端请求
  → MetricController（参数校验）
  → MetricService（查DB + 计算 MoM/YoY/YTD/CtC）
  → Mapper XML（原始月度数据/维度数据）
  → 组装 DTO → Result<T> 返回
```
