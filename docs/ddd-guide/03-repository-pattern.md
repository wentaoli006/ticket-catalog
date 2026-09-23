# 03 · Repository 模式

> 模块归属：Repository **接口** 在 `ticket-catalog-domain`；Repository **实现** + DO + Converter 在 `ticket-catalog-infrastructure/persistent`。

## 三种对象不能混用

| | DO（Data Object） | Entity（实体） | DTO（传输对象） |
|---|---|---|---|
| 目的 | 数据库表映射 | 业务逻辑载体 | 适配调用方场景 |
| 所在层 | Infrastructure | Domain | Application/Api |
| 命名 | `XxxDO` | `Xxx` | `XxxDTO` / `XxxCommand` / `XxxQuery` |
| 字段 | 和数据库字段一一对应 | 和业务语言一致，尽量用 DP 而不是裸类型 | 和调用方商定，可以合并/裁剪多个 Entity 的字段 |
| 是否需要序列化 | 不需要 | 不需要 | 需要 |
| 转换器 | `Data Converter`（Entity↔DO，在 infrastructure） | — | `DTO Assembler`（Entity→DTO，在 app） |

**红线：Domain 层代码里不允许出现任何 `XxxDO` 类型；Application 层方法不允许返回 Entity，只能返回 DTO。**

转换器统一用 MapStruct 生成（`@Mapper` 接口 + `@Mapping` 标注差异字段），禁止手写大量 `set` 赋值代码，也不要用反射类的动态映射（性能差）。

## Repository 接口规范

1. **接口名不能带数据库语义**：用 `find` / `save` / `remove`，禁止 `insert`/`select`/`update`/`delete`。是插入还是更新，由实现类根据 ID 是否存在自己判断，调用方不关心。
2. **出入参必须是 Entity（或 Aggregate Root），不能是 DO**。Repository 接口本身要放在 `domain` 模块里，看不到任何 `infrastructure` 的类型。
3. **不要用框架自动生成的"万能 Repository"**（如 Spring Data 的通用 CRUD 接口）。简单场景够用，但一旦要加定制缓存/兜底逻辑就没有扩展空间，最终还是要重做。

```java
// domain 模块
public interface Repository<T extends Aggregate<ID>, ID extends Identifier> {
    T find(ID id);
    void remove(T aggregate);
    void save(T aggregate);
}

public interface OrderRepository extends Repository<Order, OrderId> {
    Long count(OrderQuery query);
    Page<Order> query(OrderQuery query);
    Order findInStore(OrderId id, StoreId storeId);
}
```

```java
// infrastructure 模块
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderDAO dao;
    private final OrderDataConverter converter = OrderDataConverter.INSTANCE;

    @Override
    public Order find(OrderId id) {
        return converter.fromData(dao.findById(id.getValue()));
    }

    @Override
    public void save(Order aggregate) {
        if (aggregate.getId() == null) {
            OrderDO orderDO = converter.toData(aggregate);
            dao.insert(orderDO);
            aggregate.setId(converter.fromData(orderDO).getId());
        } else {
            dao.update(converter.toData(aggregate));
        }
    }
}
```

## 聚合根场景：避免无用 UPDATE，考虑变更追踪

当一个 Aggregate Root 下有多个子 Entity（如主订单 + 多个子订单行），naive 实现会对"没变化的子实体"也发一次 UPDATE。如果这类聚合体量大、写操作频繁，引入 **Snapshot 变更追踪**：

- Repository `find()` 时把结果做一份内存 snapshot（ThreadLocal 隔离，避免多线程串数据）。
- `save()` 时和 snapshot 做 diff，只对真正变化的字段/子实体生成 UPDATE/INSERT/DELETE。
- 高并发场景下配合乐观锁（版本号字段），冲突后要同步更新本地 snapshot。

如果聚合体量小、写少，**不必**上变更追踪机制，直接全量更新即可，避免过度设计。

## 命名规范补充说明

外部资料里常见 `findById`/`delete` 这类命名（如 `UserRepository.delete(User user)`），本项目统一收敛成 `find`/`save`/`remove` 三个动词——`delete` 一律实现成 `remove`，避免团队内出现两套命名习惯。

## 迁移路径（老代码 → Repository）

1. 建 `Order` 实体类（初期字段可以先和 `OrderDO` 保持一致）。
2. 建 `OrderDataConverter`（MapStruct 两行代码搞定），补单测验证转换正确性。
3. 建 `OrderRepository` 接口 + 实现，单测验证。
4. 把原来直接用 `OrderDO` 的业务代码改成用 `Order`。
5. 把原来直接注入 `OrderDAO` 的业务代码改成注入 `OrderRepository`。
6. 单测跑通后，`Order` 和其业务逻辑就和底层实现完全解耦了，DAO/DB 怎么变都不影响上层。
