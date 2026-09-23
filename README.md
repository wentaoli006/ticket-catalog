# ticket-catalog

DDD 标准版工程，由本地脚手架 `ddd-scaffold-std` 生成。

## 1. 本地环境

本地安装 Docker 后，执行：

```shell
docker compose -f docs/dev-ops/environment/docker-compose.yml up -d
```

默认控制台：

| 组件 | 地址 | 账号 / 密码 |
| --- | --- | --- |
| Nacos | http://127.0.0.1:8848/nacos | nacos / nacos |
| Apollo Portal | http://127.0.0.1:8070 | apollo / admin |
| RocketMQ 控制台 | http://127.0.0.1:8080/#/ | admin / admin |
| XXL-JOB | http://127.0.0.1:9090/xxl-job-admin/ | admin / 123456 |
| Redis Commander | http://127.0.0.1:8081/ | admin / admin |
| phpMyAdmin | http://127.0.0.1:8899/ | root / 123456 |
| Grafana | http://127.0.0.1:3000 | admin / admin |
| Loki | http://127.0.0.1:3100/ready | 无账号，Promtail 推送 |

本机应用默认连接：

| 组件 | 地址 | 默认账密 / 标识 |
| --- | --- | --- |
| MySQL | `127.0.0.1:13306` | root / 123456，库 `demo_db_00`、`demo_db_01` |
| Redis | `127.0.0.1:6379` | 密码 `123456` |
| Nacos（Dubbo 注册） | `nacos://127.0.0.1:8848` | 无额外账密 |
| Apollo Config | `http://127.0.0.1:8082` | AppId `100000001` |
| RocketMQ NameServer | `127.0.0.1:9876` | Topic `ticket-catalog-mq`，Group `ticket-catalog-group` |
| XXL-JOB Admin | `http://localhost:9090/xxl-job-admin` | accessToken `default_token` |

Apollo 本机必须配 `apollo.meta=http://127.0.0.1:8082` 和 `apollo.configService=http://127.0.0.1:8082`，跳过 Eureka。AppId 为字符串 `100000001`，先在 Portal 创建项目并发布配置，客户端才能拉到。清空 Apollo 库后，Windows 客户端还可能读 `C:\opt\data\{appId}\config-cache\`，删掉该目录并重启应用才会用代码默认值。compose 会等 ConfigService / AdminService 健康后再起 Portal。

`docs/dev-ops/environment/sql` 只在 MySQL 数据目录为空时导入一次。改了 SQL 或库名后清数据卷再起：

```shell
docker compose -f docs/dev-ops/environment/docker-compose.yml -p environment down -v
docker compose -f docs/dev-ops/environment/docker-compose.yml -p environment up -d
```

RocketMQ 首次使用先创建 Topic 和消费组：

```shell
docs/dev-ops/environment/rocketmq/create-topic.sh
docs/dev-ops/environment/rocketmq/create-group.sh
```

## 2. 如何改中间件地址、账号、密码

默认配置只适合本机 Docker。连自己的环境时，容器侧和应用侧都要改。

### 2.1 改容器（端口 / 密码）

文件：`docs/dev-ops/environment/docker-compose.yml`

- MySQL 端口：`13306:3306`；root 密码：`MYSQL_ROOT_PASSWORD`；业务用户：`MYSQL_USER` / `MYSQL_PASSWORD`
- Redis 端口：`6379:6379`；密码同步改 `docs/dev-ops/environment/redis/redis.conf` 的 `requirepass`，以及 compose 里 `REDIS_HOSTS=local:redis:6379:0:<密码>`
- Nacos 端口：`8848`；库密码：`MYSQL_SERVICE_PASSWORD`
- Apollo Portal：`8070`；ConfigService 映射：`8082:8080`；库密码：`SPRING_DATASOURCE_PASSWORD`
- XXL-JOB：`9090`；库账号密码：`SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
- RocketMQ NameServer：`9876`；控制台：`8080`
- Loki：`3100`；Grafana：`3000`，账号 `GF_SECURITY_ADMIN_USER` / `GF_SECURITY_ADMIN_PASSWORD`

### 2.2 改应用连接

开发环境默认 `dev` profile，主要改这三个文件：

1. `ticket-catalog-start/src/main/resources/application.yml`
2. `ticket-catalog-start/src/main/resources/application-dev.yml`
3. `ticket-catalog-start/src/main/resources/sharding/sharding-jdbc-dev.yaml`

**Apollo**（`application.yml`、`META-INF/app.properties`）

- `app.id`：和 Portal 项目 AppId 一致
- `apollo.meta` / `apollo.configService`：配置中心地址

**Redis**（`application-dev.yml`）

```yaml
redis.sdk.config.host: 127.0.0.1
redis.sdk.config.port: 6379
redis.sdk.config.password: 123456
```

**Dubbo / Nacos**（`application-dev.yml`）

```yaml
dubbo.application.name: ticket-catalog
dubbo.registry.address: nacos://127.0.0.1:8848
dubbo.scan.base-packages: com.owen.api
```

**RocketMQ**（`application-dev.yml`）

```yaml
rocketmq.name-server: 127.0.0.1:9876
rocketmq.consumer.group: ticket-catalog-group
rocketmq.producer.group: ticket-catalog-group
```

Topic / Group 还在：

- `ticket-catalog-trigger/src/main/java/trigger/listener/MQListener.java`
- `ticket-catalog-trigger/src/main/java/trigger/job/XXLJob.java`
- `docs/dev-ops/environment/rocketmq/create-topic.sh`
- `docs/dev-ops/environment/rocketmq/create-group.sh`

**XXL-JOB**（`application-dev.yml`）

```yaml
xxl.job.accessToken: default_token
xxl.job.admin.addresses: http://localhost:9090/xxl-job-admin
xxl.job.executor.appname: xxl-job-executor-sample
```

**MySQL**（`sharding/sharding-jdbc-dev.yaml`）

```yaml
ds_0.jdbcUrl: jdbc:mysql://127.0.0.1:13306/demo_db_00?...
ds_0.username: root
ds_0.password: 123456
ds_1.jdbcUrl: jdbc:mysql://127.0.0.1:13306/demo_db_01?...
```

改库名时同步改 `docs/dev-ops/environment/sql/demo_db_00.sql`、`demo_db_01.sql`，以及 compose 里 MySQL healthcheck 的库名。

`application-test.yml` / `application-prod.yml` 的数据源默认注释掉，上测试/生产时按同样字段打开。

### 2.3 结构化日志（Loki / Grafana / Promtail）

职责拆分：

| 组件 | 放哪 | 说明 |
| --- | --- | --- |
| Loki + Grafana | `docs/dev-ops/environment/docker-compose.yml` | 和 Nacos/Apollo/MQ 一起，不必和 Java 同机 |
| Promtail | `docs/dev-ops/app/docker-compose-1.0.yml` | 必须在写日志的那台机器上 tail 文件 |
| JSON 文件日志 | `logback-spring.xml` | 控制台仍是文本；文件是 JSON，应用不直连 Loki |

**1. 起中间件（含 Loki、Grafana）**

```shell
docker compose -f docs/dev-ops/environment/docker-compose.yml up -d
```

打开 http://127.0.0.1:3000 ，账号 `admin` / `admin`。Explore 选 Loki。

**2. 起 Promtail（应用机）**

Java 用 Docker 部署时，`LOGGING_FILE_PATH=/data/log`，和 volume `./log:/data/log` 对齐。Promtail 默认采 `docs/dev-ops/app/log/log_info.log`。

```shell
docker compose -f docs/dev-ops/app/docker-compose-1.0.yml up -d promtail
```

IDEA 本机跑 Java 时，日志在 `ticket-catalog-start/data/log`，把目录指过去：

```shell
APP_LOG_DIR=../../../ticket-catalog-start/data/log docker compose -f docs/dev-ops/app/docker-compose-1.0.yml up -d promtail
```

Promtail 和 Loki 不在同一台时：

```shell
LOKI_URL=http://<中间件IP>:3100/loki/api/v1/push docker compose -f docs/dev-ops/app/docker-compose-1.0.yml up -d promtail
```

应用机能访问 `中间件IP:3100` 即可。改 Grafana 密码看 compose 里 `GF_SECURITY_ADMIN_*`。

**3. Grafana 怎么查**

```
{app="ticket-catalog"}
{app_id="100000001"}
{app_id="100000001"} |= "error"
{app_id="100000001"} | json | trace_id="xxxx"
{app_id="100000001"} | json | thread_id="42"
```

JSON 字段：`@timestamp`、`level`、`logger`、`thread_name`、`thread_id`、`trace_id`、`app_id`、`message`。

- `app` / `app_id` / `level`：Loki label
- `trace_id` / `thread_id` / `thread_name`：高基数，只在日志行里 `| json` 过滤，不要做成 label

`app_id` 来自 `ticket-catalog-start/src/main/resources/META-INF/app.properties` 的 `app.id`，和 Apollo AppId 同一份。改 AppId 时同步改这个文件。HTTP 请求会写 `trace_id`（有 `X-Trace-Id` 就沿用，没有就生成）；启动、MQ、Job 线程没有该字段。启动类是 `ticket-catalog-start` 的 `com.owen.Application`。

只采集正在写的 `log_info.log`（INFO 文件里也有 WARN/ERROR）。不要扫 `*.log`，滚动归档会重复入库。

### 2.4 改项目名、包名

生成后再改 `groupId` / `artifactId` / `package` 要动根 pom、各模块 pom、Java 包、`spring.config.name`、Dubbo 扫描包、RocketMQ Topic/Group。更稳的做法是用正确参数重新生成。
