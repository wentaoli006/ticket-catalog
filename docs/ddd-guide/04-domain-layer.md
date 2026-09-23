# 04 · 领域层设计规范（Entity / Domain Service / Policy / Domain Event）

> 模块归属：全部在 `ticket-catalog-domain`。

## 决策表：这段规则该写在哪

| 规则类型 | 例子 | 放哪 |
|---|---|---|
| 只影响单个 Entity 自身状态 | 订单支付后状态从"待支付"变"已支付" | Entity 方法 |
| 单 Entity 状态变更但要参考只读的外部规则/数据 | 判断某职业能不能装备某武器 | Domain Service（单对象策略型），通过方法参数注入，Entity 内部 Double Dispatch 调用 |
| 会同时修改 2 个以上 Entity | 转账要同时改 A 账户和 B 账户 | Domain Service（跨对象事务型），直接在 Service 里调用，不经过任何一个 Entity 转发 |
| 同类实体但规则因子类型而不同，且未来会持续增加分支 | 不同怪物对不同伤害类型的抗性；不同供应商的库存兜底策略 | Policy/Strategy 列表 + `canApply()` 判断 |
| 跨对象的通用行为，不该绑死在某个具体实体上 | 统一的坐标移动/边界限制逻辑 | 通用组件型 Domain Service（类似 ECS 的 System） |
| 状态变更后的连带副作用，且副作用会持续增加 | 打怪掉血为0后加经验，经验满了升级，升级后发奖励 | Domain Event + EventHandler，不要写成嵌套 if |

**为什么不能用继承实现差异化规则**：Java 不支持多重继承，子类差异逻辑会导致父类方法被到处 override，一旦新增一种类型或规则，往往要改遍所有子类，违反开闭原则（OCP）。优先用组合（Policy 列表 + `canApply`）而不是继承树。

## Entity 规范

1. **创建即一致（Invariant at construction）**：要么构造函数强校验所有必要字段，要么用 Factory 封装重复参数的组装逻辑，禁止"先 new 出来再一个个字段慢慢 set"。
2. **不允许 public setter**，只暴露带业务语义的方法，方法名要"行为化"（`pay()` 而不是 `setStatus(1)`，`moveTo(x, y)` 而不是 `setPosition(x, y)`）。
3. **聚合根负责保证子实体一致性**：子实体不能有独立 Repository，只能通过聚合根的 Repository 一起存取；子实体可以有自己的状态变更方法，但跨子实体的一致性校验必须在聚合根层完成。
4. **不能强依赖其他聚合根实体或领域服务**：
   - 引用别的聚合根，只存它的强类型 ID（如 `WeaponId` 而不是 `Weapon`），不要直接持有对象引用。
   - 需要引用无副作用的只读服务时，通过方法参数传入（Double Dispatch），不要用 `@Autowired` 直接注入到 Entity 字段。
5. **一个 Entity 的方法只能直接影响自己（和自己的子实体）**，不能在方法里偷偷修改别的实体——这类跨实体影响必须走 Domain Service。

```java
public class Player {
    // 单对象策略型：通过参数传入，反转调用
    public void equip(Weapon weapon, EquipmentService equipmentService) {
        if (!equipmentService.canEquip(this, weapon)) {
            throw new IllegalArgumentException("Cannot Equip: " + weapon);
        }
        this.weaponId = weapon.getId();
    }
}
```

```java
// 跨对象事务型：不要经过任何一个 Entity 转发，直接在 Domain Service 里操作双方
public interface CombatService {
    void performAttack(Player player, Monster monster);
}
```

## 聚合（Aggregate）与一致性边界

聚合根（Aggregate Root）是聚合的唯一外部入口，Repository 只能面向聚合根存取（见 03 · Repository 模式）。判断一致性放哪一层：

- **聚合内部：强一致（事务一致）**。聚合根的一次方法调用要保证自己和所有子实体改完后状态自洽，落库在同一个事务里（如本项目的 `Product` 和它下属 `PLU`——改 PLU 状态时 Product 的聚合规则要在同一次调用里保证一致）。
- **聚合之间：最终一致（允许短暂不一致，通过事件/异步补齐）**。跨聚合的联动不要为了"强一致"把两个聚合捆进一个事务，而是发 Domain Event，由另一方的 Handler 异步接手（如本项目 SupplierContext 的原始数据变更 → ProductAggContext 的聚合重算，两者是不同聚合/不同上下文，天然只能最终一致）。

**聚合边界怎么划**：只有"必须同时保证正确性"的实体才能划进同一个聚合，划分依据是不变式（Invariant），不是数据关系的远近。聚合越大，锁范围越大、并发越差；聚合越小，跨聚合一致性问题越多。拿不准时优先划小，出现真实的强一致需求再合并。

## 工厂（Factory）

当一个 Entity/聚合根的创建需要组装多个字段、生成子实体、或者根据入参决定初始状态（不是简单的 `new + set`），把这段逻辑收进一个 Factory，而不是让调用方（Application 层）自己拼字段：

- Factory 放在 `ticket-catalog-domain`，和它创建的 Entity 同一个包。
- Factory 产出的对象必须已经满足"创建即一致"（Invariant at construction），调用方拿到手就是能直接用的合法对象，不需要再补校验。
- 简单场景（字段少、无子实体、无分支）用构造函数就够，不必强行套 Factory。

## Policy / Strategy 规范

- 每个 Policy 无状态、单例，至少两个方法：`canApply(...)` 判断是否命中，命中后再调具体的计算方法。
- Policy **只算不改**：返回计算结果（如伤害值），真正的状态变更交给调用它的 Domain Service 去做，这样 Policy 可以脱离 Entity 单独测试，也方便未来多个 Policy 叠加计算。
- 用一个 `Manager`/`Registry` 类按优先级顺序遍历 Policy 列表，新增规则只需要新增一个 Policy 类，不用改已有代码（满足 OCP）。
- Domain Service 实现多了不要都堆进一个大类/大包：按子领域拆子包（如 `domain/product/service`、`domain/schedule/service`），一个 Service 接口只服务一个子领域的编排需求，避免出现"万能 Service"。

```java
public class DamageManager {
    private static final List<DamagePolicy> POLICIES = List.of(
        new DragoonPolicy(), new DragonImmunityPolicy(), new OrcResistancePolicy(), new DefaultDamagePolicy()
    );

    public int calculateDamage(Player player, Weapon weapon, Monster monster) {
        for (DamagePolicy policy : POLICIES) {
            if (policy.canApply(player, weapon, monster)) {
                return policy.calculateDamage(player, weapon, monster);
            }
        }
        return 0;
    }
}
```

## Domain Event 规范（处理"副作用"）

当一个状态变更会触发一连串连带反应（A 导致 B，B 导致 C），不要把反应逻辑嵌套 if 写进触发它的 Domain Service，否则会无限膨胀。改用事件解耦：

1. 触发方只管 `EventBus.dispatch(new XxxEvent(...))`，不关心谁在监听。
2. 每个反应逻辑写成独立的 `XxxHandler`，各自 `register` 到 EventBus。
3. 新增一个连带反应 = 新增一个 Handler，不用改触发方代码。

已知局限：EventBus 目前只能是全局单例，会影响 Entity 单测的干净度；如果某个事件的处理时序/事务性要求很强（必须同步、必须成功），优先考虑直接在 Domain Service 里显式调用，只有"锦上添花"型的连带反应才用事件。

### 进程内事件（EventBus） vs 跨上下文事件（MQ）

两种 Domain Event 的传播方式不能混用，判断依据是事件的消费方是否跨 Bounded Context：

| | 进程内事件（EventBus） | 跨上下文事件（MQ / RocketMQ） |
|---|---|---|
| 消费方 | 同一个 Bounded Context 内部的 Handler | 别的 Bounded Context，甚至别的服务 |
| 触发方式 | Domain Service 内 `EventBus.dispatch(...)`，同步内存调用 | Domain Service/Application 层通过 MQ Producer（infrastructure）发消息 |
| 消费入口 | 同进程内的 `XxxHandler.register` | `ticket-catalog-trigger/listener` 的 MQ Listener，先转成本地 Command/Event 对象再调 ApplicationService |
| 一致性 | 可以做到强一致（同事务内派发+处理） | 只能最终一致，消费方要处理重复消费、乱序 |

本项目的真实例子：SupplierContext 拉取到供应商原始数据变更后，发布"原始数据变更事件"驱动 ProductAggContext 执行聚合——这是两个不同的 Bounded Context，必须走 MQ，不能指望 EventBus 进程内直达；而 ProductAggContext 内部"聚合完成后触发下架清单计算"这类同上下文连带反应，才用 EventBus。

## 自检清单

- [ ] 这个方法有没有直接改了"入参之外"的另一个 Entity 的字段？如果有，应该挪到 Domain Service。
- [ ] Entity 里有没有 `@Autowired` 字段？如果有，说明违反了"不能强依赖外部服务"的规则。
- [ ] 是否用继承来表达"同类但行为不同"的差异？评估能否换成 Policy 列表。
- [ ] 副作用逻辑是否已经嵌套超过 2 层 if？评估是否该拆成 Domain Event。
