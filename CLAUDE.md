# SpringBoot ECS Demo - Claude Instructions

## 项目简介
Spring Boot 3.2 + MyBatis + MySQL CRUD项目，部署在AWS ECS Fargate上。

## 技术栈
- Java 17
- Spring Boot 3.2
- MyBatis 3.0（XML方式写SQL）
- MySQL 8.0（本地）/ AWS RDS（生产）
- Docker + AWS ECS Fargate
- GitHub Actions CI/CD

## 项目结构
```
src/main/java/com/demo/crud/
├── controller/   只做参数校验，调用Service，不写业务逻辑
├── service/      业务逻辑，事务在这层
├── mapper/       MyBatis接口，只定义方法
├── model/        实体类 + 通用返回Result<T>
└── exception/    BusinessException + GlobalExceptionHandler
src/main/resources/
├── mapper/       SQL XML文件
└── application.yml
```

## 代码规范
- 统一返回 Result<T>，不要直接返回实体
- 业务异常统一抛 BusinessException，不要用 RuntimeException
- Service层需要事务的方法加 @Transactional
- 日志用 @Slf4j + log.info/warn/error，不要用 System.out.println
- 所有数据库配置用环境变量，不要硬编码密码
- 新增实体时同步更新 schema.sql 和对应的 Mapper XML

## 常用命令
```bash
# 本地启动
mvn spring-boot:run

# 打包（跳过测试）
mvn clean package -DskipTests

# 跑测试
mvn test

# Docker构建
docker build -t springboot-ecs-demo .

# Docker本地运行
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_USER=root \
  -e DB_PASSWORD=password \
  springboot-ecs-demo
```

## 环境变量说明
| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_HOST | 数据库地址 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_NAME | 数据库名 | demo_db |
| DB_USER | 用户名 | root |
| DB_PASSWORD | 密码 | password |
| SERVER_PORT | 服务端口 | 8080 |

## API规范
- 统一前缀：/api
- 分页参数：page（从1开始）、size（默认10）
- 健康检查：GET /actuator/health

## 注意事项
- 新增功能前先看现有代码结构，保持风格一致
- SQL写在XML里，不要用注解方式
- 不要修改 Dockerfile 和 .github/workflows/cicd.yml，除非明确要求

## 常用指令
- "帮我部署" → 读取并执行 .claude/skills/deploy.md 的流程
- "帮我测试" → 读取并执行 .claude/skills/test.md 的流程
- "查看日志" → 去CloudWatch查 /ecs/springboot-ecs-demo 日志组
- "新增CRUD" → 读取并执行 .claude/skills/crud.md 的流程
- "帮我review" → 读取并执行 .claude/skills/review.md 的流程
- "帮我debug" → 读取并执行 .claude/skills/debug.md 的流程

## Skills说明
所有skill文件在 .claude/skills/ 目录下
执行任何指令前先读取对应的skill文件