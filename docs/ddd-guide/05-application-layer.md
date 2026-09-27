# 05 · 应用层 / 接口层 / 防腐层

## Interface 层（本项目：`ticket-catalog-trigger`）

覆盖 HTTP Controller、RPC Provider、MQ Listener、Job 调度入口——凡是"有自己的网络/调用协议"的入口，都属于这一层。

**规范**：
- 只做协议转化、鉴权、Session 管理、限流、日志、异常兜底，**不写业务逻辑**。
- **统一返回 `Result<T>`，捕获所有异常**，避免堆栈信息泄漏给调用方。用 AOP（`@ResultHandler` 注解 + `@Around`）统一处理，不要每个方法手写 try/catch。
- **不追求"一个领域一个大而全的接口类"**：不同业务场景即使数据结构相似，只要语义或未来演进方向不同，就拆成不同的 Controller/接口类（如 `PetCardController` 和 `BabyCardController` 分开，不要塞进同一个 `CardController` 硬套统一参数）。Interface 层是可以快速膨胀的，Application 层保持稳定即可。

```java
@PostMapping("checkout")
@ResultHandler
public Result<OrderDTO> checkout(@RequestBody CheckoutCommand cmd) {
    return Result.success(checkoutService.checkout(cmd));
}
```

## Application 层（本项目：`ticket-catalog-app`）

### CQE：Command / Query / Event

**规范：ApplicationService 方法入参只能是一个 Command / Query / Event 对象**（唯一例外：按单一 ID 查询可以直接传 ID）。禁止一堆裸参数平铺在方法签名上。

| | Command | Query | Event |
|---|---|---|---|
| 语义 | 希望触发的写操作 | 只读查询 | 已经发生的事实 |
| 返回值 | DTO 或 Boolean | DTO 或 Collection | 无返回值 |

- **不同语义不复用同一个 CQE 对象**，即使字段完全一样（典型反例：Create 和 Update 共用一个对象，靠"ID 是否为空"区分——校验规则、语义都不同，必须拆成两个类）。
- **校验前置**：CQE 对象用 JSR-380（`@NotNull`/`@Positive`/`@Min`/`@Max` 等）+ Spring `@Validated`，ApplicationService 方法体内**不再写任何参数校验代码**。

```java
public interface CheckoutService {
    OrderDTO checkout(@Valid CheckoutCommand cmd);
    List<OrderDTO> query(OrderQuery query);
    OrderDTO getOrder(Long orderId); // 单一ID查询例外
}
```

### ApplicationService 只编排，不做决策

自检：Cyclomatic Complexity 应尽量等于 1（除了 precondition 式的中断判断，如库存不足直接抛异常）；不能有任何金额/业务计算（这类计算应该是 Entity 的方法或计算属性）；不能有分支型 if-else 业务判断（应下沉到 Domain Service/Policy）。

常见套路三步：**准备数据（查 Repository/Port）→ 执行操作（纯内存调用领域对象方法）→ 持久化（Repository.save + 发消息）**。

**出参永远是 DTO，不能是 Entity**：
- 避免调用方直接依赖 Entity 里的业务规则。
- DTO 可以自由组合多个 Entity/VO 的字段，Entity 数量有限但 DTO 可以按场景自由拼。

用 `DTO Assembler`（MapStruct 接口）做 Entity→DTO 的转换，禁止手写大量 getter/setter 拼装代码：

```java
@Mapper
public interface OrderDtoAssembler {
    OrderDtoAssembler INSTANCE = Mappers.getMapper(OrderDtoAssembler.class);
    @Mapping(target = "itemTitle", source = "item.title")
    OrderDTO toDTO(Order order, Item item);
}
```

### 异常处理

Application/Domain/Infrastructure 层**直接抛异常，不用 `Result` 包装**，只有 Interface 层统一兜底转换。

## 防腐层（本项目：domain `adapter.port` + infrastructure `adapter.port`）

任何对外部系统（供应商 API、别的团队的 RPC 服务、第三方 SDK）的依赖，必须经过出站端口隔离，禁止 Application/Domain 直接引用对方的 DO/SDK 类型。

```
无端口：ApplicationService → 直接依赖 ExternalItemService + ItemDO（外部字段一变，业务代码就得改）
有端口：ApplicationService → IXxxPort（领域自己的值对象） → Adapter 实现
```

端口实现能承载的能力：适配器（转换对方的数据结构/协议）、缓存、兜底（对方挂了返回缓存或默认值）、功能开关、易于 mock 测试。**Repository 本质上就是面向数据持久化的一种端口。**

> 术语对照：Ports and Adapters（六边形架构）里把这类"domain 定义接口、infrastructure 提供实现"的东西叫 **Port** + **Adapter**。本项目代码落地：接口放 `domain.{context}.adapter.port`（如 `IPoiPort`），实现放 `infrastructure.adapter.port`。资料里的 ACL/Facade 是同一模式的另一种叫法。

## Orchestration vs Choreography（复杂流程怎么设计）

| | Orchestration（编排） | Choreography（协同） |
|---|---|---|
| 驱动力 | 指令驱动，上游主动调用下游 | 事件驱动，下游被动响应事件 |
| 依赖 | 上游强依赖下游 | 下游依赖上游的事件类型，但无直接调用 |
| 灵活性 | 较差，新增流程要改上游代码 | 较高，可以新增/替换 Handler 不动上游 |
| 业务职责 | 上游对整条链路结果负责 | 没有单一责任人，每个环节各管自己 |

**判断方法**：先明确"谁必须为整体业务结果负责"——那个负责人所在的服务，用 Orchestration 主动调用下游；没有全局负责人、下游服务对上游无感知的场景，用 Choreography（发事件）。

**这一层决策只影响 Interface 层怎么触发 ApplicationService**（是被 Controller 主动调用，还是被 MQ Listener 收到事件后调用），**ApplicationService 本身对 O/C 无感知**——它天然处理 Command/Query/Event，不关心这些对象是被谁、以什么方式送进来的。不要为了赶事件驱动的潮流，把本来该同步编排、有明确责任人的流程硬拆成一堆事件（"手里有锤子，所有问题都看成钉子"）。
