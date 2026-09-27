# CLAUDE.md

本项目是票务分销上架系统，采用 DDD 六边形分层脚手架。详细业务背景见 `README2.md`。

## 模块结构（依赖只能向下，禁止反向依赖）

```
ticket-catalog-start → ticket-catalog-trigger → ticket-catalog-app → ticket-catalog-domain → ticket-catalog-types
                      → ticket-catalog-infrastructure ↗                  ↑
                      ticket-catalog-api ↗                               |
                                                            ticket-catalog-infrastructure（实现 domain 定义的接口）
```

- `ticket-catalog-types`：Domain Primitive、公共枚举/异常，纯 POJO，不依赖任何模块。
- `ticket-catalog-domain`：Entity、Domain Service、Policy、Domain Event、Repository/`adapter.port` **接口**（不含实现）。只依赖 `types`，不依赖 Spring/MyBatis 等框架。按限界上下文分包：`supplier` / `productagg` / `offlineproduct` / `publish`。
- `ticket-catalog-app`：ApplicationService、DTO Assembler。只编排流程，不写业务计算/校验。不依赖 `trigger` / `infrastructure`，也不放启动类。
- `ticket-catalog-api`：对外的 DTO / Command / Query 类型。
- `ticket-catalog-trigger`：`http`/`job`/`listener`/`rpc` 入口，统一返回 `Result`，兜底捕获异常。依赖 `app`，入口只调 ApplicationService。
- `ticket-catalog-infrastructure`：`dao`（DAO 接口与 `dao/po` 表对象）、`adapter/repository`（Repository 实现）、`adapter/port`（出站端口实现）、`redis`。
- `ticket-catalog-start`：组合根。`Application` 启动类、`config`、`application*.yml`、logback、mybatis-config、sharding。依赖 `trigger` + `infrastructure`，不写业务。

## 编码规范（详见 `docs/ddd-guide/`，写代码前先看对应章节）

写任何新功能前，先判断该放哪一层，判断依据见 [`docs/ddd-guide/README.md`](docs/ddd-guide/README.md) 里的速查表。

1. **[Domain Primitive](docs/ddd-guide/01-domain-primitive.md)**：有格式约束的基础类型（手机号、金额、订单号、班期日期等）必须封装成不可变、自校验的 DP 类，放 `ticket-catalog-types`，禁止裸 `String`/`BigDecimal` 到处传递校验逻辑。
2. **[分层架构](docs/ddd-guide/02-hexagonal-architecture.md)**：`domain`/`types` 不允许 import 任何框架或外部 SDK 类型；写代码顺序是先 domain（Entity + 接口）→ app（编排）→ infrastructure/trigger（实现）。
3. **[Repository 模式](docs/ddd-guide/03-repository-pattern.md)**：严格区分 `XxxDO`（infrastructure，数据库映射）/ `Xxx`（domain，业务实体）/ `XxxDTO`（api/app，对外传输）。Repository 接口在 domain，方法名用 `find`/`save`/`remove`，禁止 `insert`/`update`/`select` 语义；转换统一用 MapStruct。
4. **[领域层规范](docs/ddd-guide/04-domain-layer.md)**：Entity 不允许 public setter，只暴露带业务语义的方法；跨 Entity 的一致性操作放 Domain Service；同类实体的差异规则用 Policy 列表（而不是继承）；状态变更的连带副作用用 Domain Event 解耦。
5. **[应用层与防腐层](docs/ddd-guide/05-application-layer.md)**：ApplicationService 入参必须是 Command/Query/Event 对象（校验用 `@Valid` + Bean Validation 前置，方法体不写校验/计算），出参必须是 DTO（不能是 Entity）；对外部系统的依赖必须过 `adapter.port`；Interface 层统一返回 `Result` 并兜底异常，Application/Domain/Infrastructure 层直接抛异常。
6. **[战略设计](docs/ddd-guide/06-strategic-design.md)**：本项目有 5 个限界上下文（SupplierContext/ProductAggContext/OfflineProductContext/PublishContext/AdminViewContext），禁止跨上下文直接引用对方的 Entity/Repository，只能走事件（跨上下文用 MQ）或查对方 ApplicationService 拿 DTO；代码命名要和 README2.md 的业务词汇一一对应（如"班期"必须叫 `Schedule`，不要自造同义词）。

## 本项目的业务红线（摘自 README2.md，编码时务必遵守）

- 0 订单、0 履约、0 真实库存扣减，这些全部在外部履约应用，本系统只做展示库存。
- API 链路必须走聚合（原始数据→聚合→快照比对→变更清单→先下后上）；Offline 链路禁止聚合，人工录入直接成品发布。
- Product 统一售卖时间，PLU 不管理售卖时间，可售日期/单日库存/单日状态全部下沉到班期（schedule）。
- 上架固定顺序：先下架失效，再上新/更新。
