# Scorecard Backend - Claude Instructions

## 项目简介
Scorecard Performance Monitoring 后端，基于 Spring Boot 3.2 + MyBatis + MySQL。
提供2个接口供前端消费，无鉴权，直接返回指标数据。

## 技术栈
- Java 17
- Spring Boot 3.2
- MyBatis 3.0（XML方式写SQL）
- MySQL 8.0（本地）/ AWS RDS（生产）
- Lombok
- Springdoc OpenAPI（Swagger UI：`/swagger-ui/index.html`）
- Docker + AWS ECS Fargate

## 包路径
`com.demo.crud`

## 数据库
- DB name：`test1`（见 `application.yml`）
- 3张业务表（见 `design-doc.md` DDL）：
  - `metrics`：指标定义（5条固定记录）
  - `metric_monthly_data`：月度实际值 + JBP目标
  - `metric_breakdown_data`：Category/Subcategory/Brand/ASIN 维度分解数据

## 项目结构
```
src/main/java/com/demo/crud/
├── controller/
│   ├── UserController.java        已有，勿动
│   └── MetricController.java      新增，只写 /api/metric/overview 和 /api/metric/breakdown
├── service/
│   ├── UserService.java           已有，勿动
│   └── MetricService.java         新增，计算 MoM/YoY/YTD/CtC 逻辑
├── mapper/
│   ├── UserMapper.java            已有，勿动
│   ├── MetricMapper.java          新增
│   ├── MetricMonthlyDataMapper.java  新增
│   └── MetricBreakdownDataMapper.java  新增
├── model/
│   ├── User.java / UserRequest.java / Result.java   已有，勿动
│   ├── Metric.java                新增，对应 metrics 表
│   ├── MetricMonthlyData.java     新增
│   ├── MetricBreakdownData.java   新增
│   └── dto/                       新增，请求参数 + 响应 DTO
│       ├── OverviewRequest.java
│       ├── OverviewResponse.java
│       ├── BreakdownRequest.java
│       └── BreakdownResponse.java
├── config/
│   └── SwaggerConfig.java         已有，勿动
└── exception/
    ├── BusinessException.java     已有，勿动
    └── GlobalExceptionHandler.java 已有，勿动

src/main/resources/
├── mapper/
│   ├── UserMapper.xml             已有，勿动
│   ├── MetricMapper.xml           新增
│   ├── MetricMonthlyDataMapper.xml  新增
│   └── MetricBreakdownDataMapper.xml  新增
├── application.yml                已有，勿动（除非要改DB名）
└── schema.sql                     追加3张新表的DDL + Seed数据
```

## 核心接口
详细请求/响应结构见 `design-doc.md`。

### GET `/api/metric/overview`
- Query：`year`（默认当前年）、`month`（默认最新有数据月份）
- 返回：5个指标 × {当期实际值、JBP Goal、MoM、YoY、YTD各项}

### GET `/api/metric/breakdown`
- Query：`metricId`（必填）、`type`（trend/category/subcategory/brand/asin）、`viewBy`（monthly默认）、`year`、`month`
- type=trend：返回图表series数据 + YTD明细表格行
- type=category等：返回各维度分解行数据（含CtC）

## 代码规范
- 统一返回 `Result<T>`，success code=200，error code 自定义
- 业务异常统一抛 `BusinessException`，不用 `RuntimeException`
- Service层事务方法加 `@Transactional`（查询接口不需要）
- 日志用 `@Slf4j` + `log.info/warn/error`，不用 `System.out.println`
- 所有数据库配置用环境变量，不硬编码
- SQL 全部写在 XML 里，不用注解 `@Select`
- DTO 用 Lombok `@Data`，实体类也用 Lombok

## 计算逻辑（在 MetricService 里实现）
- **MoM**：当期 actual - 上期 actual；MoM% = MoM / 上期 actual × 100
- **YoY**：当期 actual - 去年同期 actual；YoY% = YoY / 去年同期 actual × 100
- **YTD actual**：当年1月 ~ 选定月的 actual 累加（financial/integer类型）；percentage类型取加权平均
- **YTD JBP Goal**：同上
- **vsJbpGoalPct**：(actual - jbpGoal) / jbpGoal × 100
- **CtC (bps)**：(当前维度 actual / 总 actual - 上期维度 actual / 上期总 actual) × 10000

## Seed数据要求
`schema.sql` 需补充：
- `metrics` 5条：revenue / net_ppm / deal_ops / new_customer_count / net_ordered_gms
- `metric_monthly_data`：2024全年 + 2025年1月至今，每个指标都有数据
- `metric_breakdown_data`：每个指标 × 每月 × category/subcategory/brand/asin，各至少3~5条明细

## 环境变量
| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_HOST | 数据库地址 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_NAME | 数据库名 | test1 |
| DB_USER | 用户名 | root |
| DB_PASSWORD | 密码 | password |
| SERVER_PORT | 服务端口 | 8080 |

## 常用命令
```bash
# 本地启动
mvn spring-boot:run

# 打包（跳过测试）
mvn clean package -DskipTests

# Docker构建
docker build -t scorecard-backend .

# Docker本地运行
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_USER=root \
  -e DB_PASSWORD=password \
  scorecard-backend
```

## 注意事项
- 只操作 `backend/` 目录，不修改 `frontend/`
- 不动已有的 User 相关代码（UserController/UserService/UserMapper）
- 新增代码保持与现有风格一致（Lombok、Result<T>、XML SQL）
- 完成后输出 `api-spec.md`（供前端对接用）


## TDD规范（严格遵守）

### 阶段A：测试先行
先写所有测试用例，不准写任何实现代码
测试必须包含：
  - 正常流程
  - 边界条件
  - 异常情况
完成后列出测试清单等用户确认

### 阶段B：等用户确认后才能写实现
触发词："测试确认，开始实现"
写最小实现让测试通过
重构
确认全部绿色