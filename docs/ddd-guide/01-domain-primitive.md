# 01 · Domain Primitive（DP）

> 模块归属：`ticket-catalog-types`（可对外暴露的 DP）；仅内部使用的 DP 也可以放在 `ticket-catalog-domain` 里，但优先放 types，方便被 api 层的 DTO/Command 复用。

## 是什么

Domain Primitive = 拥有精准定义、可自我验证、拥有行为的不可变 Value Object。比如 `PhoneNumber`、`Money`、`OrderId`、`ExchangeRate`。

判定标准（三者缺一都不算合格的 DP）：
- **Immutable**：字段 `private final`，没有 public setter。
- **Self-validating**：校验逻辑写在构造函数里，一旦构造成功即保证合法，后续用它的地方永远不需要再校验。
- **有行为**：和这个概念相关的无状态计算（如取手机号区号、汇率换算）收进类里，不要留在调用方或散落在 Utils 类里。

## 三条原则

1. **让隐性的概念显性化**：入参里如果有一段"从字符串里抠数据再做判断"的逻辑（如从电话号解析区号），说明这个字符串背后有个被忽略的概念，应该拆出一个 DP 类，把解析逻辑做成它的方法。
2. **让隐性的上下文显性化**：数值型参数如果暗含一个"默认单位/默认币种"之类的上下文，必须把上下文一起显性化，合并成一个 DP（`BigDecimal amount` + `Currency` → `Money`）。
3. **封装多对象行为**：一段逻辑涉及 2 个以上的值对象协作计算（如汇率转换涉及 `Money` + `Currency` + `ExchangeRate`），把该行为封装进其中一个 DP 的方法里，不要摊在业务方法里。

## 代码模板

```java
public final class PhoneNumber {
    private final String number;

    public PhoneNumber(String number) {
        if (number == null) {
            throw new ValidationException("number不能为空");
        }
        if (!isValid(number)) {
            throw new ValidationException("number格式错误");
        }
        this.number = number;
    }

    public String getAreaCode() {
        // 无状态计算逻辑收进类里，而不是散落在 Utils 里
        ...
    }

    public static boolean isValid(String number) {
        return number.matches("^0?[1-9]{2,3}-?\\d{8}$");
    }
}
```

多对象协作的 DP：

```java
@Value
public class ExchangeRate {
    BigDecimal rate;
    Currency from;
    Currency to;

    public Money exchange(Money fromMoney) {
        isTrue(this.from.equals(fromMoney.getCurrency()));
        return new Money(fromMoney.getAmount().multiply(rate), to);
    }
}
```

## 值对象的等价性

DP 本质上是值对象（Value Object），判等和实体（Entity）不同：

- **按值比较，不按引用/ID 比较**：两个 `Money` 只要金额和币种相同就应该 `equals`，禁止手写基于地址的默认 `equals`。统一用 Lombok `@Value`（自带 `final` 字段 + 无参 setter + `equals`/`hashCode`/`toString`）或 `@EqualsAndHashCode`，禁止手写大段字段比对代码。
- **不可变 + 可替换**：值对象的"修改"必须返回一个新实例，不能改自身字段（`private final` 决定了也改不了）。如 `Money.add(Money other)` 返回新的 `Money`，而不是 `this.amount += other.amount`。

```java
@Value
public class Money {
    BigDecimal amount;
    Currency currency;

    public Money add(Money other) {
        isTrue(this.currency.equals(other.currency));
        return new Money(this.amount.add(other.amount), this.currency); // 返回新实例，不改自身
    }
}
```

## 使用场景清单

- 有格式限制的 String：手机号、订单号、编码、地址
- 有范围限制的数值：百分比（0-100）、数量（>=0）、ID（>0）
- 可枚举的 int（不用 Enum，避免反序列化问题）
- 有业务含义的 Double/BigDecimal：金额、汇率、评分
- 复杂结构（Map/List 组合）：把所有操作封装掉，只暴露必要行为

## 效果检验（改造前 vs 改造后自查）

- [ ] 方法签名参数类型能否让阅读者一眼看出语义（而不是一串 `String, String, String`）？
- [ ] 方法体内还有没有 `if (xxx == null || !isValid(xxx))` 这类校验代码？如果有，说明该参数还没 DP 化。
- [ ] 是否存在只用一次、和某个基础类型强绑定的静态工具方法（如 `findAreaCode(String)`）？这类方法应该收进对应 DP。
- [ ] 单测数量：DP 本身需要 M 个用例覆盖校验分支；使用该 DP 的方法只需要覆盖 "为 null" 这一种异常分支，其余分支已被 DP 兜底。

## 老代码迁移四步法

1. 新建 DP 类，把散落各处的校验代码和无状态逻辑（如 `findAreaCode`）搬进去。
2. 不改方法签名，先把方法体内的裸校验替换成 `new XxxDP(...)`，方法仍然接收原始类型。
3. 新建一个签名为 DP 类型的新接口方法。
4. 迁移外部调用方到新接口，旧接口标记废弃后删除。
