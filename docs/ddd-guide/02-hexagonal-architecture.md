# 02 · 六边形分层架构 与 本项目模块映射

## 为什么不能写事务脚本（Transaction Script）

一个方法里混杂"参数校验 + 查DB + 调外部服务 + 计算金额 + 落库 + 发消息"，会导致：
- **可维护性差**：任何一个外部依赖（DB 表结构、第三方 API、MQ 协议）变化，都要改这段代码。
- **可扩展性差**：新增一种相似场景（如新增一个供应商接入方式）基本无法复用，只能整段重写，滋生大量 if-else。
- **可测试性差**：想测试必须把 DB、MQ、第三方服务全部跑起来，测试用例数量随耦合的子步骤数指数级增长。

判断代码是否落入这个陷阱，看它是否违反了 SRP（单一职责）、DIP（依赖反转）、OCP（开闭原则）。

## 分层与依赖方向

```
        ┌────────────────────────────────────────┐
        │        Infrastructure（外层，可替换）      │
        │  Repository实现 / Port Adapter实现 / MQ Producer  │
        └───────────────┬──────────────────────────┘
                         │ 依赖
        ┌────────────────▼─────────────────────────┐
        │        Application（编排层）                │
        │  ApplicationService / DTO Assembler / CQE  │
        └───────────────┬──────────────────────────┘
                         │ 依赖
        ┌────────────────▼─────────────────────────┐
        │        Domain（核心，最稳定）                │
        │  Entity / Domain Service / Policy / Repo接口 │
        └───────────────┬──────────────────────────┘
                         │ 依赖
        ┌────────────────▼─────────────────────────┐
        │        Types（Domain Primitive，纯POJO）    │
        └────────────────────────────────────────────┘
```

依赖只能往内指，**Domain 和 Types 不允许 import 任何 Spring / MyBatis / RocketMQ / 第三方 SDK 的类**。Domain 层需要外部能力时，只依赖自己定义的接口（Repository、`adapter.port`），具体实现放 Infrastructure，由 Spring 在运行时注入。

## 与本项目模块的对应关系

| 文章里的层 | 本项目模块 | 放什么 | 依赖限制 |
|---|---|---|---|
| Types（DP层） | `ticket-catalog-types` | Domain Primitive、跨层共享的枚举/异常 | 不依赖任何模块 |
| Domain（领域层） | `ticket-catalog-domain` | Entity、Domain Service、Policy、Repository/`adapter.port` **接口**、Domain Event | 只依赖 `types` |
| Application（应用层） | `ticket-catalog-app` | ApplicationService、DTO Assembler | 依赖 `domain`、`api`（CQE）、`types`。不依赖 `trigger` / `infrastructure` |
| Interface（接口层） | `ticket-catalog-trigger`（`http` / `job` / `listener` / `rpc`） | Controller、Job、MQ Listener、RPC Provider；鉴权、Result 包装、异常兜底 | 依赖 `app` |
| DTO/CQE 类型 | `ticket-catalog-api` | 对外的 Command / Query / Event / DTO，按上下文分包 | 依赖 `types` |
| Infrastructure（基础设施层） | `ticket-catalog-infrastructure`（`adapter` / `dao` / `redis`） | Repository 实现、DAO/PO、出站端口实现、缓存 | 依赖 `domain`（实现其接口） |
| 启动（组合根） | `ticket-catalog-start` | `Application`、Spring 配置(`config`)、yml、logback、mybatis-config | 依赖 `trigger`、`infrastructure`，不写业务 |

落地时的判断顺序：
1. 先在 `domain` 里定义 Entity 和它依赖的 Repository/`adapter.port` **接口**（不管实现）。
2. 在 `app` 里写 ApplicationService，只调用 domain 接口和领域对象方法。
3. 最后在 `infrastructure` 补实现类，在 `trigger` 补协议入口。启动类和配置只放 `ticket-catalog-start`。

这个顺序保证了核心业务逻辑不会被数据库表结构、第三方接口签名"倒逼"设计。

## 代码演进速度不同

- `infrastructure`：变化最慢，只有外部依赖真的变了才动。
- `app`：Use Case 级别，接口相对稳定，新增业务用例 = 新增方法/新增 Service，不改老的。
- `domain`：变化最快，业务规则天天在变，但因为不依赖外部框架，改动成本最低、最容易测试。

如果发现"改一个业务规则要动 infrastructure 甚至 trigger"，基本可以断定是分层没做对，业务逻辑泄漏到了外层。

## 测试策略

- `types`、`domain`：纯 POJO，追求 100% 单测覆盖，不需要起 Spring 容器。
- `app`：mock 掉所有 domain 接口（Repository/`adapter.port`），验证编排逻辑本身没有分支遗漏。
- `infrastructure`：接口少、相对稳定，可以慢一点但要覆盖到；DB/MQ 相关建议用 Testcontainers 或本地环境跑一次集成测试即可，不需要每次全量跑。
- `trigger`：MockMVC 或直接测 Controller，把它依赖的 Application Service mock 掉。
- 全链路集成测试放在 `start`/启动模块级别，其余模块单测覆盖到位后再补少量集成测试验证真实链路。
