# 06 · 战略设计（Bounded Context / 通用语言 / 充血模型）

> 01~05 讲的是战术设计（Entity 怎么写、Repository 怎么分层）。这一讲讲更上层的问题：业务边界怎么划、模块之间怎么通信、代码里的名词怎么和业务对齐。这些决策做错了，战术设计做得再规范也没用——错误的边界会让"跨上下文"的耦合渗透进每一层。

## 限界上下文（Bounded Context）：本项目的 5 个上下文

本项目（详见 `README2.md`）共有 5 个业务上下文 + 1 个基础设施层，**没有订单/履约上下文**（订单履约在外部系统，本系统只做展示和中转）：

| 上下文 | 核心实体 | 边界（不做什么） |
|---|---|---|
| SupplierContext（供应商接入） | Supplier、ApiProject、ApiSession、ApiTicketItem | 只处理原始数据，不聚合、不上架、不碰 Offline 数据 |
| ProductAggContext（API商品聚合） | ApiProduct、ApiPlu | 纯计算域，无 RPC/无外部调用，完全不处理 Offline 业务 |
| OfflineProductContext（Offline商品管理） | OfflineProduct、OfflinePlu、OfflineSchedule | 只维护数据，不聚合、不自动上架（上架由 PublishContext 执行） |
| PublishContext（货架发布） | PublishRecord、PublishItem | 只做统一发布编排，不产生商品数据（消费 API 聚合结果 / Offline 成品数据） |
| AdminViewContext（运营视图后台） | 无独立实体，薄视图层 | 无业务规则，只查询/权限/日志 + Offline 履约信息中转，不做履约业务处理 |

## 跨上下文通信规则

**禁止跨 Context 直接 import 对方的 Entity/Repository**——即使两个上下文的实体长得很像（如 `ApiProduct` 和 `OfflineProduct`），也不能互相引用对方的领域对象，只能通过下面两种方式协作：

1. **事件驱动**（多数场景）：上游发 Domain Event（跨上下文走 MQ，见 04 · 领域层规范的 EventBus vs MQ 一节），下游订阅后用自己的 Command/Event 对象重新表达，不直接反序列化对方的 Entity。例：SupplierContext 原始数据变更 → 事件 → ProductAggContext 消费后自己发起聚合。
2. **只读查询**（少数场景，如 AdminViewContext 汇总展示）：通过对方暴露的 ApplicationService 方法拿 DTO，禁止直接查对方的表或 Repository。

判断某段代码是否"越界"，看它是否符合上表里每个上下文的"边界"限定——一旦发现 OfflineProductContext 的代码里出现聚合逻辑，或者 ProductAggContext 里出现了发货架 RPC 调用，说明业务逻辑放错了上下文。

## 通用语言（Ubiquitous Language）

代码里的类名/方法名/字段名必须和 `README2.md` 里的业务词汇保持一个词对一个词的映射，禁止程序员自造同义词：

| 业务词汇 | 代码命名 | 反例 |
|---|---|---|
| 班期 | `Schedule` | `DateSlot`、`SessionDate` |
| 展示库存 | `stockShowNum` / `ShowStock` | `inventory`（容易和"真实库存"混淆，本系统没有真实库存概念） |
| 聚合 | `aggregate`（动词）/ `Agg`（前缀，如 `ApiProduct`） | 不要叫 `merge`/`sync`，业务上"聚合"特指原始数据→标准商品的计算过程 |
| 上架/下架 | `publish` / `unpublish` | 不要混用 `online`/`offline`（`Offline` 在本项目已经是"线下供应商接入方式"的专有名词，撞车） |

一旦发现代码里的命名和业务方/文档里说的不是同一个词，先去确认是不是理解错了业务概念，而不是"顺手"翻译一个看起来更像英文的词。

## 充血模型 vs 贫血模型

- **贫血模型（Anemic Domain Model）**：Entity 只有字段 + get/set，所有业务逻辑写在 Service 里，Service 疯狂膨胀成事务脚本（回顾 02 · 六边形架构里"为什么不能写事务脚本"）。
- **充血模型（Rich Domain Model）**：业务规则回归 Entity/Domain Service/Policy（04 · 领域层规范的决策表），Service 只做编排（05 · 应用层规范）。

本项目已有的强制规则本质上都是在强制充血模型落地：**Entity 不允许 public setter**（04 规范第 2 条）+ **ApplicationService 不能有业务计算/分支判断**（05 规范）。判断一个新 PR 是不是又在写贫血模型，看 diff 里 ApplicationService 是不是又新增了 if-else 或者算钱的代码——如果是，说明该下沉到 Entity/Domain Service 了。

## 战略设计自检清单

- [ ] 这段代码是否 import 了别的 Bounded Context 的 Entity 或 Repository？如果有，改成事件或 DTO 查询。
- [ ] 新加的类名/字段名，能不能在 `README2.md` 里找到对应的原文？找不到就先跟业务对齐概念，别自己起名。
- [ ] ApplicationService 的方法体是不是又长出了 if-else 或算钱逻辑？是的话说明业务逻辑正在从充血模型滑向贫血模型。
- [ ] 这个上下文的代码，是否越权做了表里"边界"栏写明不该做的事（如 OfflineProductContext 里出现聚合、AdminViewContext 里出现履约业务处理）？
