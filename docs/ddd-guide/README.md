# DDD 开发知识库（本项目专用）

来源：阿里技术专家殷浩「DDD 系列」1~5 讲 + 通用 DDD 理论补充，已按本项目的模块结构（`ticket-catalog-types` / `-domain` / `-app` / `-infrastructure` / `-trigger` / `-api`）重新组织为可执行的编码规范。写代码前先看对应章节，不确定放哪一层时优先查《04 领域层》和《05 应用层》的判定表；不确定该不该跨模块调用时查《06 战略设计》。

## 目录

1. [Domain Primitive](01-domain-primitive.md) —— 把隐性概念/上下文显性化，杜绝裸 `String`/`BigDecimal` 参数
2. [六边形分层架构](02-hexagonal-architecture.md) —— 模块依赖方向、每层职责、和本项目模块的对应关系
3. [Repository 模式](03-repository-pattern.md) —— Entity/DO/DTO 三对象规范、Repository 接口写法、变更追踪
4. [领域层设计规范](04-domain-layer.md) —— Entity / Domain Service / Policy / Domain Event 怎么选、聚合一致性边界、工厂
5. [应用层与防腐层](05-application-layer.md) —— Interface 层、CQE、DTO Assembler、出站端口、Orchestration vs Choreography
6. [战略设计](06-strategic-design.md) —— 限界上下文（本项目的 5 个上下文）、跨上下文通信规则、通用语言、充血模型 vs 贫血模型

## 速查：新需求该往哪里写代码

| 你要做的事 | 放在哪 | 对应模块 |
|---|---|---|
| 一个有格式约束的基础类型（手机号、金额、订单号...） | Domain Primitive（VO） | `ticket-catalog-types` |
| 单个实体自身状态变更 + 校验 | Entity 方法 | `ticket-catalog-domain` |
| 跨多个 Entity 的一致性操作 | Domain Service | `ticket-catalog-domain` |
| 同类实体的可插拔规则（不同供应商/不同商品类型的差异逻辑） | Policy/Strategy | `ticket-catalog-domain` |
| 状态变更后的连带副作用（发通知、加积分...） | Domain Event | `ticket-catalog-domain` + EventBus |
| 一次业务用例的流程编排（查数据→调领域对象→落库→发消息） | Application Service | `ticket-catalog-app` |
| HTTP/RPC/MQ/Job 入口，鉴权、Result 包装、异常兜底 | Interface | `ticket-catalog-trigger` |
| 启动类、Spring 配置、yml | 组合根 | `ticket-catalog-start` |
| 对外暴露的 DTO/Command/Query 类型 | DTO/CQE | `ticket-catalog-api` |
| 数据库读写、字段映射 | Repository 实现 + DO | `ticket-catalog-infrastructure` |
| 调用外部服务/中间件 | 出站端口 Adapter | `ticket-catalog-infrastructure` |
| 需要用到别的限界上下文（如 SupplierContext）的数据 | 事件订阅或查对方 ApplicationService 拿 DTO，禁止直连对方 Entity/Repository | 见 06 战略设计 |

## 铁律（贯穿全部层级）

- **DP 优先**：任何"有格式约束的基础类型"一旦被多处校验或有隐性业务逻辑（如取区号），必须封装成 Domain Primitive，禁止裸 `String`/`Long`/`BigDecimal` 到处传。
- **依赖方向只能向内**：`api`/`trigger`/`infrastructure` 依赖 `domain`/`types`，反过来禁止。`domain`、`types` 不依赖任何框架、任何外部服务的具体实现类。
- **Entity 不能有 public setter**，状态变更只能通过带业务语义的方法（`pay()`、`ship()`），且方法内部要保证不变性（Invariant）。
- **ApplicationService 只编排，不做计算、不写 if/else 业务判断**：校验用 Bean Validation 前置，计算下沉到 Entity/DomainService/Policy。
- **Interface 层统一返回 `Result` 并兜底捕获所有异常；Application/Domain/Infrastructure 层直接抛异常，不用 `Result` 包装。**
- **外部依赖（供应商 API、中间件、别的团队的服务）必须过 `adapter.port`**，禁止在 Domain/Application 直接引用对方的 DO/DTO/SDK 类型。
- **禁止跨限界上下文直接引用对方的 Entity/Repository**（如 OfflineProductContext 不能 import ProductAggContext 的 `ApiProduct`），只能走事件或查对方 DTO，详见 06 战略设计。
