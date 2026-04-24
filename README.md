# Spring Boot ECS Demo

Spring Boot 3.2 + MyBatis + MySQL CRUD，部署在 AWS ECS Fargate，通过 GitHub Actions 自动 CI/CD。

---

## 本地运行

### 1. 准备数据库
```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 2. 启动
```bash
export DB_HOST=localhost DB_USER=root DB_PASSWORD=password
mvn spring-boot:run
```

### 3. 测试接口
```bash
# 查询列表
curl http://localhost:8080/api/users

# 新增
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"robin","email":"robin@example.com"}'

# 健康检查
curl http://localhost:8080/actuator/health
```

---

## AWS部署步骤

### Step 1：创建 ECR 仓库
```bash
aws ecr create-repository --repository-name springboot-ecs-demo --region ap-northeast-1
# 记录返回的 repositoryUri
```

### Step 2：创建 RDS MySQL
```
控制台路径：RDS → Create database
  Engine: MySQL 8.0
  Template: Free tier
  DB name: demo_db
  Username: admin
  Password: 自己设置
  VPC: 默认VPC
  Public access: Yes（测试用，生产改No）
```
记录 Endpoint 地址，后面用。

### Step 3：初始化 RDS 数据库
```bash
mysql -h <RDS_ENDPOINT> -u admin -p < src/main/resources/schema.sql
```

### Step 4：创建 ECS Cluster
```bash
aws ecs create-cluster --cluster-name springboot-cluster --region ap-northeast-1
```

### Step 5：创建 Task Definition
保存为 task-definition.json，替换 <YOUR_ECR_URI> 和 <YOUR_AWS_ACCOUNT_ID>：
```json
{
  "family": "springboot-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::<YOUR_AWS_ACCOUNT_ID>:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "springboot-ecs-demo",
      "image": "<YOUR_ECR_URI>/springboot-ecs-demo:latest",
      "portMappings": [{"containerPort": 8080, "protocol": "tcp"}],
      "environment": [
        {"name": "DB_HOST",     "value": "<RDS_ENDPOINT>"},
        {"name": "DB_PORT",     "value": "3306"},
        {"name": "DB_NAME",     "value": "demo_db"},
        {"name": "DB_USER",     "value": "admin"},
        {"name": "DB_PASSWORD", "value": "<YOUR_DB_PASSWORD>"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/springboot-ecs-demo",
          "awslogs-region": "ap-northeast-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```
```bash
# 注册 Task Definition
aws ecs register-task-definition --cli-input-json file://task-definition.json
```

### Step 6：创建 CloudWatch Log Group
```bash
aws logs create-log-group --log-group-name /ecs/springboot-ecs-demo --region ap-northeast-1
```

### Step 7：创建 ECS Service
```bash
aws ecs create-service \
  --cluster springboot-cluster \
  --service-name springboot-service \
  --task-definition springboot-task \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[<YOUR_SUBNET_ID>],securityGroups=[<YOUR_SG_ID>],assignPublicIp=ENABLED}" \
  --region ap-northeast-1
```

### Step 8：配置 GitHub Secrets
```
仓库 → Settings → Secrets and variables → Actions → New repository secret

添加：
  AWS_ACCESS_KEY_ID      → IAM用户的Access Key
  AWS_SECRET_ACCESS_KEY  → IAM用户的Secret Key
```

### Step 9：更新 cicd.yml 里的环境变量
```yaml
env:
  AWS_REGION: ap-northeast-1           # 你的区域
  ECR_REPOSITORY: springboot-ecs-demo  # 你的ECR仓库名
  ECS_CLUSTER: springboot-cluster      # 你的集群名
  ECS_SERVICE: springboot-service      # 你的服务名
  ECS_TASK_DEFINITION: springboot-task # 你的Task Definition名
  CONTAINER_NAME: springboot-ecs-demo  # 你的容器名
```

### Step 10：推送代码触发 CI/CD
```bash
git add .
git commit -m "feat: deploy to ECS"
git push origin main
# 去 GitHub → Actions 查看流水线
```

---

## CI/CD 流程

```
git push main
     ↓
GitHub Actions 触发
     ↓
Job1: mvn test（构建+测试）
     ↓
Job2: docker build → push to ECR
     ↓
Job3: 更新 ECS Task Definition → 滚动部署
     ↓
CloudWatch 收集日志和指标
```

---

## IAM 权限（给 GitHub Actions 用的 IAM 用户）

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecs:DescribeTaskDefinition",
        "ecs:RegisterTaskDefinition",
        "ecs:UpdateService",
        "ecs:DescribeServices",
        "iam:PassRole",
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "*"
    }
  ]
}
```

---

## MCP 配置（Claude Code 本地开发）

编辑 `.claude/mcp.json`，填入真实的配置：
```json
{
  "mcpServers": {
    "mysql": { ... },     // 本地MySQL直连
    "github": { ... },    // GitHub操作
    "aws": { ... }        // AWS资源查询
  }
}
```

安装 MCP 依赖：
```bash
npm install -g @benborla29/mcp-server-mysql
npm install -g @modelcontextprotocol/server-github
npm install -g aws-mcp-server
```
